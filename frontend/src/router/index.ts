import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from 'vue-router'
import { getToken } from '@/utils/auth'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'announcements',
        name: 'announcements',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: { title: '校园公告' },
      },
      {
        path: 'announcements/:id',
        name: 'announcement-detail',
        component: () => import('@/views/announcement/AnnouncementDetail.vue'),
        meta: { title: '公告详情' },
      },
      {
        path: 'market',
        name: 'market',
        component: () => import('@/views/market/ProductList.vue'),
        meta: { title: '二手市场' },
      },
      {
        path: 'market/products/:id',
        name: 'product-detail',
        component: () => import('@/views/market/ProductDetail.vue'),
        meta: { title: '商品详情' },
      },
      {
        path: 'market/publish',
        name: 'product-publish',
        component: () => import('@/views/market/ProductPublish.vue'),
        meta: { title: '发布商品', requiresAuth: true },
      },
      {
        path: 'market/edit/:id',
        name: 'product-edit',
        component: () => import('@/views/market/ProductEdit.vue'),
        meta: { title: '编辑商品', requiresAuth: true },
      },
      {
        path: 'market/my',
        name: 'my-products',
        component: () => import('@/views/market/MyProducts.vue'),
        meta: { title: '我的商品', requiresAuth: true },
      },
      {
        path: 'activities',
        name: 'activities',
        component: () => import('@/views/activity/ActivityList.vue'),
        meta: { title: '校园活动' },
      },
      {
        path: 'activities/:id',
        name: 'activity-detail',
        component: () => import('@/views/activity/ActivityDetail.vue'),
        meta: { title: '活动详情' },
      },
      {
        path: 'activities/my',
        name: 'my-activities',
        component: () => import('@/views/activity/MyActivities.vue'),
        meta: { title: '我的活动', requiresAuth: true },
      },
      {
        path: 'lost-found',
        name: 'lost-found',
        component: () => import('@/views/lost-found/LostFoundList.vue'),
        meta: { title: '失物招领' },
      },
      {
        path: 'lost-found/:id',
        name: 'lost-found-detail',
        component: () => import('@/views/lost-found/LostFoundDetail.vue'),
        meta: { title: '失物详情' },
      },
      {
        path: 'lost-found/publish',
        name: 'lost-found-publish',
        component: () => import('@/views/lost-found/LostFoundPublish.vue'),
        meta: { title: '发布失物/招领', requiresAuth: true },
      },
      {
        path: 'lost-found/my',
        name: 'my-lost-found',
        component: () => import('@/views/lost-found/MyLostFound.vue'),
        meta: { title: '我的失物招领', requiresAuth: true },
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('@/views/notification/NotificationList.vue'),
        meta: { title: '通知中心', requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
      {
        path: 'profile/favorites',
        name: 'my-favorites',
        component: () => import('@/views/user/MyFavorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览', requiresAdmin: true },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', requiresAdmin: true },
      },
      {
        path: 'announcements',
        name: 'admin-announcements',
        component: () => import('@/views/admin/AnnouncementManagement.vue'),
        meta: { title: '公告管理', requiresAdmin: true },
      },
      {
        path: 'products',
        name: 'admin-products',
        component: () => import('@/views/admin/ProductManagement.vue'),
        meta: { title: '商品管理', requiresAdmin: true },
      },
      {
        path: 'activities',
        name: 'admin-activities',
        component: () => import('@/views/admin/ActivityManagement.vue'),
        meta: { title: '活动管理', requiresAdmin: true },
      },
      {
        path: 'lost-found',
        name: 'admin-lost-found',
        component: () => import('@/views/admin/LostFoundManagement.vue'),
        meta: { title: '失物管理', requiresAdmin: true },
      },
      {
        path: 'notifications',
        name: 'admin-notifications',
        component: () => import('@/views/admin/NotificationManagement.vue'),
        meta: { title: '通知管理', requiresAdmin: true },
      },
    ],
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('@/views/error/Forbidden.vue'),
    meta: { title: '无权限' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.path !== from.path) return { top: 0 }
    return undefined
  },
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    if (!authStore.isLoggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    return { path: '/403' }
  }
  if ((to.name === 'login' || to.name === 'register') && getToken()) {
    return { path: '/' }
  }
  return true
})

router.afterEach((to) => {
  const title = to.meta.title
  document.title = title ? `${title} · CampusHub 校园综合服务平台` : 'CampusHub 校园综合服务平台'
})

export default router
