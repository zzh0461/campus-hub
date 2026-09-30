import type { PageParams } from './common'

export type LostFoundType = 'LOST' | 'FOUND'

export type LostFoundStatus = 'OPEN' | 'RESOLVED'

export interface LostFoundItem {
  id: number
  type: LostFoundType
  title: string
  description: string
  location: string
  /** 联系方式(发布者自行填写的自由文本:电话 / QQ / 微信) */
  contact: string
  /**
   * 当前请求者是否有权看到 contact:仅已登录用户为 true。
   * 未登录时后端会把 contact 置空并返回 false,前端据此引导登录。
   *
   * 列表接口不返回该字段(取不到即为 undefined,按"可见"处理,因为列表不展示联系方式)。
   */
  contactVisible?: boolean
  images: string[]
  status: LostFoundStatus
  publisherId: number
  publisherName: string
  publisherAvatar: string
  createdAt: string
}

export interface LostFoundQuery extends PageParams {
  keyword?: string
  type?: LostFoundType | ''
}

export interface LostFoundPublishParams {
  type: LostFoundType
  title: string
  description: string
  location: string
  contact: string
  images: string[]
}
