<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NIcon,
  NPopconfirm,
  useMessage,
} from 'naive-ui'
import {
  PhArrowLeft,
  PhChatCircleDots,
  PhClock,
  PhEye,
  PhHeart,
  PhPencilSimple,
} from '@phosphor-icons/vue'
import {
  favoriteProduct,
  getProduct,
  unfavoriteProduct,
  updateProduct,
} from '@/api/market'
import { getUserPublicInfo } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { ContactVisibility } from '@/types/common'
import type { Product } from '@/types/market'
import type { UserPublicInfo } from '@/types/user'
import { formatDateTime, formatPrice, formatRelative } from '@/utils/format'
import { emitToast } from '@/utils/events'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref(false)
const product = ref<Product | null>(null)
const activeImage = ref(0)
const favoriting = ref(false)
const togglingStatus = ref(false)

/** 卖家介绍 / 联系方式弹窗显隐 */
const sellerModalVisible = ref(false)
/** 卖家公开信息加载态:打开弹窗后按需拉取,避免详情页首屏多打一次接口 */
const sellerLoading = ref(false)
/** 卖家公开信息:含昵称/头像/简介/注册时间 + 按可见性裁剪后的联系方式 */
const sellerInfo = ref<UserPublicInfo | null>(null)

const isOwner = computed(() => authStore.user?.id === product.value?.sellerId)

/**
 * 联系方式可见性(后端判定,前端只负责渲染):
 * - 未登录 → anonymous(引导登录)
 * - 已登录 → granted(完整明文展示)
 */
const contactVisibility = computed<ContactVisibility>(() =>
  authStore.isLoggedIn ? 'granted' : 'anonymous',
)

/** 弹窗里展示的联系方式列表:未登录或手机号为空时不传,交给弹窗显示对应提示 */
const sellerContacts = computed(() => {
  if (contactVisibility.value !== 'granted') return []
  const phone = sellerInfo.value?.phone
  return phone ? [{ label: '手机号', value: phone, copyTip: '手机号已复制' }] : []
})

const sellerSubtitle = computed(() => {
  if (!sellerInfo.value) return ''
  const parts: string[] = []
  if (sellerInfo.value.bio) parts.push(sellerInfo.value.bio)
  if (sellerInfo.value.createdAt) {
    parts.push(`${formatDateTime(sellerInfo.value.createdAt)} 加入平台`)
  }
  return parts.join(' · ')
})

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    product.value = await getProduct(Number(route.params.id))
    activeImage.value = 0
    // 换了商品,之前的卖家名片缓存作废(否则会显示上一个商品的卖家联系方式)
    sellerInfo.value = null
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

/**
 * 拉取卖家公开信息。
 *
 * 联系方式只做登录门控(注册时手机号必填):已登录就能拿到完整号码,未登录拿到空串 + 引导文案。
 *
 * @param force 忽略缓存强制刷新
 */
async function fetchSellerInfo(force = false): Promise<void> {
  if (!product.value) return
  // 已有同一卖家的缓存则跳过,减少重复请求
  if (!force && sellerInfo.value?.id === product.value.sellerId) return

  sellerLoading.value = true
  try {
    sellerInfo.value = await getUserPublicInfo(product.value.sellerId)
  } catch {
    // 拉取失败时回退到商品卡片上已有的卖家名/头像,保证弹窗仍可展示;
    // 联系方式置空 + 明确文案,不要伪装成"卖家没填"
    sellerInfo.value = {
      id: product.value.sellerId,
      nickname: product.value.sellerName,
      avatar: product.value.sellerAvatar,
      bio: '',
      createdAt: '',
      phone: '',
      phoneVisible: false,
      contactHint: '卖家信息加载失败,请稍后重试',
    }
  } finally {
    sellerLoading.value = false
  }
}

/** 点击「卖家信息」:弹出卖家介绍 / 联系方式弹窗 */
async function openSellerModal(): Promise<void> {
  if (!product.value) return
  sellerModalVisible.value = true
  await fetchSellerInfo()
}

async function toggleFavorite(): Promise<void> {
  if (!authStore.isLoggedIn) {
    emitToast('warning', '请先登录')
    void router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!product.value || favoriting.value) return
  favoriting.value = true
  try {
    if (product.value.favorite) {
      await unfavoriteProduct(product.value.id)
      product.value.favorite = false
      product.value.favoriteCount = Math.max(0, product.value.favoriteCount - 1)
      message.success('已取消收藏')
    } else {
      await favoriteProduct(product.value.id)
      product.value.favorite = true
      product.value.favoriteCount += 1
      message.success('已收藏,可在「我的收藏」查看')
    }
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    favoriting.value = false
  }
}

