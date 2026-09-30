import { nextTick, onMounted, ref, shallowRef, watch, type Ref } from 'vue'
import type { PageResult } from '@/types/common'

/** 每页条数候选项：与 Pagination.vue 的 show-size-picker 保持一致 */
export const PAGE_SIZE_OPTIONS = [10, 12, 20, 50]

export interface UsePaginationOptions {
  /** 初始页码，默认 1 */
  initialPageNum?: number
  /** 初始每页条数，默认 10（不在 PAGE_SIZE_OPTIONS 里时自动回落到 10） */
  initialPageSize?: number
}

/** 分页请求参数：直接透传给 api 层的 { pageNum, pageSize } */
export interface PaginationParams {
  pageNum: number
  pageSize: number
}

/** 拉取一页数据：接收分页参数，返回统一分页结构 */
export type PageFetcher<T> = (params: PaginationParams) => Promise<PageResult<T>>

/**
 * 页面的数据函数：内部调用 {@link UsePaginationReturn.load} 拉数据并自行赋值。
 *
 * 保持 `() => Promise<void>` 的形状，是为了让各列表页原来的 `load()` / `fetchList()`
 * 几乎不用改结构（只是把函数体重写成调用 load(fetcher)）。
 */
export type PageLoadFn = () => Promise<void>

export interface UsePaginationReturn<T> {
  /** 当前页数据；请求失败或被竞态丢弃时保持上一次的值（不会变成空列表） */
  result: Ref<PageResult<T> | null>
  /** 当前页码（绑定 `v-model:page-num`） */
  pageNum: Ref<number>
  /** 当前每页条数（绑定 `:page-size`，变更走 handlePageSizeChange） */
  pageSize: Ref<number>
  /** 最近一次**有效**响应的总条数（绑定 `:total`，可直接替换原来的 total ref） */
  total: Ref<number>
  /** 是否正在加载 */
  loading: Ref<boolean>
  /** 是否加载失败 */
  error: Ref<boolean>
  /**
   * 按当前 pageNum / pageSize 拉取数据（写入 result / total）。
   *
   * @param fetcher 接收 { pageNum, pageSize }，返回统一分页结构
   * @returns 后端分页结果；被竞态丢弃或失败时返回 null
   */
  load: (fetcher: PageFetcher<T>) => Promise<PageResult<T> | null>
  /**
   * 接上页面的数据函数（首屏加载 + 翻页监听）。
   *
   * 只监听 pageNum：换每页条数由 handlePageSizeChange 自己重载，
   * 若这里再监听 pageSize，换条数时就会多发一次并发请求。
   *
   * @param pageLoad 页面的数据函数，形如 `async () => { await load(fetcher) }`
   */
  bind: (pageLoad: PageLoadFn) => void
  /** 处理 `<pagination>` 的 `update:page-size`：重置到第 1 页并只重新加载一次 */
  handlePageSizeChange: (size: number, pageLoad: PageLoadFn) => Promise<void>
  /** 回到第 1 页并重新加载（搜索 / 切换筛选条件时用） */
  reset: (pageLoad: PageLoadFn) => Promise<void>
}

/**
 * 列表页分页统一逻辑。
 *
 * 解决三个反复踩到的问题：
 * 1. **换每页条数后列表空白**：换条数时如果只改 pageSize 不重置页码，就会请求一个在新
 *    条数下已经越界的页码，后端返回空列表，页面看起来就"搜不到东西了"。这里强制回第 1 页。
 * 2. **并发请求乱序覆盖**：换条数会同时改 pageNum 和 pageSize，两个请求谁后到谁写结果，
 *    旧请求（空列表）后到就把正确结果盖掉了。这里用请求序号只采纳最后一次响应。
 * 3. **只监听页码不监听条数**：部分后台页漏了 pageSize 的监听，换条数根本不重新请求。
 *    这里把两件事一起接好，页面不再需要自己写 watch。
 *
 * 页面用法：
 * ```ts
 * const {
 *   result, pageNum, pageSize, total, loading, error,
 *   load, handlePageSizeChange, reset, bind,
 * } = usePagination<Product>({ initialPageSize: 12 })
 *
 * async function fetchList(): Promise<void> {
 *   await load((p) => getProducts({ ...p, categoryId: filters.categoryId ?? undefined }))
 * }
 * bind(fetchList)                       // 自动首屏加载 + 翻页重新拉取
 * // 搜索：await reset(fetchList)       // 回到第 1 页（替代手写 pageNum = 1; load()）
 * ```
 * 模板里（`result.records` 换成 `result?.records ?? []`）：
 * ```html
 * <pagination v-model:page-num="pageNum" :page-size="pageSize" :total="total"
 *   @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)" />
 * ```
 */
