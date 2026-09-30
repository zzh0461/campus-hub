<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<template>
  <div class="front-layout">
    <app-header />
    <main class="front-layout__main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="($route.fullPath)" />
        </transition>
      </router-view>
    </main>
    <app-footer />
  </div>
</template>

<style scoped lang="scss">
.front-layout {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;

  &__main {
    flex: 1;
  }
}
</style>
