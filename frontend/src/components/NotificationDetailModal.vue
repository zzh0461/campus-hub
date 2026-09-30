<script setup lang="ts">
import { computed } from 'vue'
import { NButton, NModal } from 'naive-ui'
import type { Notification } from '@/types/notification'
import { formatDateTime } from '@/utils/format'

const props = defineProps<{
  show: boolean
  notification: Notification | null
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  /** 弹出的通知尚未读时通知父组件标记已读 */
  (e: 'read', item: Notification): void
}>()

const visible = computed({
  get: () => props.show,
  set: (value: boolean) => emit('update:show', value),
})

function handleOpen(): void {
  const item = props.notification
  if (item && !item.read) {
    emit('read', item)
  }
}

function handleClose(): void {
  visible.value = false
}
</script>

<template>
  <n-modal
    v-model:show="visible"
    preset="card"
    title="通知详情"
    style="width: 520px; max-width: 94vw"
    @after-enter="handleOpen"
  >
    <div v-if="notification" class="notice-detail">
      <div class="notice-detail__head">
        <status-tag :value="notification.type" />
        <span class="notice-detail__time text-tertiary">
          {{ formatDateTime(notification.createdAt) }}
        </span>
        <span v-if="!notification.read" class="notice-detail__unread">未读</span>
      </div>
      <h3 class="notice-detail__title">{{ notification.title }}</h3>
      <p class="notice-detail__content">{{ notification.content }}</p>
    </div>
    <template #footer>
      <div class="notice-detail__foot">
        <n-button @click="handleClose">关闭</n-button>
      </div>
    </template>
  </n-modal>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__head {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__time {
    font-size: 12px;
  }

  &__unread {
    font-size: 12px;
    color: $color-danger;
  }

  &__title {
    margin: 0;
    font-size: 17px;
    font-weight: 650;
    color: $color-text;
  }

  &__content {
    margin: 0;
    font-size: 14px;
    line-height: 1.8;
    color: $color-text-secondary;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.notice-detail__foot {
  display: flex;
  justify-content: flex-end;
}
</style>
