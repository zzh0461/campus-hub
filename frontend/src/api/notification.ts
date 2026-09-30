import { http } from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { Notification, NotificationQuery } from '@/types/notification'

/** 通知分页列表 */
export function getNotifications(params: NotificationQuery): Promise<PageResult<Notification>> {
  return http.get<PageResult<Notification>>('/api/notifications', { params })
}

/** 未读通知数量 */
export function getUnreadCount(): Promise<{ count: number }> {
  return http.get<{ count: number }>('/api/notifications/unread-count')
}

/** 标记单条已读 */
export function markNotificationRead(id: number): Promise<null> {
  return http.put<null>(`/api/notifications/${id}/read`)
}

/** 全部已读 */
export function markAllNotificationsRead(): Promise<null> {
  return http.put<null>('/api/notifications/read-all')
}

/** 删除通知 */
export function deleteNotification(id: number): Promise<null> {
  return http.delete<null>(`/api/notifications/${id}`)
}
