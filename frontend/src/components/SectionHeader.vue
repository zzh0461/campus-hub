<script setup lang="ts">
import { PhArrowRight } from '@phosphor-icons/vue'
import { NIcon } from 'naive-ui'

defineProps<{
  title: string
  subtitle?: string
  moreText?: string
}>()

const emit = defineEmits<{ (e: 'more'): void }>()
</script>

<template>
  <div class="section-header">
    <div class="section-header__text">
      <h2 class="section-header__title">
        <span class="section-header__bar" aria-hidden="true" />
        {{ title }}
      </h2>
      <p v-if="subtitle" class="section-header__subtitle">{{ subtitle }}</p>
    </div>
    <button v-if="moreText" class="section-header__more pressable" type="button" @click="emit('more')">
      {{ moreText }}
      <n-icon :size="14" :component="PhArrowRight" />
    </button>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 22px;

  &__title {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__bar {
    width: 4px;
    height: 18px;
    border-radius: 2px;
    background: $color-accent;
  }

  &__subtitle {
    margin: 6px 0 0 14px;
    font-size: 13px;
    color: $color-text-secondary;
  }

  &__more {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 10px;
    border: none;
    background: transparent;
    font-size: 13px;
    color: $color-text-secondary;
    cursor: pointer;
    border-radius: 8px;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
      background: $color-accent-soft;
    }
  }
}
</style>
