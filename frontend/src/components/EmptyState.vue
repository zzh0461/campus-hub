<script setup lang="ts">
import { NIcon, NButton } from 'naive-ui'
import { PhArchive } from '@phosphor-icons/vue'

withDefaults(
  defineProps<{
    title?: string
    description?: string
    actionText?: string
    compact?: boolean
  }>(),
  {
    title: '这里还空空如也',
    description: '换个关键词试试,或者成为第一个发布的人。',
    actionText: '',
    compact: false,
  },
)

const emit = defineEmits<{ (e: 'action'): void }>()
</script>

<template>
  <div class="empty-state" :class="{ 'empty-state--compact': compact }">
    <div class="empty-state__icon">
      <n-icon :size="compact ? 26 : 34" :component="PhArchive" />
    </div>
    <p class="empty-state__title">{{ title }}</p>
    <p class="empty-state__desc">{{ description }}</p>
    <n-button v-if="actionText" type="primary" ghost size="small" @click="emit('action')">
      {{ actionText }}
    </n-button>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 48px 20px;
  text-align: center;

  &--compact {
    padding: 28px 16px;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    margin-bottom: 6px;
    border-radius: 18px;
    color: $color-text-tertiary;
    background: $color-surface-soft;
    border: 1px solid $color-border;
  }

  &__title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: $color-text;
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    color: $color-text-secondary;
    max-width: 38ch;
  }
}
</style>
