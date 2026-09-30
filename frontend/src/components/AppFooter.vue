<script setup lang="ts">
const links = [
  { label: '二手市场', to: '/market' },
  { label: '校园活动', to: '/activities' },
  { label: '失物招领', to: '/lost-found' },
  { label: '校园公告', to: '/announcements' },
]
</script>

<template>
  <footer class="app-footer">
    <div class="container app-footer__inner">
      <div class="app-footer__brand">
        <span class="app-footer__logo" aria-hidden="true">
          <brand-mark :size="20" />
        </span>
        <span class="app-footer__name">CampusHub 校园综合服务平台</span>
      </div>
      <p class="app-footer__desc">
        二手交易、活动报名、失物招领、校园公告,让校园信息更高效地流转。
      </p>
      <nav class="app-footer__links" aria-label="底部导航">
        <router-link v-for="link in links" :key="link.to" :to="link.to" class="app-footer__link">
          {{ link.label }}
        </router-link>
      </nav>
    </div>
  </footer>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.app-footer {
  border-top: 1px solid $color-border;
  background: $color-surface;
  padding: 44px 0 36px;

  &__inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    text-align: center;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 10px;
    background: $color-accent-soft;
    border: 1px solid rgba(22, 163, 74, 0.22);
  }

  &__name {
    font-size: 15px;
    font-weight: 650;
    color: $color-text;
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    color: $color-text-secondary;
  }

  &__links {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 4px 22px;
  }

  &__link {
    font-size: 13px;
    color: $color-text-secondary;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
    }
  }
}
</style>
