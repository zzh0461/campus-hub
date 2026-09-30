import type { AxiosAdapter, AxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse, PageResult } from '@/types/common'
import type { Activity, ActivityPublishParams } from '@/types/activity'
import type {
  Announcement,
  AnnouncementPublishParams,
} from '@/types/announcement'
import type {
  LostFoundItem,
  LostFoundPublishParams,
  LostFoundStatus,
} from '@/types/lostFound'
import type {
  Product,
  ProductPublishParams,
  ProductStatus,
  ProductUpdateParams,
} from '@/types/market'
import type { User, UserStatus } from '@/types/user'
import {
  activities,
  activityCategories,
  activityRegistrations,
  announcements,
  dashboardData,
  demoAccounts,
  lostFoundItems,
  notifications,
  productCategories,
  products,
  users,
} from './data'

interface MockContext {
  method: string
  url: string
  path: string
  query: URLSearchParams
  params: Record<string, string>
  body: Record<string, unknown>
  authUser: User | null
}

type MockHandler = (ctx: MockContext) => ApiResponse<unknown> | Promise<ApiResponse<unknown>>

interface MockRoute {
  method: string
  pattern: RegExp
  handler: MockHandler
  delay: number
}

const routes: MockRoute[] = []

function mock(method: string, pattern: RegExp, handler: MockHandler, delay = 260): void {
  routes.push({ method, pattern, handler, delay })
}

const ok = (data: unknown): ApiResponse<unknown> => ({ code: 200, message: 'success', data })
const fail = (code: number, message: string): ApiResponse<unknown> => ({ code, message, data: null })

function page<T>(
  records: T[],
  total: number,
  pageNum: number,
  pageSize: number,
): PageResult<T> {
  return {
    records,
    total,
    pageNum,
    pageSize,
    pages: Math.max(1, Math.ceil(total / pageSize)),
  }
}

function slicePage<T>(list: T[], pageNum: number, pageSize: number): PageResult<T> {
  const start = (pageNum - 1) * pageSize
  return page(list.slice(start, start + pageSize), list.length, pageNum, pageSize)
}

function num(value: unknown, fallback = 0): number {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

function parseBody(config: AxiosRequestConfig): Record<string, unknown> {
  const data = config.data
  if (!data) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data) as Record<string, unknown>
    } catch {
      return {}
    }
  }
  if (data instanceof FormData) {
    const out: Record<string, unknown> = {}
    data.forEach((value, key) => {
      out[key] = value
    })
    return out
  }
  return data as Record<string, unknown>
}

function getHeader(config: AxiosRequestConfig, name: string): string | null {
  const headers = config.headers as
    | (Record<string, unknown> & { get?: (key: string) => string | undefined })
    | undefined
  if (!headers) return null
  const value = typeof headers.get === 'function' ? headers.get(name) : (headers[name] as string)
  return value ?? null
}

function findUserByToken(config: AxiosRequestConfig): User | null {
  const auth = getHeader(config, 'Authorization') ?? ''
  const match = /^Bearer\s+(.+)$/.exec(auth)
  if (!match) return null
  const token = match[1]
  const username = token.startsWith('mock-token-') ? token.slice('mock-token-'.length) : ''
  return users.find((u) => u.username === username) ?? null
}

function requireAuth(ctx: MockContext): User | null {
  if (!ctx.authUser) return null
  if (ctx.authUser.status === 'DISABLED') return null
  return ctx.authUser
}

function requireAdmin(ctx: MockContext): User | null {
  const user = requireAuth(ctx)
  return user && user.role === 'ADMIN' ? user : null
}

function withRegistered(activity: Activity, userId: number | null): Activity {
  const registered = userId != null && (activityRegistrations[activity.id] ?? []).includes(userId)
  return { ...activity, registered }
}

function toProductView(product: Product, userId: number | null): Product {
  void userId
  return product
}

