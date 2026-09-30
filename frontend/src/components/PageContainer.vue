<script setup lang="ts">
defineProps<{
  title?: string
  subtitle?: string
  maxWidth?: number
}>()
</script>

<template>
  <section class="page-container">
    <div class="container page-container__inner" :style="maxWidth ? { maxWidth: `${maxWidth}px` } : undefined">
      <header v-if="title || $slots.extra" class="page-container__head">
        <div>
          <h1 v-if="title" class="page-container__title">{{ title }}</h1>
          <p v-if="subtitle" class="page-container__subtitle">{{ subtitle }}</p>
        </div>
        <div v-if="$slots.extra" class="page-container__extra">
          <slot name="extra" />
        </div>
      </header>
      <slot />
    </div>
  </section>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.page-container {
  padding: 36px 0 72px;

  &__head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 24px;
    margin-bottom: 28px;
  }

  &__title {
    margin: 0;
    font-size: 26px;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__subtitle {
    margin: 6px 0 0;
    font-size: 14px;
    color: $color-text-secondary;
  }

  &__extra {
    flex-shrink: 0;
  }

  @media (max-width: 768px) {
    padding: 24px 0 48px;

    &__head {
      flex-direction: column;
      align-items: flex-start;
      gap: 14px;
    }
  }
}
</style>
