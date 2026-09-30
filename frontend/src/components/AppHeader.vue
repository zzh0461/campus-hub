<script setup lang="ts">
import { computed, h, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NAvatar,
  NBadge,
  NButton,
  NDrawer,
  NDrawerContent,
  NDropdown,
  NIcon,
  type DropdownOption,
} from 'naive-ui'
import {
  PhCaretDown,
  PhHeart,
  PhSignOut,
  PhList,
  PhBell,
  PhUserCircle,
  PhMagnifyingGlass,
  PhGearSix,
  PhStorefront,
  PhTicket,
} from '@phosphor-icons/vue'
import { getUnreadCount } from '@/api/notification'
import { useAuthStore } from '@/stores/auth'
import { emitToast, onNotificationsChanged } from '@/utils/events'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const unreadCount = ref(0)
const mobileOpen = ref(false)
// 头像图片加载失败时退回首字符(与 UserAvatar 同款互斥渲染)
const avatarFailed = ref(false)
let timer: ReturnType<typeof setInterval> | undefined
let offNotificationsChanged: (() => void) | undefined
// ... existing code ...
onMounted(() => {
  void refreshUnread()
  timer = setInterval(() => {
    void refreshUnread()
  }, 60000)
  // 通知页发生已读/删除时实时刷新角标，不再干等路由切换或 60 秒轮询
  offNotificationsChanged = onNotificationsChanged(() => {
    void refreshUnread()
  })
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  offNotificationsChanged?.()
})

const navItems = [
  { label: '首页', to: '/' },
  { label: '二手市场', to: '/market' },
  { label: '校园活动', to: '/activities' },
  { label: '失物招领', to: '/lost-found' },
  { label: '校园公告', to: '/announcements' },
]

const isActive = (to: string): boolean => {
  if (to === '/') return route.path === '/'
  return route.path.startsWith(to)
}

const userMenuOptions = computed<DropdownOption[]>(() => {
  const options: DropdownOption[] = [
    { label: '个人中心', key: 'profile', icon: () => h(PhUserCircle) },
    { label: '我的收藏', key: 'favorites', icon: () => h(PhHeart) },
    { label: '我的商品', key: 'my-products', icon: () => h(PhStorefront) },
    { label: '我的活动', key: 'my-activities', icon: () => h(PhTicket) },
    { label: '我的失物招领', key: 'my-lost-found', icon: () => h(PhMagnifyingGlass) },
    { label: '通知中心', key: 'notifications', icon: () => h(PhBell) },
  ]
  if (authStore.isAdmin) {
    options.push({ label: '管理后台', key: 'admin', icon: () => h(PhGearSix) })
  }
  options.push({
    label: '退出登录',
    key: 'logout',
    icon: () => h(PhSignOut),
  })
  return options
})

async function refreshUnread(): Promise<void> {
  if (!authStore.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    const result = await getUnreadCount()
    unreadCount.value = result.count
  } catch {
    unreadCount.value = 0
  }
}

function handleUserMenu(key: string): void {
  const paths: Record<string, string> = {
    profile: '/profile',
    favorites: '/profile/favorites',
    'my-products': '/market/my',
    'my-activities': '/activities/my',
    'my-lost-found': '/lost-found/my',
    notifications: '/notifications',
    admin: '/admin',
  }
  if (key === 'logout') {
    authStore.logout()
    emitToast('success', '已退出登录')
    void router.push('/')
    return
  }
  const path = paths[key]
  if (path) void router.push(path)
}

function goTo(path: string): void {
  mobileOpen.value = false
  void router.push(path)
}

watch(
  () => authStore.isLoggedIn,
  () => {
    void refreshUnread()
  },
)

watch(
  () => route.path,
  () => {
    mobileOpen.value = false
    void refreshUnread()
  },
)
</script>