function currentPageNum(ctx: MockContext): number {
  return Math.max(1, num(ctx.query.get('pageNum'), 1))
}

function currentPageSize(ctx: MockContext): number {
  return Math.max(1, num(ctx.query.get('pageSize'), 10))
}

function nextId(list: Array<{ id: number }>): number {
  return Math.max(0, ...list.map((item) => item.id)) + 1
}

// ---------------------------------------------------------------
// 认证
// ---------------------------------------------------------------

mock('post', /^\/api\/auth\/login$/, (ctx) => {
  const { username, password } = ctx.body as { username?: string; password?: string }
  const user = users.find((u) => u.username === username)
  if (!user || password !== '123456') {
    return fail(400, '用户名或密码错误')
  }
  if (user.status === 'DISABLED') {
    return fail(403, '账号已被禁用,请联系管理员')
  }
  return ok({
    token: `mock-token-${user.username}`,
    tokenType: 'Bearer',
    user,
  })
})

mock('post', /^\/api\/auth\/register$/, (ctx) => {
  const body = ctx.body as {
    username?: string
    password?: string
    nickname?: string
    phone?: string
  }
  if (!body.username || !body.password || !body.nickname || !body.phone) {
    return fail(400, '注册信息不完整')
  }
  if (users.some((u) => u.username === body.username)) {
    return fail(400, '用户名已存在')
  }
  const newUser: User = {
    id: nextId(users),
    username: body.username,
    nickname: body.nickname,
    avatar: `https://picsum.photos/seed/ch-avatar-new-${Date.now()}/120/120`,
    phone: body.phone,
    role: 'USER',
    status: 'ACTIVE',
    bio: '这个人很懒,还没有填写简介。',
    createdAt: new Date().toISOString(),
  }
  users.push(newUser)
  return ok(newUser)
})

// ---------------------------------------------------------------
// 用户
// ---------------------------------------------------------------

mock('get', /^\/api\/users\/me$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  return ok(user)
})

mock('put', /^\/api\/users\/me$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const { nickname, phone, avatar, bio } = ctx.body as Partial<User>
  if (nickname) user.nickname = nickname
  if (phone) user.phone = phone
  if (avatar) user.avatar = avatar
  if (bio !== undefined) user.bio = String(bio)
  return ok(user)
})

