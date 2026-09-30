<script setup lang="ts">
import { h, ref } from 'vue'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NPopconfirm,
  NSpace,
  type DataTableColumns,
  useMessage,
} from 'naive-ui'
import { PhMagnifyingGlass } from '@phosphor-icons/vue'
import {
  deleteAdminProduct,
  getAdminProducts,
  updateAdminProductStatus,
} from '@/api/admin'
import type { Product, ProductStatus } from '@/types/market'
import { formatDateTime, formatPrice } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

const message = useMessage()

const {
  result,
  pageNum,
  pageSize,
  total,
  loading,
  error,
  load,
  handlePageSizeChange,
  reset,
  bind,
} = usePagination<Product>()

const keyword = ref('')

async function fetchList(): Promise<void> {
  await load((p) => getAdminProducts({ ...p, keyword: keyword.value.trim() || undefined }))
}

/** 搜索:关键词变了必须回到第 1 页,否则会留在越界页码上搜不到东西 */
async function handleSearch(): Promise<void> {
  await reset(fetchList)
}

async function toggleStatus(product: Product): Promise<void> {
  const next: ProductStatus = product.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE'
  try {
    await updateAdminProductStatus(product.id, next)
    product.status = next
    message.success(next === 'ON_SALE' ? '已上架' : '已下架')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleDelete(product: Product): Promise<void> {
  try {
    await deleteAdminProduct(product.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((p) => p.id !== product.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

const columns: DataTableColumns<Product> = [
  {
    title: '商品',
    key: 'product',
    width: 280,
    render: (row) =>
      h('div', { class: 'product-cell' }, [
        h('img', {
          class: 'product-cell__img',
          src: row.images[0],
          alt: row.title,
          loading: 'lazy',
        }),
        h('div', { class: 'product-cell__text' }, [
          h('strong', row.title),
          h('span', `${row.categoryName} · 卖家:${row.sellerName}`),
        ]),
      ]),
  },
  {
    title: '价格',
    key: 'price',
    width: 110,
    render: (row) => h('span', { class: 'num product-price' }, formatPrice(row.price)),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h('span', null, row.status === 'ON_SALE' ? '在售' : row.status === 'SOLD' ? '已售出' : '已下架'),
  },
  {
    title: '发布时间',
    key: 'createdAt',
    width: 170,
    render: (row) => h('span', { class: 'num' }, formatDateTime(row.createdAt)),
  },
  {
    title: '浏览/收藏',
    key: 'stats',
    width: 110,
    render: (row) =>
      h('span', { class: 'num text-secondary' }, `${row.viewCount} / ${row.favoriteCount}`),
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) =>
      h(NSpace, { size: 4 }, {
        default: () => [
          row.status !== 'SOLD'
            ? h(
                NPopconfirm,
                { onPositiveClick: () => toggleStatus(row) },
                {
                  trigger: () =>
                    h(
                      NButton,
                      { size: 'small', type: row.status === 'ON_SALE' ? 'warning' : 'success', ghost: true },
                      { default: () => (row.status === 'ON_SALE' ? '下架' : '上架') },
                    ),
                  default: () => (row.status === 'ON_SALE' ? '确认下架该商品?' : '确认上架该商品?'),
                },
              )
            : null,
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' }),
              default: () => '删除后不可恢复,确认删除该商品?',
            },
          ),
        ],
      }),
  },
]

bind(fetchList)
</script>

<template>
  <div class="manage-page">
    <div class="manage-toolbar">
      <n-input
        v-model:value="keyword"
        placeholder="搜索商品标题"
        clearable
        class="manage-toolbar__search"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon :component="PhMagnifyingGlass" />
        </template>
      </n-input>
      <n-button type="primary" @click="handleSearch">搜索</n-button>
    </div>

    <div v-if="loading" class="manage-loading">
      <loading-state :rows="8" variant="panel" />
    </div>
    <div v-else-if="error" class="manage-error">
      <empty-state
        title="商品列表加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <n-data-table
      v-else
      :columns="columns"
      :data="result?.records ?? []"
      :row-key="(row: Product) => row.id"
      :bordered="false"
      class="manage-table"
    />

    <pagination
      v-if="total > 0"
      v-model:page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)"
    />
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.manage-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 420px) auto;
  gap: 12px;
  margin-bottom: 18px;
}

.manage-loading {
  padding: 24px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
}

.manage-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.manage-table {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;
}

:deep(.product-cell) {
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.product-cell__img) {
  width: 52px;
  height: 40px;
  flex-shrink: 0;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid $color-border;
}

:deep(.product-cell__text) {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

:deep(.product-cell__text strong) {
  font-size: 13px;
  color: $color-text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.product-cell__text span) {
  font-size: 12px;
  color: $color-text-tertiary;
}

:deep(.product-price) {
  color: $color-danger;
  font-weight: 600;
}
</style>
