import type { App } from 'vue'
import type { Component } from 'vue'
import PageContainer from './PageContainer.vue'
import BrandMark from './BrandMark.vue'
import SectionHeader from './SectionHeader.vue'
import EmptyState from './EmptyState.vue'
import LoadingState from './LoadingState.vue'
import Pagination from './Pagination.vue'
import StatusTag from './StatusTag.vue'
import UserAvatar from './UserAvatar.vue'
import ImageUpload from './ImageUpload.vue'
import ProductCard from './ProductCard.vue'
import ActivityCard from './ActivityCard.vue'
import AnnouncementCard from './AnnouncementCard.vue'
import NotificationDetailModal from './NotificationDetailModal.vue'
import ContactDialog from './ContactDialog.vue'

const components: Array<[string, Component]> = [
  ['PageContainer', PageContainer],
  ['BrandMark', BrandMark],
  ['SectionHeader', SectionHeader],
  ['EmptyState', EmptyState],
  ['LoadingState', LoadingState],
  ['Pagination', Pagination],
  ['StatusTag', StatusTag],
  ['UserAvatar', UserAvatar],
  ['ImageUpload', ImageUpload],
  ['ProductCard', ProductCard],
  ['ActivityCard', ActivityCard],
  ['AnnouncementCard', AnnouncementCard],
  ['NotificationDetailModal', NotificationDetailModal],
  ['ContactDialog', ContactDialog],
]

export function registerGlobalComponents(app: App): void {
  components.forEach(([name, component]) => {
    app.component(name, component)
  })
}
