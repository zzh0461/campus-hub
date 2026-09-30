/** 统一响应包装:所有后端接口必须返回该结构 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** 统一分页响应结构 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/** 统一分页请求参数 */
export interface PageParams {
  pageNum: number
  pageSize: number
}

/** 排序方向 */
export type SortOrder = 'asc' | 'desc'

/** 通用图片上传结果 */
export interface UploadResult {
  url: string
}

/**
 * 联系方式可见性(商品/失物招领等"联系发布者"场景共用)
 *
 * - anonymous:未登录,引导去登录
 * - granted:已登录,完整明文展示联系方式
 *
 * 说明:曾短暂做过"收藏后才可见"的 gated 中间态,因注册时手机号本就必填、
 * 门控没有实际约束价值,已去掉,只保留登录门控。
 */
export type ContactVisibility = 'anonymous' | 'granted'

/**
 * 一条联系方式(可能有多条:手机号 / 微信 / QQ …)
 *
 * value 始终是**完整明文**,不做脱敏;可见性由 ContactVisibility 在上层控制。
 */
export interface ContactItem {
  /** 展示用的渠道名,如「手机号」「微信」 */
  label: string
  /** 完整联系方式明文 */
  value: string
  /** 复制成功后的提示,缺省用「{label}已复制」 */
  copyTip?: string
}
