import type { PageParams } from './common'

export type ActivityStatus = 'UPCOMING' | 'ONGOING' | 'FINISHED'

export interface ActivityCategory {
  id: number
  name: string
}

export interface Activity {
  id: number
  title: string
  description: string
  categoryId: number
  categoryName: string
  location: string
  cover: string
  startTime: string
  endTime: string
  maxParticipants: number
  currentParticipants: number
  remainingParticipants: number
  registered: boolean
  organizer: string
  organizerId: number
  status: ActivityStatus
  createdAt: string
}

export interface ActivityQuery extends PageParams {
  keyword?: string
  categoryId?: number
  status?: ActivityStatus | ''
}

export interface ActivityPublishParams {
  title: string
  description: string
  categoryId: number
  location: string
  startTime: string
  endTime: string
  maxParticipants: number
  cover: string
  /** 主办方：后台可指定；普通用户发布时不传，后端自动取发布者昵称 */
  organizer?: string
}
