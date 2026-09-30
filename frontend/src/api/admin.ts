import { http } from '@/utils/request'
import type {
  Activity,
  ActivityPublishParams,
} from '@/types/activity'
import type {
  Announcement,
  AnnouncementPublishParams,
} from '@/types/announcement'
import type { PageParams, PageResult } from '@/types/common'
import type {
  LostFoundItem,
  LostFoundStatus,
} from '@/types/lostFound'
import type { Product, ProductStatus } from '@/types/market'
import type { User, UserStatus } from '@/types/user'

// ---------- 用户管理 ----------

export interface AdminUserQuery extends PageParams {
  keyword?: string
}

export function getAdminUsers(params: AdminUserQuery): Promise<PageResult<User>> {
  return http.get<PageResult<User>>('/api/admin/users', { params })
}

export function updateUserStatus(id: number, status: UserStatus): Promise<User> {
  return http.put<User>(`/api/admin/users/${id}/status`, { status })
}

export function deleteUser(id: number): Promise<null> {
  return http.delete<null>(`/api/admin/users/${id}`)
}

// ---------- 公告管理 ----------

export function getAdminAnnouncements(params: PageParams & { keyword?: string }): Promise<PageResult<Announcement>> {
  return http.get<PageResult<Announcement>>('/api/admin/announcements', { params })
}

export function createAnnouncement(params: AnnouncementPublishParams): Promise<Announcement> {
  return http.post<Announcement>('/api/admin/announcements', params)
}

export function updateAnnouncement(id: number, params: AnnouncementPublishParams): Promise<Announcement> {
  return http.put<Announcement>(`/api/admin/announcements/${id}`, params)
}

export function deleteAnnouncement(id: number): Promise<null> {
  return http.delete<null>(`/api/admin/announcements/${id}`)
}

// ---------- 商品管理 ----------

export function getAdminProducts(params: PageParams & { keyword?: string }): Promise<PageResult<Product>> {
  return http.get<PageResult<Product>>('/api/admin/products', { params })
}

export function updateAdminProductStatus(id: number, status: ProductStatus): Promise<Product> {
  return http.put<Product>(`/api/admin/products/${id}/status`, { status })
}

export function deleteAdminProduct(id: number): Promise<null> {
  return http.delete<null>(`/api/admin/products/${id}`)
}

// ---------- 活动管理 ----------

export function getAdminActivities(params: PageParams & { keyword?: string }): Promise<PageResult<Activity>> {
  return http.get<PageResult<Activity>>('/api/admin/activities', { params })
}

export function createAdminActivity(params: ActivityPublishParams): Promise<Activity> {
  return http.post<Activity>('/api/admin/activities', params)
}

export function updateAdminActivity(id: number, params: ActivityPublishParams): Promise<Activity> {
  return http.put<Activity>(`/api/admin/activities/${id}`, params)
}

export function deleteAdminActivity(id: number): Promise<null> {
  return http.delete<null>(`/api/admin/activities/${id}`)
}

// ---------- 失物管理 ----------

export function getAdminLostFound(params: PageParams & { keyword?: string }): Promise<PageResult<LostFoundItem>> {
  return http.get<PageResult<LostFoundItem>>('/api/admin/lost-found', { params })
}

export function updateLostFoundStatus(id: number, status: LostFoundStatus): Promise<LostFoundItem> {
  return http.put<LostFoundItem>(`/api/admin/lost-found/${id}/status`, { status })
}

export function deleteAdminLostFound(id: number): Promise<null> {
  return http.delete<null>(`/api/admin/lost-found/${id}`)
}
