<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NButton, NIcon } from 'naive-ui'
import { PhHouseSimple } from '@phosphor-icons/vue'

const router = useRouter()
</script>

<template>
  <div class="error-page">
    <div class="error-page__code num">404</div>
    <h1 class="error-page__title">页面走丢了</h1>
    <p class="error-page__desc">你访问的页面不存在或已被移动,回到首页继续探索吧。</p>
    <n-button type="primary" @click="router.push('/')">
      <template #icon>
        <n-icon :component="PhHouseSimple" />
      </template>
      回到首页
    </n-button>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.error-page {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  text-align: center;
  background:
    radial-gradient(ellipse 60% 50% at 50% 20%, rgba(22, 163, 74, 0.08), transparent 70%),
    $color-bg;

  &__code {
    font-size: 88px;
    font-weight: 800;
    letter-spacing: -0.03em;
    color: $color-accent;
    line-height: 1;
  }

  &__title {
    margin: 0;
    font-size: 22px;
    color: $color-text;
  }

  &__desc {
    margin: 0 0 10px;
    font-size: 14px;
    color: $color-text-secondary;
  }
}
</style>