async function toggleStatus(): Promise<void> {
  if (!product.value || togglingStatus.value) return
  togglingStatus.value = true
  try {
    const nextStatus = product.value.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE'
    const updated = await updateProduct(product.value.id, {
      title: product.value.title,
      categoryId: product.value.categoryId,
      price: product.value.price,
      description: product.value.description,
      images: product.value.images,
      status: nextStatus,
    })
    product.value = updated
    message.success(nextStatus === 'ON_SALE' ? '商品已重新上架' : '商品已下架')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    togglingStatus.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <page-container max-width="1080">
    <n-button text class="detail-back" @click="router.back()">
      <template #icon>
        <n-icon :component="PhArrowLeft" />
      </template>
      返回
    </n-button>

    <div v-if="loading" class="product-detail-loading">
      <div class="skeleton-block" style="height: 420px" />
      <div class="product-detail-loading__side">
        <div class="skeleton-block" style="height: 28px; width: 70%" />
        <div class="skeleton-block" style="height: 40px; width: 40%" />
        <div class="skeleton-block" style="height: 160px" />
      </div>
    </div>

    <div v-else-if="error" class="product-detail-error">
      <empty-state
        title="商品加载失败"
        description="该商品可能已下架或删除,去看看其他好物吧"
        action-text="回到市场"
        @action="router.push('/market')"
      />
    </div>

    <div v-else-if="product" class="product-detail">
      <div class="product-detail__gallery">
        <div class="product-detail__main-img">
          <img
            :src="product.images[activeImage] || product.images[0]"
            :alt="product.title"
          />
          <div class="product-detail__status">
            <status-tag :value="product.status" />
          </div>
        </div>
        <div v-if="product.images.length > 1" class="product-detail__thumbs">
          <button
            v-for="(image, index) in product.images"
            :key="image"
            type="button"
            class="product-detail__thumb pressable"
            :class="{ 'product-detail__thumb--active': activeImage === index }"
            @click="activeImage = index"
          >
            <img :src="image" :alt="`商品图片 ${index + 1}`" loading="lazy" />
          </button>
        </div>
      </div>

      <div class="product-detail__info">
        <div class="product-detail__category">
          {{ product.categoryName }}
        </div>
        <h1 class="product-detail__title">{{ product.title }}</h1>
        <div class="product-detail__price-row">
          <span class="product-detail__price num">{{ formatPrice(product.price) }}</span>
          <span v-if="product.originalPrice > product.price" class="product-detail__origin num">
            {{ formatPrice(product.originalPrice) }}
          </span>
          <span class="product-detail__discount" v-if="product.originalPrice > product.price">
            省 {{ formatPrice(product.originalPrice - product.price) }}
          </span>
        </div>

        <div class="product-detail__stats">
          <span class="product-detail__stat">
            <n-icon :size="14" :component="PhEye" />
            <span class="num">{{ product.viewCount }} 次浏览</span>
          </span>
          <span class="product-detail__stat">
            <n-icon :size="14" :component="PhHeart" />
            <span class="num">{{ product.favoriteCount }} 人收藏</span>
          </span>
          <span class="product-detail__stat">
            <n-icon :size="14" :component="PhClock" />
            {{ formatRelative(product.createdAt) }}发布
          </span>
        </div>

        <div class="product-detail__actions">
          <n-button
            :type="product.favorite ? 'default' : 'primary'"
            :loading="favoriting"
            @click="toggleFavorite"
          >
            <template #icon>
              <n-icon :size="18">
                <PhHeart :weight="product.favorite ? 'fill' : 'regular'" />
              </n-icon>
            </template>
            {{ product.favorite ? '已收藏' : '收藏' }}
          </n-button>
          <n-button v-if="isOwner" @click="router.push(`/market/edit/${product.id}`)">
            <template #icon>
              <n-icon :component="PhPencilSimple" />
            </template>
            编辑商品
          </n-button>
          <n-popconfirm
            v-if="isOwner && product.status !== 'SOLD'"
            @positive-click="toggleStatus"
          >
            <template #trigger>
              <n-button :loading="togglingStatus">
                {{ product.status === 'ON_SALE' ? '下架商品' : '重新上架' }}
              </n-button>
            </template>
            {{ product.status === 'ON_SALE' ? '确认下架该商品?下架后其他同学将无法看到。' : '确认重新上架该商品?' }}
          </n-popconfirm>
        </div>

        <section class="product-detail__section">
          <h2 class="product-detail__section-title">商品描述</h2>
          <p class="rich-text product-detail__desc">{{ product.description }}</p>
        </section>

        <section class="product-detail__section">
          <h2 class="product-detail__section-title">卖家信息</h2>
          <button
            type="button"
            class="product-detail__seller pressable"
            @click="openSellerModal"
          >
            <user-avatar
              :avatar="product.sellerAvatar"
              :name="product.sellerName"
              :size="44"
            />
            <div class="product-detail__seller-info">
              <strong>{{ product.sellerName }}</strong>
              <span class="text-tertiary">
                发布于 {{ formatDateTime(product.createdAt) }}
              </span>
            </div>
            <span class="product-detail__seller-more">
              查看联系方式
              <n-icon :size="14" :component="PhChatCircleDots" />
            </span>
          </button>
          <p class="product-detail__hint text-tertiary">
            交易前请当面验货,谨防私下转账。遇到问题可联系平台管理员。
          </p>
        </section>
      </div>
    </div>

    <contact-dialog
      v-model:show="sellerModalVisible"
      :name="sellerInfo?.nickname || product?.sellerName || ''"
      :avatar="sellerInfo?.avatar || product?.sellerAvatar || ''"
      :subtitle="sellerSubtitle"
      :contacts="sellerContacts"
      :visibility="contactVisibility"
      :loading="sellerLoading"
      :gate-hint="sellerInfo?.contactHint || '登录后即可查看联系方式'"
      tip="交易请当面验货,谨防私下转账。"
    />
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.detail-back {
  margin-bottom: 18px;
}

.product-detail-loading {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 32px;

  &__side {
    display: flex;
    flex-direction: column;
    gap: 18px;
  }

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.product-detail-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.product-detail {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.85fr);
  gap: 36px;

  &__main-img {
    position: relative;
    aspect-ratio: 4 / 3;
    border-radius: $radius-lg;
    overflow: hidden;
    border: 1px solid $color-border;
    background: $color-surface;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__status {
    position: absolute;
    top: 14px;
    left: 14px;
  }

  &__thumbs {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    margin-top: 12px;
  }

  &__thumb {
    aspect-ratio: 1;
    border-radius: $radius-sm;
    overflow: hidden;
    border: 2px solid transparent;
    padding: 0;
    background: none;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &--active {
      border-color: $color-accent;
    }
  }

  &__category {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 7px;
    font-size: 12px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__title {
    margin: 12px 0;
    font-size: 24px;
    line-height: 1.4;
    font-weight: 750;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__price-row {
    display: flex;
    align-items: baseline;
    gap: 12px;
    padding: 14px 0;
    border-top: 1px solid $color-border;
    border-bottom: 1px solid $color-border;
  }

  &__price {
    font-size: 28px;
    font-weight: 750;
    color: $color-danger;
  }

  &__origin {
    font-size: 14px;
    color: $color-text-tertiary;
    text-decoration: line-through;
  }

  &__discount {
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 6px;
    color: $color-danger;
    background: $color-danger-soft;
  }

  &__stats {
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    margin: 16px 0 20px;
  }

  &__stat {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: $color-text-secondary;
  }

  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  &__section {
    margin-top: 26px;
  }

  &__section-title {
    margin: 0 0 10px;
    font-size: 15px;
    font-weight: 650;
    color: $color-text;
  }

  &__desc {
    margin: 0;
    font-size: 14px;
  }

  &__seller {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
    padding: 12px;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    background: $color-surface;
    transition: $transition-fast;
    cursor: pointer;
    text-align: left;

    &:hover {
      border-color: rgba(22, 163, 74, 0.4);
    }
  }

  &__seller-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;

    strong {
      font-size: 14px;
      color: $color-text;
    }

    span {
      font-size: 12px;
    }
  }

  &__seller-more {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 600;
    color: $color-accent;
    white-space: nowrap;
  }

  &__hint {
    margin: 10px 2px 0;
    font-size: 12px;
    line-height: 1.7;
  }

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}
</style>
