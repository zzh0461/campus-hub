import { http } from '@/utils/request'
import type {
  Announcement,
  AnnouncementQuery,
} from '@/types/announcement'
import type { PageResult } from '@/types/common'

/** 公告分页列表 */
export function getAnnouncements(params: AnnouncementQuery): Promise<PageResult<Announcement>> {
  return http.get<PageResult<Announcement>>('/api/announcements', { params })
}

/** 公告详情 */
export function getAnnouncement(id: number): Promise<Announcement> {
  return http.get<Announcement>(`/api/announcements/${id}`)
}

/** 最新公告(首页) */
export function getLatestAnnouncements(): Promise<Announcement[]> {
  return http.get<Announcement[]>('/api/announcements/latest')
}