mock('get', /^\/api\/users\/favorites$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const list = products.filter((p) => p.favorite)
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

// 注:用户公开信息接口为页面需要的补充契约(见 README 说明)。
// 联系方式只做登录门控(与 user-service 一致):未登录不下发号码,已登录返回完整明文。
// 注册时手机号必填,故不存在"卖家没填手机号"的情况。
mock('get', /^\/api\/users\/(\d+)\/public$/, (ctx) => {
  const target = users.find((u) => u.id === num(ctx.params['1']))
  if (!target) return fail(404, '用户不存在')

  const loggedIn = ctx.authUser != null
  return ok({
    id: target.id,
    nickname: target.nickname,
    avatar: target.avatar,
    bio: target.bio,
    createdAt: target.createdAt,
    phone: loggedIn ? (target.phone ?? '') : '',
    phoneVisible: loggedIn,
    contactHint: loggedIn ? '' : '登录后即可查看联系方式',
  })
})

// ---------------------------------------------------------------
// 公告
// ---------------------------------------------------------------

mock('get', /^\/api\/announcements\/latest$/, () => {
  return ok(announcements.filter((a) => a.published).slice(0, 5))
})

mock('get', /^\/api\/announcements\/(\d+)$/, (ctx) => {
  const item = announcements.find((a) => a.id === num(ctx.params.id))
  if (!item) return fail(404, '公告不存在')
  item.viewCount += 1
  return ok(item)
})

mock('get', /^\/api\/announcements$/, (ctx) => {
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  const category = ctx.query.get('category') ?? ''
  let list = announcements.filter((a) => a.published)
  if (keyword) {
    list = list.filter((a) => a.title.toLowerCase().includes(keyword) || a.content.includes(keyword))
  }
  if (category) {
    list = list.filter((a) => a.category === category)
  }
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

// ---------------------------------------------------------------
// 二手市场
// ---------------------------------------------------------------

// 注:分类接口后端未显式定义,为分类筛选 UI 而补充(见 README 说明)
mock('get', /^\/api\/market\/categories$/, () => ok(productCategories))

mock('get', /^\/api\/market\/products\/recommend$/, (ctx) => {
  const userId = ctx.authUser?.id ?? null
  const list = products
    .filter((p) => p.status === 'ON_SALE')
    .sort((a, b) => b.favoriteCount * 2 + b.viewCount - (a.favoriteCount * 2 + a.viewCount))
    .slice(0, 8)
  return ok(list.map((p) => toProductView(p, userId)))
})

mock('get', /^\/api\/market\/products\/my$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const status = ctx.query.get('status') as ProductStatus | null
  let list = products.filter((p) => p.sellerId === user.id)
  if (status) list = list.filter((p) => p.status === status)
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('get', /^\/api\/market\/products\/(\d+)$/, (ctx) => {
  const item = products.find((p) => p.id === num(ctx.params.id))
  if (!item) return fail(404, '商品不存在')
  item.viewCount += 1
  return ok(toProductView(item, ctx.authUser?.id ?? null))
})

mock('post', /^\/api\/market\/products$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const body = ctx.body as unknown as ProductPublishParams
  if (!body.title || !body.categoryId || !(body.price > 0)) {
    return fail(400, '商品信息不完整')
  }
  const category = productCategories.find((c) => c.id === num(body.categoryId))
  const product: Product = {
    id: nextId(products),
    title: body.title,
    categoryId: num(body.categoryId),
    categoryName: category?.name ?? '其他',
    price: num(body.price),
    originalPrice: 0,
    description: body.description ?? '',
    images: body.images?.length ? body.images : ['https://picsum.photos/seed/campus-new/800/600'],
    sellerId: user.id,
    sellerName: user.nickname,
    sellerAvatar: user.avatar,
    status: 'ON_SALE',
    favoriteCount: 0,
    viewCount: 0,
    favorite: false,
    createdAt: new Date().toISOString(),
  }
  products.unshift(product)
  return ok(product)
})

// 注:商品编辑接口为页面需要的补充契约(见 README 说明)
mock('put', /^\/api\/market\/products\/(\d+)$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const item = products.find((p) => p.id === num(ctx.params.id))
  if (!item) return fail(404, '商品不存在')
  if (item.sellerId !== user.id && user.role !== 'ADMIN') {
    return fail(403, '只能操作自己的商品')
  }
  const body = ctx.body as unknown as ProductUpdateParams
  if (body.title) item.title = body.title
  if (body.categoryId) {
    item.categoryId = num(body.categoryId)
    item.categoryName = productCategories.find((c) => c.id === item.categoryId)?.name ?? '其他'
  }
  if (body.price !== undefined) item.price = num(body.price)
  if (body.description !== undefined) item.description = body.description
  if (body.images?.length) item.images = body.images
  if (body.status) item.status = body.status
  return ok(item)
})

mock('post', /^\/api\/market\/products\/(\d+)\/favorite$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const item = products.find((p) => p.id === num(ctx.params.id))
  if (!item) return fail(404, '商品不存在')
  item.favorite = true
  item.favoriteCount += 1
  return ok({ favorite: true })
})

mock('delete', /^\/api\/market\/products\/(\d+)\/favorite$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const item = products.find((p) => p.id === num(ctx.params.id))
  if (!item) return fail(404, '商品不存在')
  item.favorite = false
  item.favoriteCount = Math.max(0, item.favoriteCount - 1)
  return ok({ favorite: false })
})

