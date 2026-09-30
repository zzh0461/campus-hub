<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NAvatar,
  NButton,
  NIcon,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  type MenuOption,
} from 'naive-ui'
import {
  PhTote,
  PhCalendarBlank,
  PhLifebuoy,
  PhSignOut,
  PhEnvelopeSimpleOpen,
  PhMegaphone,
  PhUsers,
  PhChartLineUp,
} from '@phosphor-icons/vue'
import { useAuthStore } from '@/stores/auth'
import { emitToast } from '@/utils/events'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const collapsed = ref(false)
// 头像图片加载失败时退回首字符
const avatarFailed = ref(false)

const menuOptions: MenuOption[] = [
  {
    label: '数据概览',
    key: '/admin',
    icon: () => h(NIcon, null, { default: () => h(PhChartLineUp) }),
  },
  {
    label: '用户管理',
    key: '/admin/users',
    icon: () => h(NIcon, null, { default: () => h(PhUsers) }),
  },
  {
    label: '公告管理',
    key: '/admin/announcements',
    icon: () => h(NIcon, null, { default: () => h(PhMegaphone) }),
  },
  {
    label: '商品管理',
    key: '/admin/products',
    icon: () => h(NIcon, null, { default: () => h(PhTote) }),
  },
  {
    label: '活动管理',
    key: '/admin/activities',
    icon: () => h(NIcon, null, { default: () => h(PhCalendarBlank) }),
  },
  {
    label: '失物管理',
    key: '/admin/lost-found',
    icon: () => h(NIcon, null, { default: () => h(PhLifebuoy) }),
  },
  {
    label: '通知管理',
    key: '/admin/notifications',
    icon: () => h(NIcon, null, { default: () => h(PhEnvelopeSimpleOpen) }),
  },
]

const activeKey = computed(() => {
  if (route.path === '/admin') return '/admin'
  return route.path
})

const pageTitle = computed(() => route.meta.title ?? '管理后台')

function handleMenu(key: string): void {
  void router.push(key)
}

function handleLogout(): void {
  authStore.logout()
  emitToast('success', '已退出登录')
  void router.push('/login')
}
</script>

<template>
  <n-layout has-sider class="admin-layout">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="224"
      v-model:collapsed="collapsed"
      :show-trigger="true"
      class="admin-layout__sider"
    >
      <router-link to="/" class="admin-layout__brand">
        <span class="admin-layout__logo" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2 3 7v10l9 5 9-5V7l-9-5Z" stroke-linejoin="round" />
            <path d="M3 7l9 5 9-5M12 12v10" stroke-linejoin="round" />
          </svg>
        </span>
        <span v-if="!collapsed" class="admin-layout__brand-text">CampusHub Admin</span>
      </router-link>
      <n-menu
        :value="activeKey"
        :options="menuOptions"
        :collapsed="collapsed"
        :collapsed-width="64"
        @update:value="handleMenu"
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered class="admin-layout__header">
        <div class="admin-layout__header-left">
          <h1 class="admin-layout__title">{{ pageTitle }}</h1>
        </div>
        <div class="admin-layout__header-right">
          <n-button text @click="router.push('/')">返回前台</n-button>
          <div class="admin-layout__admin">
            <!-- n-avatar 默认插槽优先于 src,二者互斥渲染(见 UserAvatar 同款说明) -->
            <n-avatar
              v-if="authStore.avatar && !avatarFailed"
              round
              :size="30"
              :src="authStore.avatar"
              @error="avatarFailed = true"
            />
            <n-avatar v-else round :size="30" class="admin-layout__avatar-fallback">
              {{ authStore.displayName.slice(0, 1) }}
            </n-avatar>
            <span>{{ authStore.displayName }}</span>
          </div>
          <n-button quaternary circle title="退出登录" @click="handleLogout">
            <template #icon>
              <n-icon :component="PhSignOut" />
            </template>
          </n-button>
        </div>
      </n-layout-header>

      <n-layout-content class="admin-layout__content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-layout {
  min-height: 100dvh;

  &__sider {
    background: $color-surface;

    :deep(.n-layout-sider-scroll-container) {
      display: flex;
      flex-direction: column;
    }
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 18px 20px;
    border-bottom: 1px solid $color-border;
  }

  &__logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    flex-shrink: 0;
    border-radius: 10px;
    color: #fff;
    background: $color-accent;
  }

  &__brand-text {
    font-size: 15px;
    font-weight: 700;
    color: $color-text;
    white-space: nowrap;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    height: 60px;
    padding: 0 24px;
    background: $color-surface;
  }

  &__title {
    margin: 0;
    font-size: 16px;
    font-weight: 650;
    color: $color-text;
  }

  &__header-right {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__admin {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: $color-text;
  }

  &__avatar-fallback {
    background: $color-accent-soft;
    color: $color-accent;
    font-weight: 600;
    border: 1px solid rgba(22, 163, 74, 0.22);
  }

  &__content {
    padding: 28px;
    background: $color-bg;
  }

  @media (max-width: 768px) {
    &__content {
      padding: 16px;
    }

    &__admin span {
      display: none;
    }
  }
}
</style>
