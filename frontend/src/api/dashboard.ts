import { http } from '@/utils/request'
import type { DashboardData } from '@/types/dashboard'

/** 后台数据概览 */
export function getDashboard(): Promise<DashboardData> {
  return http.get<DashboardData>('/api/admin/dashboard')
}