mock('get', /^\/api\/market\/search$/, (ctx) => {
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  const categoryId = num(ctx.query.get('categoryId'))
  const sort = ctx.query.get('sort')
  let list = products.filter((p) => p.status === 'ON_SALE')
  if (keyword) {
    list = list.filter(
      (p) =>
        p.title.toLowerCase().includes(keyword) ||
        p.description.toLowerCase().includes(keyword),
    )
  }
  if (categoryId) list = list.filter((p) => p.categoryId === categoryId)
  if (sort === 'priceAsc') list = [...list].sort((a, b) => a.price - b.price)
  if (sort === 'priceDesc') list = [...list].sort((a, b) => b.price - a.price)
  if (sort === 'latest' || !sort) list = [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('get', /^\/api\/market\/products$/, (ctx) => {
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  const categoryId = num(ctx.query.get('categoryId'))
  const minPrice = num(ctx.query.get('minPrice'))
  const maxPrice = num(ctx.query.get('maxPrice'))
  const sort = ctx.query.get('sort')
  let list = products.filter((p) => p.status === 'ON_SALE')
  if (keyword) {
    list = list.filter(
      (p) =>
        p.title.toLowerCase().includes(keyword) ||
        p.description.toLowerCase().includes(keyword),
    )
  }
  if (categoryId) list = list.filter((p) => p.categoryId === categoryId)
  if (minPrice) list = list.filter((p) => p.price >= minPrice)
  if (maxPrice) list = list.filter((p) => p.price <= maxPrice)
  if (sort === 'priceAsc') list = [...list].sort((a, b) => a.price - b.price)
  if (sort === 'priceDesc') list = [...list].sort((a, b) => b.price - a.price)
  if (sort === 'latest' || !sort) list = [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

// ---------------------------------------------------------------
// 活动
// ---------------------------------------------------------------

mock('get', /^\/api\/activities\/categories$/, () => ok(activityCategories))

mock('get', /^\/api\/activities\/upcoming$/, (ctx) => {
  const userId = ctx.authUser?.id ?? null
  return ok(
    activities
      .filter((a) => a.status === 'UPCOMING')
      .slice(0, 3)
      .map((a) => withRegistered(a, userId)),
  )
})

mock('get', /^\/api\/activities\/my$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const list = activities.filter((a) => (activityRegistrations[a.id] ?? []).includes(user.id))
  return ok(slicePage(list.map((a) => withRegistered(a, user.id)), currentPageNum(ctx), currentPageSize(ctx)))
})

mock('get', /^\/api\/activities\/(\d+)$/, (ctx) => {
  const item = activities.find((a) => a.id === num(ctx.params.id))
  if (!item) return fail(404, '活动不存在')
  return ok(withRegistered(item, ctx.authUser?.id ?? null))
})

mock('get', /^\/api\/activities$/, (ctx) => {
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  const categoryId = num(ctx.query.get('categoryId'))
  const status = ctx.query.get('status') ?? ''
  const userId = ctx.authUser?.id ?? null
  let list = activities
  if (keyword) {
    list = list.filter(
      (a) => a.title.toLowerCase().includes(keyword) || a.description.includes(keyword),
    )
  }
  if (categoryId) list = list.filter((a) => a.categoryId === categoryId)
  if (status) list = list.filter((a) => a.status === status)
  return ok(slicePage(list.map((a) => withRegistered(a, userId)), currentPageNum(ctx), currentPageSize(ctx)))
})

mock('post', /^\/api\/activities\/(\d+)\/registrations$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const item = activities.find((a) => a.id === num(ctx.params.id))
  if (!item) return fail(404, '活动不存在')
  const list = activityRegistrations[item.id] ?? []
  if (list.includes(user.id)) return fail(400, '你已报名该活动')
  if (item.remainingParticipants <= 0) return fail(400, '活动名额已满')
  list.push(user.id)
  activityRegistrations[item.id] = list
  item.currentParticipants += 1
  item.remainingParticipants = Math.max(0, item.maxParticipants - item.currentParticipants)
  return ok(withRegistered(item, user.id))
})

mock('delete', /^\/api\/activities\/(\d+)\/registrations$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const item = activities.find((a) => a.id === num(ctx.params.id))
  if (!item) return fail(404, '活动不存在')
  const list = activityRegistrations[item.id] ?? []
  const index = list.indexOf(user.id)
  if (index < 0) return fail(400, '你尚未报名该活动')
  list.splice(index, 1)
  item.currentParticipants = Math.max(0, item.currentParticipants - 1)
  item.remainingParticipants = item.maxParticipants - item.currentParticipants
  return ok(withRegistered(item, user.id))
})

// ---------------------------------------------------------------
// 失物招领
// ---------------------------------------------------------------

// 联系方式(contact)只对已登录用户下发:未登录时置空 + contactVisible=false,
// 与 content-service 的 LostFoundController 口径一致(发布者填的是自由文本,无法脱敏)
mock('get', /^\/api\/lost-found\/(\d+)$/, (ctx) => {
  const item = lostFoundItems.find((l) => l.id === num(ctx.params.id))
  if (!item) return fail(404, '记录不存在')
  const loggedIn = ctx.authUser != null
  return ok({
    ...item,
    contact: loggedIn ? item.contact : '',
    contactVisible: loggedIn,
  })
})

// 我发布的失物招领(需登录):只返回自己发布的,与 content-service 的 /my 接口一致
// (与详情路由不冲突:"my" 不匹配 \d+,不会被 /^\/api\/lost-found\/(\d+)$/ 捕获)
mock('get', /^\/api\/lost-found\/my$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  let list = lostFoundItems.filter((l) => l.publisherId === user.id)
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  const type = ctx.query.get('type') ?? ''
  if (keyword) {
    list = list.filter(
      (l) =>
        l.title.toLowerCase().includes(keyword) ||
        l.description.toLowerCase().includes(keyword) ||
        l.location.includes(keyword),
    )
  }
  if (type) list = list.filter((l) => l.type === type)
  list = [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  // 自己的发布保留联系方式原值(与后端 getMyLostFoundList 一致)
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('post', /^\/api\/lost-found$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const body = ctx.body as unknown as LostFoundPublishParams
  if (!body.title || !body.type || !body.location) {
    return fail(400, '发布信息不完整')
  }
  const item: LostFoundItem = {
    id: nextId(lostFoundItems),
    type: body.type,
    title: body.title,
    description: body.description ?? '',
    location: body.location,
    contact: body.contact ?? '',
    images: body.images ?? [],
    status: 'OPEN',
    publisherId: user.id,
    publisherName: user.nickname,
    publisherAvatar: user.avatar,
    createdAt: new Date().toISOString(),
  }
  lostFoundItems.unshift(item)
  return ok(item)
})

mock('delete', /^\/api\/lost-found\/(\d+)$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const index = lostFoundItems.findIndex((l) => l.id === num(ctx.params.id))
  if (index < 0) return fail(404, '记录不存在')
  const item = lostFoundItems[index]
  if (item.publisherId !== user.id && user.role !== 'ADMIN') {
    return fail(403, '只能删除自己发布的记录')
  }
  lostFoundItems.splice(index, 1)
  return ok(null)
})

mock('get', /^\/api\/lost-found$/, (ctx) => {
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  const type = ctx.query.get('type') ?? ''
  let list = lostFoundItems
  if (keyword) {
    list = list.filter(
      (l) =>
        l.title.toLowerCase().includes(keyword) ||
        l.description.toLowerCase().includes(keyword) ||
        l.location.includes(keyword),
    )
  }
  if (type) list = list.filter((l) => l.type === type)
  list = [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  // 列表接口不下发联系方式(与 content-service 的 stripContact 一致):
  // 列表页不展示电话,需要联系走详情页 + 登录校验
  const pageResult = slicePage(list, currentPageNum(ctx), currentPageSize(ctx))
  pageResult.records = pageResult.records.map((item) => ({
    ...item,
    contact: '',
    contactVisible: false,
  }))
  return ok(pageResult)
})

// ---------------------------------------------------------------
// 通知中心
// ---------------------------------------------------------------

mock('get', /^\/api\/notifications\/unread-count$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  return ok({ count: notifications.filter((n) => !n.read).length })
})

mock('put', /^\/api\/notifications\/(\d+)\/read$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const item = notifications.find((n) => n.id === num(ctx.params.id))
  if (!item) return fail(404, '通知不存在')
  item.read = true
  return ok(null)
})

mock('put', /^\/api\/notifications\/read-all$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  notifications.forEach((n) => {
    n.read = true
  })
  return ok(null)
})

mock('delete', /^\/api\/notifications\/(\d+)$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const index = notifications.findIndex((n) => n.id === num(ctx.params.id))
  if (index < 0) return fail(404, '通知不存在')
  notifications.splice(index, 1)
  return ok(null)
})