<template>
  <header class="app-header">
    <div class="container app-header__inner">
      <router-link to="/" class="app-header__brand">
          <span class="app-header__logo" aria-hidden="true">
            <brand-mark :size="24" />
          </span>
        <span class="app-header__name">CampusHub</span>
      </router-link>

      <nav class="app-header__nav" aria-label="主导航">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="app-header__link"
          :class="{ 'app-header__link--active': isActive(item.to) }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <div class="app-header__actions">
        <button
          class="app-header__icon-btn pressable"
          type="button"
          aria-label="搜索商品"
          @click="goTo('/market')"
        >
          <n-icon :size="19" :component="PhMagnifyingGlass" />
        </button>

        <n-badge :value="unreadCount" :max="99" :show="unreadCount > 0" :offset="[-4, 4]">
          <button
            class="app-header__icon-btn pressable"
            type="button"
            aria-label="通知中心"
            @click="goTo('/notifications')"
          >
            <n-icon :size="19" :component="PhBell" />
          </button>
        </n-badge>

        <n-dropdown
          v-if="authStore.isLoggedIn"
          trigger="click"
          :options="userMenuOptions"
          @select="handleUserMenu"
        >
          <button class="app-header__user pressable" type="button">
            <!-- n-avatar 默认插槽优先于 src,二者互斥渲染(见 UserAvatar 同款说明) -->
            <n-avatar
              v-if="authStore.avatar && !avatarFailed"
              round
              :size="32"
              :src="authStore.avatar"
              @error="avatarFailed = true"
            />
            <n-avatar v-else round :size="32" class="app-header__avatar-fallback">
              {{ authStore.displayName.slice(0, 1) }}
            </n-avatar>
            <span class="app-header__username">{{ authStore.displayName }}</span>
            <n-icon :size="13" :component="PhCaretDown" class="text-tertiary" />
          </button>
        </n-dropdown>

        <template v-else>
          <n-button text @click="goTo('/login')">登录</n-button>
          <n-button type="primary" size="small" @click="goTo('/register')">注册</n-button>
        </template>

        <button
          class="app-header__burger pressable"
          type="button"
          aria-label="打开菜单"
          @click="mobileOpen = true"
        >
          <n-icon :size="22" :component="PhList" />
        </button>
      </div>
    </div>

    <n-drawer v-model:show="mobileOpen" placement="left" :width="280">
      <n-drawer-content>
        <div class="app-header__mobile">
          <router-link to="/" class="app-header__brand app-header__brand--mobile" @click="mobileOpen = false">
              <span class="app-header__logo" aria-hidden="true">
                <brand-mark :size="24" />
              </span>
            <span class="app-header__name">CampusHub</span>
          </router-link>
          <nav class="app-header__mobile-nav">
            <button
              v-for="item in navItems"
              :key="item.to"
              type="button"
              class="app-header__mobile-link"
              :class="{ 'app-header__mobile-link--active': isActive(item.to) }"
              @click="goTo(item.to)"
            >
              {{ item.label }}
            </button>
          </nav>
          <div class="app-header__mobile-actions">
            <template v-if="authStore.isLoggedIn">
              <n-button block @click="goTo('/profile')">个人中心</n-button>
              <n-button block type="primary" ghost @click="goTo('/market/publish')">发布商品</n-button>
            </template>
            <template v-else>
              <n-button block @click="goTo('/login')">登录</n-button>
              <n-button block type="primary" @click="goTo('/register')">注册</n-button>
            </template>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </header>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid $color-border;

  &__inner {
    display: flex;
    align-items: center;
    gap: 28px;
    height: $header-height;
  }

  &__brand {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;

    &--mobile {
      margin-bottom: 24px;
    }
  }

  &__logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 12px;
    // 品牌图腾摆在校园绿软底方块里,与白底导航形成清爽对比
    background: $color-accent-soft;
    border: 1px solid rgba(22, 163, 74, 0.22);
    box-shadow: 0 6px 16px -9px rgba(22, 163, 74, 0.5);
  }

  &__name {
    font-size: 18px;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__nav {
    display: flex;
    align-items: center;
    gap: 4px;
    flex: 1;
  }

  &__link {
    position: relative;
    padding: 8px 12px;
    font-size: 14px;
    color: $color-text-secondary;
    border-radius: 8px;
    transition: $transition-fast;
    white-space: nowrap;

    &:hover {
      color: $color-text;
      background: $color-surface-soft;
    }

    &--active {
      color: $color-accent;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        left: 12px;
        right: 12px;
        bottom: 2px;
        height: 2px;
        border-radius: 2px;
        background: $color-accent;
      }
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-shrink: 0;
  }

  &__icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border: none;
    border-radius: 10px;
    color: $color-text-secondary;
    background: transparent;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
      background: $color-accent-soft;
    }
  }

  &__user {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 10px 4px 4px;
    border: 1px solid $color-border;
    border-radius: 999px;
    background: $color-surface;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      border-color: rgba(22, 163, 74, 0.4);
    }
  }

  &__avatar-fallback {
    background: $color-accent-soft;
    color: $color-accent;
    font-weight: 600;
    border: 1px solid rgba(22, 163, 74, 0.22);
  }

  &__username {
    max-width: 88px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    font-weight: 500;
    color: $color-text;
  }

  &__burger {
    display: none;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border: 1px solid $color-border;
    border-radius: 10px;
    background: $color-surface;
    color: $color-text;
    cursor: pointer;
  }

  &__mobile {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__mobile-nav {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 20px;
  }

  &__mobile-link {
    padding: 12px 14px;
    text-align: left;
    font-size: 15px;
    color: $color-text;
    background: transparent;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    transition: $transition-fast;

    &:hover,
    &--active {
      color: $color-accent;
      background: $color-accent-soft;
    }
  }

  &__mobile-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  @media (max-width: 768px) {
    &__nav,
    &__username {
      display: none;
    }

    &__burger {
      display: flex;
    }
  }
}
</style>
