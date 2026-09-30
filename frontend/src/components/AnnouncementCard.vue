<script setup lang="ts">
import { NIcon } from 'naive-ui'
import { PhArrowRight, PhFileText } from '@phosphor-icons/vue'
import type { Announcement } from '@/types/announcement'
import { formatRelative } from '@/utils/format'

defineProps<{
  announcement: Announcement
}>()
</script>

<template>
  <router-link :to="`/announcements/${announcement.id}`" class="announcement-card pressable">
    <div class="announcement-card__icon">
      <n-icon :size="17" :component="PhFileText" />
    </div>
    <div class="announcement-card__body">
      <div class="announcement-card__meta">
        <span class="announcement-card__category">{{ announcement.category }}</span>
        <span class="text-tertiary">{{ formatRelative(announcement.createdAt) }}</span>
      </div>
      <h3 class="announcement-card__title">{{ announcement.title }}</h3>
    </div>
    <n-icon :size="16" :component="PhArrowRight" class="announcement-card__arrow" />
  </router-link>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.announcement-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-sm;
  transition: $transition-fast;

  &:hover {
    border-color: rgba(22, 163, 74, 0.4);
    background: rgba(22, 163, 74, 0.03);

    .announcement-card__arrow {
      opacity: 1;
      transform: translateX(2px);
    }
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    flex-shrink: 0;
    border-radius: 12px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 12px;
  }

  &__category {
    padding: 1px 8px;
    border-radius: 6px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__title {
    margin: 3px 0 0;
    font-size: 14px;
    font-weight: 600;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__arrow {
    flex-shrink: 0;
    color: $color-text-tertiary;
    opacity: 0.5;
    transition: $transition-fast;
  }
}
</style>