mock('get', /^\/api\/notifications$/, (ctx) => {
  const user = requireAuth(ctx)
  if (!user) return fail(401, '未登录或登录已过期')
  const read = ctx.query.get('read')
  let list = [...notifications].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  if (read === 'true') list = list.filter((n) => n.read)
  if (read === 'false') list = list.filter((n) => !n.read)
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

// ---------------------------------------------------------------
// 文件上传
// ---------------------------------------------------------------

mock(
  'post',
  /^\/api\/upload\/image$/,
  (ctx) => {
    const user = requireAuth(ctx)
    if (!user) return fail(401, '未登录或登录已过期')
    const file = ctx.body.file as File | undefined
    if (!file) return fail(400, '未选择文件')
    const ext = file.name.split('.').pop() ?? 'jpg'
    return ok({
      url: `https://picsum.photos/seed/upload-${Date.now()}-${Math.floor(Math.random() * 1000)}/800/600.${ext === 'gif' ? 'jpg' : ext}`,
    })
  },
  420,
)

// ---------------------------------------------------------------
// 管理后台
// ---------------------------------------------------------------

mock('get', /^\/api\/admin\/dashboard$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  return ok(dashboardData)
})

mock('get', /^\/api\/admin\/users$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  let list = users
  if (keyword) {
    list = list.filter(
      (u) =>
        u.username.toLowerCase().includes(keyword) ||
        u.nickname.toLowerCase().includes(keyword) ||
        u.phone.includes(keyword),
    )
  }
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('put', /^\/api\/admin\/users\/(\d+)\/status$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const user = users.find((u) => u.id === num(ctx.params.id))
  if (!user) return fail(404, '用户不存在')
  const { status } = ctx.body as { status?: UserStatus }
  if (status !== 'ACTIVE' && status !== 'DISABLED') return fail(400, '状态参数不合法')
  user.status = status
  return ok(user)
})

mock('delete', /^\/api\/admin\/users\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const index = users.findIndex((u) => u.id === num(ctx.params.id))
  if (index < 0) return fail(404, '用户不存在')
  if (users[index].role === 'ADMIN') return fail(400, '不能删除管理员账号')
  users.splice(index, 1)
  return ok(null)
})

