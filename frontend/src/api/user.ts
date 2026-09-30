import { http } from '@/utils/request'
import type { PageParams, PageResult } from '@/types/common'
import type { Product } from '@/types/market'
import type { UpdateProfileParams, User, UserPublicInfo } from '@/types/user'

/** 获取当前登录用户信息 */
export function getMe(): Promise<User> {
  return http.get<User>('/api/users/me')
}

/**
 * 获取指定用户的公开信息(商品详情页"卖家介绍"/联系方式弹窗用)
 *
 * 联系方式只做登录门控(注册时手机号必填,登录用户之间互相可见):
 * 未登录返回 phoneVisible=false + contactHint,前端据此渲染"去登录"引导;
 * 已登录返回完整明文手机号。
 *
 * @param id 目标用户 ID
 */
export function getUserPublicInfo(id: number): Promise<UserPublicInfo> {
  return http.get<UserPublicInfo>(`/api/users/${id}/public`)
}

/** 更新当前用户信息 */
export function updateMe(params: UpdateProfileParams): Promise<User> {
  return http.put<User>('/api/users/me', params)
}

/** 我的收藏 */
export function getMyFavorites(params: PageParams): Promise<PageResult<Product>> {
  return http.get<PageResult<Product>>('/api/users/favorites', { params })
}
