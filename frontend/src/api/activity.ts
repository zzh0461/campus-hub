import { http } from '@/utils/request'
import type {
  Activity,
  ActivityCategory,
  ActivityPublishParams,
  ActivityQuery,
} from '@/types/activity'
import type { PageParams, PageResult } from '@/types/common'

/** 活动分类(筛选 UI 所需的补充契约,见 README) */
export function getActivityCategories(): Promise<ActivityCategory[]> {
  return http.get<ActivityCategory[]>('/api/activities/categories')
}

/** 活动分页列表 */
export function getActivities(params: ActivityQuery): Promise<PageResult<Activity>> {
  return http.get<PageResult<Activity>>('/api/activities', { params })
}

/** 即将开始的活动(首页) */
export function getUpcomingActivities(): Promise<Activity[]> {
  return http.get<Activity[]>('/api/activities/upcoming')
}

/** 活动详情 */
export function getActivity(id: number): Promise<Activity> {
  return http.get<Activity>(`/api/activities/${id}`)
}

/** 我的活动 */
export function getMyActivities(params: PageParams): Promise<PageResult<Activity>> {
  return http.get<PageResult<Activity>>('/api/activities/my', { params })
}

/** 报名活动 */
export function registerActivity(id: number): Promise<Activity> {
  return http.post<Activity>(`/api/activities/${id}/registrations`)
}

/** 取消报名 */
export function cancelActivityRegistration(id: number): Promise<Activity> {
  return http.delete<Activity>(`/api/activities/${id}/registrations`)
}

/** 发布活动(普通用户也可办活动,主办方自动取当前用户昵称) */
export function createActivity(params: ActivityPublishParams): Promise<Activity> {
  return http.post<Activity>('/api/activities', params)
}

/** 我发布的活动 */
export function getMyPublishedActivities(params: PageParams): Promise<PageResult<Activity>> {
  return http.get<PageResult<Activity>>('/api/activities/my-published', { params })
}

/** 编辑自己发布的活动 */
export function updateActivity(id: number, params: ActivityPublishParams): Promise<Activity> {
  return http.put<Activity>(`/api/activities/${id}`, params)
}

/** 删除自己发布的活动(已有人报名时后端会拒绝) */
export function deleteActivity(id: number): Promise<null> {
  return http.delete<null>(`/api/activities/${id}`)
}
