<script setup lang="ts">
withDefaults(
  defineProps<{
    rows?: number
    variant?: 'list' | 'grid' | 'panel'
  }>(),
  {
    rows: 4,
    variant: 'list',
  },
)
</script>

<template>
  <div v-if="variant === 'grid'" class="loading-grid">
    <div v-for="i in rows" :key="i" class="loading-grid__item">
      <div class="skeleton-block loading-grid__media" />
      <div class="skeleton-block loading-grid__line" style="width: 80%" />
      <div class="skeleton-block loading-grid__line" style="width: 50%" />
    </div>
  </div>
  <div v-else-if="variant === 'panel'" class="loading-panel">
    <div v-for="i in rows" :key="i" class="skeleton-block loading-panel__row" />
  </div>
  <ul v-else class="loading-list">
    <li v-for="i in rows" :key="i" class="loading-list__row">
      <div class="skeleton-block loading-list__thumb" />
      <div class="loading-list__body">
        <div class="skeleton-block" style="width: 70%; height: 14px" />
        <div class="skeleton-block" style="width: 40%; height: 12px" />
      </div>
    </li>
  </ul>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.loading-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  &__item {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 14px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-md;
  }

  &__media {
    aspect-ratio: 4 / 3;
  }

  &__line {
    height: 14px;
  }

  @media (max-width: 992px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 560px) {
    grid-template-columns: 1fr;
  }
}

.loading-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__row {
    height: 52px;
  }
}

.loading-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__row {
    display: flex;
    gap: 14px;
    align-items: center;
    padding: 12px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-sm;
  }

  &__thumb {
    width: 72px;
    height: 54px;
    flex-shrink: 0;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
  }
}
</style>
