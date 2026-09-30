import type { PageParams } from './common'

export interface Announcement {
  id: number
  title: string
  content: string
  category: string
  author: string
  published: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
}

export interface AnnouncementQuery extends PageParams {
  keyword?: string
  category?: string
}

export interface AnnouncementPublishParams {
  title: string
  content: string
  category: string
  published: boolean
}
