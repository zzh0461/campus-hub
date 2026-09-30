import { http } from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  LostFoundItem,
  LostFoundPublishParams,
  LostFoundQuery,
  LostFoundStatus,
} from '@/types/lostFound'

/** 失物招领分页列表 */
export function getLostFoundList(params: LostFoundQuery): Promise<PageResult<LostFoundItem>> {
  return http.get<PageResult<LostFoundItem>>('/api/lost-found', { params })
}

/** 我发布的失物招领分页列表 */
export function getMyLostFoundList(params: LostFoundQuery): Promise<PageResult<LostFoundItem>> {
  return http.get<PageResult<LostFoundItem>>('/api/lost-found/my', { params })
}

/** 失物招领详情 */
export function getLostFoundDetail(id: number): Promise<LostFoundItem> {
  return http.get<LostFoundItem>(`/api/lost-found/${id}`)
}

/** 发布失物/招领 */
export function publishLostFound(params: LostFoundPublishParams): Promise<LostFoundItem> {
  return http.post<LostFoundItem>('/api/lost-found', params)
}

/** 删除自己的发布 */
export function deleteLostFound(id: number): Promise<null> {
  return http.delete<null>(`/api/lost-found/${id}`)
}

/** 更新自己发布的状态（OPEN 进行中 / RESOLVED 已解决） */
export function updateLostFoundStatus(id: number, status: LostFoundStatus): Promise<LostFoundItem> {
  return http.put<LostFoundItem>(`/api/lost-found/${id}/status`, { status })
}