mock('get', /^\/api\/admin\/announcements$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  let list = announcements
  if (keyword) list = list.filter((a) => a.title.toLowerCase().includes(keyword))
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('post', /^\/api\/admin\/announcements$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const body = ctx.body as unknown as AnnouncementPublishParams
  if (!body.title || !body.content || !body.category) return fail(400, '公告信息不完整')
  const item: Announcement = {
    id: nextId(announcements),
    title: body.title,
    content: body.content,
    category: body.category,
    author: ctx.authUser?.nickname ?? '管理员',
    published: body.published ?? true,
    viewCount: 0,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  }
  announcements.unshift(item)
  return ok(item)
})

mock('put', /^\/api\/admin\/announcements\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const item = announcements.find((a) => a.id === num(ctx.params.id))
  if (!item) return fail(404, '公告不存在')
  const body = ctx.body as unknown as AnnouncementPublishParams
  if (body.title !== undefined) item.title = body.title
  if (body.content !== undefined) item.content = body.content
  if (body.category !== undefined) item.category = body.category
  if (body.published !== undefined) item.published = body.published
  item.updatedAt = new Date().toISOString()
  return ok(item)
})

mock('delete', /^\/api\/admin\/announcements\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const index = announcements.findIndex((a) => a.id === num(ctx.params.id))
  if (index < 0) return fail(404, '公告不存在')
  announcements.splice(index, 1)
  return ok(null)
})

