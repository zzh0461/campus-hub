import type { PageParams } from './common'

export type NotificationType = 'SYSTEM' | 'MARKET' | 'ACTIVITY' | 'LOST_FOUND'

export interface Notification {
  id: number
  title: string
  content: string
  type: NotificationType
  read: boolean
  createdAt: string
}

export interface NotificationQuery extends PageParams {
  read?: boolean
}
