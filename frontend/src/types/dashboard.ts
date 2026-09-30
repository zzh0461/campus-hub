export interface DashboardStats {
  userTotal: number
  productTotal: number
  activityTotal: number
  lostFoundTotal: number
  todayNewUsers: number
  todayNewProducts: number
  todayRegistrations: number
}

export interface TrendPoint {
  date: string
  value: number
}

export interface LostFoundStatItem {
  type: 'LOST' | 'FOUND'
  count: number
}

export interface DashboardData {
  stats: DashboardStats
  userGrowth: TrendPoint[]
  productTrend: TrendPoint[]
  registrationTrend: TrendPoint[]
  lostFoundStats: LostFoundStatItem[]
}