mock('get', /^\/api\/admin\/products$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  let list = products
  if (keyword) list = list.filter((p) => p.title.toLowerCase().includes(keyword))
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('put', /^\/api\/admin\/products\/(\d+)\/status$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const item = products.find((p) => p.id === num(ctx.params.id))
  if (!item) return fail(404, '商品不存在')
  const { status } = ctx.body as { status?: ProductStatus }
  if (!['ON_SALE', 'OFF_SHELF', 'SOLD'].includes(status ?? '')) {
    return fail(400, '状态参数不合法')
  }
  item.status = status as ProductStatus
  return ok(item)
})

mock('delete', /^\/api\/admin\/products\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const index = products.findIndex((p) => p.id === num(ctx.params.id))
  if (index < 0) return fail(404, '商品不存在')
  products.splice(index, 1)
  return ok(null)
})

mock('get', /^\/api\/admin\/activities$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  let list = activities
  if (keyword) list = list.filter((a) => a.title.toLowerCase().includes(keyword))
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('post', /^\/api\/admin\/activities$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const body = ctx.body as unknown as ActivityPublishParams
  if (!body.title || !body.location || !body.startTime || !body.endTime || !body.maxParticipants) {
    return fail(400, '活动信息不完整')
  }
  const category = activityCategories.find((c) => c.id === num(body.categoryId))
  const item: Activity = {
    id: nextId(activities),
    title: body.title,
    description: body.description ?? '',
    categoryId: num(body.categoryId),
    categoryName: category?.name ?? '其他',
    location: body.location,
    cover: body.cover || `https://picsum.photos/seed/campus-new-a${Date.now()}/960/540`,
    startTime: body.startTime,
    endTime: body.endTime,
    maxParticipants: num(body.maxParticipants),
    currentParticipants: 0,
    remainingParticipants: num(body.maxParticipants),
    registered: false,
    organizer: ctx.authUser?.nickname ?? '管理员',
    organizerId: ctx.authUser?.id ?? 0,
    status: new Date(body.startTime).getTime() > Date.now() ? 'UPCOMING' : 'ONGOING',
    createdAt: new Date().toISOString(),
  }
  activities.unshift(item)
  return ok(item)
})

mock('put', /^\/api\/admin\/activities\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const item = activities.find((a) => a.id === num(ctx.params.id))
  if (!item) return fail(404, '活动不存在')
  const body = ctx.body as unknown as ActivityPublishParams
  if (body.title !== undefined) item.title = body.title
  if (body.description !== undefined) item.description = body.description
  if (body.categoryId) {
    item.categoryId = num(body.categoryId)
    item.categoryName = activityCategories.find((c) => c.id === item.categoryId)?.name ?? '其他'
  }
  if (body.location !== undefined) item.location = body.location
  if (body.startTime !== undefined) item.startTime = body.startTime
  if (body.endTime !== undefined) item.endTime = body.endTime
  if (body.maxParticipants !== undefined) {
    item.maxParticipants = num(body.maxParticipants)
    item.remainingParticipants = Math.max(0, item.maxParticipants - item.currentParticipants)
  }
  if (body.cover !== undefined) item.cover = body.cover
  return ok(item)
})

