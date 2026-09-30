<script setup lang="ts">
import { NIcon, NProgress } from 'naive-ui'
import { PhCalendarBlank, PhMapPin } from '@phosphor-icons/vue'
import type { Activity } from '@/types/activity'
import { formatDateTime } from '@/utils/format'
import StatusTag from './StatusTag.vue'

defineProps<{
  activity: Activity
}>()
</script>

<template>
  <router-link :to="`/activities/${activity.id}`" class="activity-card lift">
    <div class="activity-card__cover">
      <img :src="activity.cover" :alt="activity.title" loading="lazy" />
      <div class="activity-card__tag">
        <status-tag :value="activity.status" />
      </div>
    </div>
    <div class="activity-card__body">
      <span class="activity-card__category text-tertiary">{{ activity.categoryName }}</span>
      <h3 class="activity-card__title">{{ activity.title }}</h3>
      <div class="activity-card__info">
        <span class="activity-card__info-item">
          <n-icon :size="14" :component="PhCalendarBlank" />
          {{ formatDateTime(activity.startTime) }}
        </span>
        <span class="activity-card__info-item">
          <n-icon :size="14" :component="PhMapPin" />
          {{ activity.location }}
        </span>
      </div>
      <div class="activity-card__seats">
        <n-progress
          type="line"
          :percentage="Math.min(100, Math.round((activity.currentParticipants / activity.maxParticipants) * 100))"
          :show-indicator="false"
          :height="4"
          color="#16a34a"
          rail-color="#e8eaee"
        />
        <span class="num text-tertiary">
          {{ activity.currentParticipants }} / {{ activity.maxParticipants }} 人
        </span>
      </div>
    </div>
  </router-link>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.activity-card {
  display: flex;
  flex-direction: column;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;

  &__cover {
    position: relative;
    aspect-ratio: 16 / 8;
    overflow: hidden;
    background: $color-surface-soft;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    }
  }

  &:hover &__cover img {
    transform: scale(1.04);
  }

  &__tag {
    position: absolute;
    top: 10px;
    left: 10px;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 16px;
  }

  &__category {
    font-size: 12px;
  }

  &__title {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
    color: $color-text;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 13px;
    color: $color-text-secondary;
  }

  &__info-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }

  &__seats {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 6px;

    :deep(.n-progress) {
      flex: 1;
    }

    span {
      font-size: 12px;
      white-space: nowrap;
    }
  }
}
</style>
