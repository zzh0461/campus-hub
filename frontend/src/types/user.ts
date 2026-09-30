export type UserRole = 'USER' | 'ADMIN'

export type UserStatus = 'ACTIVE' | 'DISABLED'

export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  phone: string
  role: UserRole
  status: UserStatus
  bio: string
  createdAt: string
}

export interface UpdateProfileParams {
  nickname: string
  phone: string
  avatar: string
  bio: string
}

/**
 * 用户公开信息(他人可见的名片)
 * 对应后端 GET /api/users/{id}/public,用于商品详情页等展示"卖家介绍"/联系方式。
 * 刻意不含 username / role / status 等敏感字段。
 *
 * 联系方式只做登录门控(注册时手机号必填,登录用户之间互相可见):
 * - 未登录 → phoneVisible=false、phone=''、contactHint='登录后即可查看联系方式'
 * - 已登录 → phoneVisible=true、phone 为完整明文号码
 */
export interface UserPublicInfo {
  id: number
  nickname: string
  avatar: string
  bio: string
  createdAt: string
  /** 完整联系方式(明文);未登录或卖家未填写时为空串 */
  phone: string
  /** 当前请求者是否有权看到 phone,由后端判定 */
  phoneVisible: boolean
  /** 未登录时的引导文案,已登录时为空串 */
  contactHint: string
}
