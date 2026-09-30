<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { isLoadingVisible } from '@/utils/loading'
import type { ToastPayload } from '@/utils/events'

const message = useMessage()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

function handleToast(event: Event): void {
  const { type, content } = (event as CustomEvent<ToastPayload>).detail
  message[type](content)
}

function handleUnauthorized(): void {
  authStore.logout()
  userStore.reset()
  if (router.currentRoute.value.path !== '/login') {
    void router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath },
    })
  }
}

onMounted(() => {
  window.addEventListener('campushub:toast', handleToast)
  window.addEventListener('campushub:unauthorized', handleUnauthorized)
})

onBeforeUnmount(() => {
  window.removeEventListener('campushub:toast', handleToast)
  window.removeEventListener('campushub:unauthorized', handleUnauthorized)
})
</script>

<template>
  <div class="app-shell">
    <transition name="loading-fade">
      <div v-if="isLoadingVisible()" class="app-shell__loading-bar" aria-hidden="true" />
    </transition>
    <router-view />
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.app-shell {
  min-height: 100dvh;
}

.app-shell__loading-bar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  width: 30%;
  height: 2px;
  border-radius: 0 2px 2px 0;
  background: linear-gradient(90deg, transparent, $color-accent, $color-accent-hover);
  animation: loading-slide 1.2s cubic-bezier(0.16, 1, 0.3, 1) infinite;
}

@keyframes loading-slide {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(420%);
  }
}

.loading-fade-enter-active,
.loading-fade-leave-active {
  transition: opacity 0.3s ease;
}

.loading-fade-enter-from,
.loading-fade-leave-to {
  opacity: 0;
}
</style>