export function usePagination<T>(options: UsePaginationOptions = {}): UsePaginationReturn<T> {
  const requested = options.initialPageSize ?? PAGE_SIZE_OPTIONS[0]
  const pageSize = ref(PAGE_SIZE_OPTIONS.includes(requested) ? requested : PAGE_SIZE_OPTIONS[0])
  const pageNum = ref(options.initialPageNum ?? 1)
  const total = ref(0)
  const loading = ref(true)
  const error = ref(false)
  // 用 shallowRef：分页结果每页整体替换，不需要深层响应式，
  // 也避免 Ref 深解包把 PageResult<T> 推成 UnwrapRefSimple<T> 造成类型不匹配
  const result = shallowRef<PageResult<T> | null>(null)

  /** 请求序号：每发起一次请求自增，响应回来时只认最新的序号 */
  let requestSeq = 0
  /**
   * 抑制标记：换每页条数时会同时改 pageSize 和 pageNum，
   * 页面 watch 会因"页码被重置"再触发一次请求。
   * 置位期间 watch 回调直接跳过，保证换条数只发一次请求。
   *
   * 用计数器而非布尔：连点两次每页条数时，前一次还没复位也不影响后一次的语义。
   */
  let suppressWatchCount = 0

  async function run<R>(task: () => Promise<R>): Promise<R | null> {
    const seq = ++requestSeq
    loading.value = true
    error.value = false
    try {
      const data = await task()
      // 已经发出更新的请求了，本次结果作废（不写结果、不写 loading）
      if (seq !== requestSeq) return null
      return data
    } catch {
      if (seq !== requestSeq) return null
      error.value = true
      return null
    } finally {
      // 只有最后一次请求有权结束 loading，避免旧请求提前关掉 loading 导致骨架屏闪断
      if (seq === requestSeq) loading.value = false
    }
  }

  const load = (fetcher: PageFetcher<T>): Promise<PageResult<T> | null> =>
    run(async () => {
      // 快照本次参数，避免 await 期间页码又被改掉
      const data = await fetcher({ pageNum: pageNum.value, pageSize: pageSize.value })
      // 统一在这里落库：页面只需 `await load(...)`，不用各自再写一遍 result.value = ...
      total.value = data.total ?? 0
      result.value = data
      return data
    })

  const reset = async (pageLoad: PageLoadFn): Promise<void> => {
    pageNum.value = 1
    await pageLoad()
  }

  async function handlePageSizeChange(size: number, pageLoad: PageLoadFn): Promise<void> {
    if (size === pageSize.value) return
    // 先置抑制标记，再改状态：watch 是异步冲刷的，这样它的回调一定能看到标记
    suppressWatchCount += 1
    // 关键：换条数必须回到第 1 页，否则请求的是新条数下已越界的页码，后端只返回空列表
    pageSize.value = size
    pageNum.value = 1
    try {
      // 本方法自己负责重载，页面 watch 那一次被抑制掉，因此只发一次请求
      await pageLoad()
    } finally {
      // 等本轮 watch 回调都跑完再复位，避免过早放开又触发一次
      await nextTick()
      suppressWatchCount = Math.max(0, suppressWatchCount - 1)
    }
  }

  function bind(pageLoad: PageLoadFn): void {
    watch(pageNum, () => {
      if (suppressWatchCount > 0) return
      void pageLoad()
    })
    onMounted(() => {
      void pageLoad()
    })
  }

  return {
    result,
    pageNum,
    pageSize,
    total,
    loading,
    error,
    load,
    bind,
    handlePageSizeChange,
    reset,
  }
}