mock('delete', /^\/api\/admin\/activities\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const index = activities.findIndex((a) => a.id === num(ctx.params.id))
  if (index < 0) return fail(404, '活动不存在')
  activities.splice(index, 1)
  return ok(null)
})

mock('get', /^\/api\/admin\/lost-found$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const keyword = (ctx.query.get('keyword') ?? '').trim().toLowerCase()
  let list = lostFoundItems
  if (keyword) list = list.filter((l) => l.title.toLowerCase().includes(keyword))
  return ok(slicePage(list, currentPageNum(ctx), currentPageSize(ctx)))
})

mock('put', /^\/api\/admin\/lost-found\/(\d+)\/status$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const item = lostFoundItems.find((l) => l.id === num(ctx.params.id))
  if (!item) return fail(404, '记录不存在')
  const { status } = ctx.body as { status?: LostFoundStatus }
  if (status !== 'OPEN' && status !== 'RESOLVED') return fail(400, '状态参数不合法')
  item.status = status
  return ok(item)
})

mock('delete', /^\/api\/admin\/lost-found\/(\d+)$/, (ctx) => {
  if (!requireAdmin(ctx)) return fail(403, '仅管理员可访问')
  const index = lostFoundItems.findIndex((l) => l.id === num(ctx.params.id))
  if (index < 0) return fail(404, '记录不存在')
  lostFoundItems.splice(index, 1)
  return ok(null)
})

// ---------------------------------------------------------------
// 适配器
// ---------------------------------------------------------------

function matchRoute(method: string, path: string): { route: MockRoute; params: Record<string, string> } | null {
  const lower = method.toLowerCase()
  for (const route of routes) {
    if (route.method !== lower) continue
    const match = route.pattern.exec(path)
    if (!match) continue
    const params: Record<string, string> = {}
    if (match.groups) {
      Object.assign(params, match.groups)
    } else {
      match.slice(1).forEach((value, index) => {
        params[String(index + 1)] = value
      })
    }
    return { route, params }
  }
  return null
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 给每次响应叠加 0~180ms 抖动。
 *
 * 真实网络下响应到达顺序本来就不保证(这也是"快速切页/换每页条数偶尔列表变空"的根因),
 * 固定延时会让 Mock 里的并发请求永远按发起顺序返回,把竞态问题掩盖掉。
 * 加抖动后,usePagination 的"只采纳最后一次响应"才真的被验证到。
 */
function jitter(): number {
  return Math.floor(Math.random() * 180)
}

export const mockAdapter: AxiosAdapter = async (config) => {
  const path = (config.url ?? '').split('?')[0]
  const queryString = (config.url ?? '').includes('?')
    ? (config.url ?? '').split('?')[1] ?? ''
    : ''
  // axios 自定义适配器不会把 config.params 拼进 url,这里手动合并,
  // 否则分页/筛选参数全部丢失(pageSize 缺省会被 num(null)=0 顶成 1)
  const mergedQuery = new URLSearchParams(queryString)
  const params = config.params as Record<string, unknown> | undefined
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null) mergedQuery.set(key, String(value))
    }
  }
  const method = (config.method ?? 'get').toLowerCase()
  const matched = matchRoute(method, path)
  const ctx: MockContext = {
    method,
    url: config.url ?? '',
    path,
    query: mergedQuery,
    params: matched?.params ?? {},
    body: parseBody(config),
    authUser: findUserByToken(config),
  }

  await delay((matched?.route.delay ?? 320) + jitter())

  const result = matched
    ? await matched.route.handler(ctx)
    : fail(404, `Mock 未定义该接口:${method.toUpperCase()} ${path}`)

  return {
    data: result,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  } as AxiosResponse
}

// 供开发调试时快速查看可用演示账号
export const mockDemoAccounts = demoAccounts

export default mockAdapter
