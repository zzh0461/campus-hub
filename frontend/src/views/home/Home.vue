<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import {
  PhArrowRight,
  PhArrowUp,
  PhBasketball,
  PhBookOpen,
  PhChatCircleText,
  PhDeviceMobile,
  PhFire,
  PhHeart,
  PhHouseLine,
  PhMagnifyingGlass,
  PhMapPin,
  PhPackage,
  PhPlus,
  PhSparkle,
  PhSquaresFour,
  PhTShirt,
} from '@phosphor-icons/vue'
import { getLatestAnnouncements } from '@/api/announcement'
import { getProductCategories, getProducts, getRecommendedProducts } from '@/api/market'
import { getUpcomingActivities } from '@/api/activity'
import { getLostFoundList } from '@/api/lostFound'
import { formatDate, formatPrice } from '@/utils/format'
import heroBg from '@/assets/market-hero.jpg'
import type { Announcement } from '@/types/announcement'
import type { Product, ProductCategory } from '@/types/market'
import type { Activity } from '@/types/activity'
import type { LostFoundItem } from '@/types/lostFound'

const router = useRouter()

const announcements = ref<Announcement[]>([])
const products = ref<Product[]>([])
const latestProducts = ref<Product[]>([])
const categories = ref<ProductCategory[]>([])
const activities = ref<Activity[]>([])
const lostFound = ref<LostFoundItem[]>([])

const loadingAnnouncements = ref(true)
const loadingProducts = ref(true)
const loadingLatest = ref(true)
const loadingActivities = ref(true)
const loadingLostFound = ref(true)

const errorAnnouncements = ref(false)
const errorProducts = ref(false)
const errorLatest = ref(false)
const errorActivities = ref(false)
const errorLostFound = ref(false)

const keyword = ref('')
const hotKeywords = ['iPad', '书籍', '电脑', '运动鞋', '台灯']

const quickEntries = [
  { label: '发布闲置', desc: '快速发布 · 轻松交易', to: '/market/publish', icon: PhPlus },
  { label: '我的收藏', desc: '心仪好物 · 收藏', to: '/profile/favorites', icon: PhHeart },
  { label: '我的商品', desc: '查看商品 · 管理', to: '/market/my', icon: PhPackage },
  { label: '我的消息', desc: '交易消息 · 通知', to: '/notifications', icon: PhChatCircleText },
]

const sortLinks = [
  { label: '综合排序', query: {} as Record<string, string> },
  { label: '最新发布', query: { sort: 'latest' } },
  { label: '价格从低到高', query: { sort: 'priceAsc' } },
  { label: '价格从高到低', query: { sort: 'priceDesc' } },
]

// 分类图标:按名称关键字匹配,未命中落到通用图标(不改后端契约)
function categoryIcon(name: string) {
  if (/数码|电子|电脑|手机/.test(name)) return PhDeviceMobile
  if (/书|教材|资料|学习/.test(name)) return PhBookOpen
  if (/服饰|鞋|包|衣/.test(name)) return PhTShirt
  if (/运动|户外|球/.test(name)) return PhBasketball
  if (/生活|好物|用品|家居/.test(name)) return PhHouseLine
  if (/美妆|个护/.test(name)) return PhSparkle
  return PhSquaresFour
}

function goSearch(value?: string): void {
  const kw = (value ?? keyword.value).trim()
  void router.push({ path: '/market', query: kw ? { keyword: kw } : {} })
}

function goCategory(id: number): void {
  void router.push({ path: '/market', query: { categoryId: String(id) } })
}

async function loadAnnouncements(): Promise<void> {
  loadingAnnouncements.value = true
  errorAnnouncements.value = false
  try {
    announcements.value = (await getLatestAnnouncements()).slice(0, 5)
  } catch {
    errorAnnouncements.value = true
  } finally {
    loadingAnnouncements.value = false
  }
}

async function loadProducts(): Promise<void> {
  loadingProducts.value = true
  errorProducts.value = false
  try {
    products.value = (await getRecommendedProducts()).slice(0, 8)
  } catch {
    errorProducts.value = true
  } finally {
    loadingProducts.value = false
  }
}

async function loadLatest(): Promise<void> {
  loadingLatest.value = true
  errorLatest.value = false
  try {
    const result = await getProducts({ pageNum: 1, pageSize: 6, sort: 'latest' })
    latestProducts.value = result.records
  } catch {
    errorLatest.value = true
  } finally {
    loadingLatest.value = false
  }
}

async function loadCategories(): Promise<void> {
  try {
    categories.value = await getProductCategories()
  } catch {
    categories.value = []
  }
}

async function loadActivities(): Promise<void> {
  loadingActivities.value = true
  errorActivities.value = false
  try {
    activities.value = (await getUpcomingActivities()).slice(0, 3)
  } catch {
    errorActivities.value = true
  } finally {
    loadingActivities.value = false
  }
}

async function loadLostFound(): Promise<void> {
  loadingLostFound.value = true
  errorLostFound.value = false
  try {
    const result = await getLostFoundList({ pageNum: 1, pageSize: 4 })
    lostFound.value = result.records
  } catch {
    errorLostFound.value = true
  } finally {
    loadingLostFound.value = false
  }
}

// 右侧悬浮操作条:回到顶部 + 发布闲置
const showBackTop = ref(false)
function onScroll(): void {
  showBackTop.value = window.scrollY > 360
}
function backToTop(): void {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  void loadAnnouncements()
  void loadProducts()
  void loadLatest()
  void loadCategories()
  void loadActivities()
  void loadLostFound()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div class="home">
    <!-- Hero:标语 + 搜索(背景图在容器内右侧对齐,与整体布局同宽) -->
    <section class="home__hero">
      <div class="home__hero-scrim" aria-hidden="true"></div>
      <div class="container home__hero-inner">
        <div class="home__hero-photo" aria-hidden="true">
          <img class="home__hero-img" :src="heroBg" alt="" />
        </div>
        <div class="home__hero-copy">
          <span class="home__hero-pill">CampusHub · 校园二手市场</span>
          <h1 class="home__hero-title">
            闲置不闲置<br />换个主人继续发光
          </h1>
          <p class="home__hero-lead">在这里,遇见校园里真正需要的宝贝</p>
          <div class="home__search">
            <n-icon :size="18" :component="PhMagnifyingGlass" class="home__search-icon" />
            <input
              v-model="keyword"
              class="home__search-input"
              type="text"
              placeholder="搜索商品、图书、数码产品、生活用品..."
              @keyup.enter="goSearch()"
            />
            <button class="home__search-btn pressable" type="button" @click="goSearch()">搜 索</button>
          </div>
          <div class="home__hot">
            <span class="home__hot-label">热门搜索:</span>
            <button
              v-for="word in hotKeywords"
              :key="word"
              type="button"
              class="home__hot-word"
              @click="goSearch(word)"
            >
              {{ word }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="container home__quick">
      <button
        v-for="(entry, index) in quickEntries"
        :key="entry.label"
        type="button"
        class="home__quick-item pressable stagger-item"
        :style="{ '--i': index }"
        @click="router.push(entry.to)"
      >
        <span class="home__quick-icon">
          <n-icon :size="22" :component="entry.icon" />
        </span>
        <span class="home__quick-text">
          <strong>{{ entry.label }}</strong>
          <small>{{ entry.desc }}</small>
        </span>
        <n-icon :size="15" :component="PhArrowRight" class="home__quick-arrow" />
      </button>
    </section>

    <!-- 商品分类 -->
    <section class="container home__section">
      <div class="home__section-head">
        <h2 class="home__section-title">
          <span class="home__section-bar" aria-hidden="true" />
          商品分类
        </h2>
        <button class="home__section-more pressable" type="button" @click="router.push('/market')">
          全部分类
          <n-icon :size="13" :component="PhArrowRight" />
        </button>
      </div>
      <div v-if="categories.length" class="home__cats">
        <button
          v-for="(cat, index) in categories"
          :key="cat.id"
          type="button"
          class="home__cat pressable stagger-item"
          :style="{ '--i': index }"
          @click="goCategory(cat.id)"
        >
          <span class="home__cat-icon">
            <n-icon :size="24" :component="categoryIcon(cat.name)" />
          </span>
          <span class="home__cat-name">{{ cat.name }}</span>
        </button>
      </div>
      <div v-else class="home__cats">
        <div v-for="n in 7" :key="n" class="home__cat home__cat--ghost">
          <span class="home__cat-icon skeleton-block"></span>
          <span class="skeleton-block" style="width: 48px; height: 12px"></span>
        </div>
      </div>
    </section>

    <!-- 热门商品 -->
    <section class="container home__section">
      <div class="home__section-head">
        <h2 class="home__section-title home__section-title--fire">
          <n-icon :size="22" :component="PhFire" class="home__fire" />
          热门商品
        </h2>
        <div class="home__sorts">
          <button
            v-for="link in sortLinks"
            :key="link.label"
            type="button"
            class="home__sort"
            @click="router.push({ path: '/market', query: link.query })"
          >
            {{ link.label }}
          </button>
        </div>
      </div>
      <div v-if="loadingProducts">
        <loading-state :rows="4" variant="grid" />
      </div>
      <div v-else-if="errorProducts" class="home__section-error">
        <empty-state
          title="商品加载失败"
          description="请检查网络或后端服务后重试"
          action-text="重新加载"
          compact
          @action="loadProducts"
        />
      </div>
      <div v-else-if="products.length" class="home__products">
        <product-card
          v-for="(item, index) in products"
          :key="item.id"
          :product="item"
          class="stagger-item"
          :style="{ '--i': index }"
        />
      </div>
      <empty-state
        v-else
        title="暂时没有在售商品"
        description="成为第一个发布闲置的同学吧"
        action-text="发布商品"
        compact
        @action="router.push('/market/publish')"
      />
    </section>

    <!-- 最新上架 -->
    <section class="container home__section">
      <div class="home__section-head">
        <h2 class="home__section-title">
          <span class="home__section-bar" aria-hidden="true" />
          最新上架
          <small class="home__section-sub">刚发布的闲置好物</small>
        </h2>
        <button class="home__section-more pressable" type="button" @click="router.push('/market')">
          查看更多
          <n-icon :size="13" :component="PhArrowRight" />
        </button>
      </div>
      <div v-if="loadingLatest">
        <loading-state :rows="2" variant="grid" />
      </div>
      <div v-else-if="errorLatest" class="home__section-error">
        <empty-state
          title="商品加载失败"
          description="请检查网络或后端服务后重试"
          action-text="重新加载"
          compact
          @action="loadLatest"
        />
      </div>
      <div v-else-if="latestProducts.length" class="home__latest">
        <router-link
          v-for="(item, index) in latestProducts"
          :key="item.id"
          :to="`/market/products/${item.id}`"
          class="home__latest-item pressable stagger-item"
          :style="{ '--i': index }"
        >
          <img
            v-if="item.images[0]"
            class="home__latest-cover"
            :src="item.images[0]"
            :alt="item.title"
            loading="lazy"
          />
          <div v-else class="home__latest-cover home__latest-cover--ghost"></div>
          <div class="home__latest-body">
            <p class="home__latest-title">{{ item.title }}</p>
            <p class="home__latest-meta">
              <span class="home__latest-price num">{{ formatPrice(item.price) }}</span>
              <span class="text-tertiary">{{ item.categoryName }}</span>
            </p>
          </div>
        </router-link>
      </div>
      <empty-state v-else title="暂无新上架商品" description="刷新看看,或去发布第一件闲置" compact />
    </section>

    <!-- 底部:校园活动 + 校园资讯/失物招领 -->
    <section class="container home__section home__section--last">
      <div class="home__bottom">
        <div>
          <div class="home__section-head">
            <h2 class="home__section-title">
              <span class="home__section-bar" aria-hidden="true" />
              校园活动
              <small class="home__section-sub">名额有限,尽早报名</small>
            </h2>
            <button class="home__section-more pressable" type="button" @click="router.push('/activities')">
              全部活动
              <n-icon :size="13" :component="PhArrowRight" />
            </button>
          </div>
          <div v-if="loadingActivities">
            <loading-state :rows="3" variant="panel" />
          </div>
          <div v-else-if="errorActivities" class="home__section-error">
            <empty-state
              title="活动加载失败"
              description="请检查网络或后端服务后重试"
              action-text="重新加载"
              compact
              @action="loadActivities"
            />
          </div>
          <div v-else-if="activities.length" class="home__activity-grid">
            <activity-card
              v-for="(item, index) in activities"
              :key="item.id"
              :activity="item"
              class="stagger-item"
              :style="{ '--i': index }"
            />
          </div>
          <empty-state v-else title="近期暂无活动" description="新的校园活动筹备中,敬请期待" compact />
        </div>

        <aside class="home__aside">
          <!-- 校园资讯 -->
          <div class="home__panel">
            <div class="home__section-head home__section-head--compact">
              <h2 class="home__section-title home__section-title--sm">
                <span class="home__section-bar" aria-hidden="true" />
                校园资讯
              </h2>
              <button class="home__section-more pressable" type="button" @click="router.push('/announcements')">
                更多
                <n-icon :size="13" :component="PhArrowRight" />
              </button>
            </div>
            <div v-if="loadingAnnouncements" class="home__news-list">
              <div v-for="n in 4" :key="n" class="home__news-item">
                <span class="skeleton-block" style="width: 70%; height: 14px"></span>
              </div>
            </div>
            <div v-else-if="errorAnnouncements" class="home__section-error">
              <empty-state title="资讯加载失败" compact action-text="重试" @action="loadAnnouncements" />
            </div>
            <div v-else-if="announcements.length" class="home__news-list">
              <router-link
                v-for="item in announcements"
                :key="item.id"
                :to="`/announcements/${item.id}`"
                class="home__news-item"
              >
                <n-icon :size="13" :component="PhArrowRight" class="home__news-arrow" />
                <span class="home__news-title">{{ item.title }}</span>
                <span class="home__news-date num">{{ formatDate(item.createdAt) }}</span>
              </router-link>
            </div>
            <empty-state v-else title="暂无公告" compact />
          </div>

          <!-- 失物招领 -->
          <div class="home__panel">
            <div class="home__section-head home__section-head--compact">
              <h2 class="home__section-title home__section-title--sm">
                <span class="home__section-bar" aria-hidden="true" />
                失物招领
              </h2>
              <button class="home__section-more pressable" type="button" @click="router.push('/lost-found')">
                更多
                <n-icon :size="13" :component="PhArrowRight" />
              </button>
            </div>
            <div v-if="loadingLostFound" class="home__lost-list">
              <div v-for="n in 3" :key="n" class="home__lost-item">
                <span class="skeleton-block" style="width: 80%; height: 14px"></span>
              </div>
            </div>
            <div v-else-if="errorLostFound" class="home__section-error">
              <empty-state title="失物信息加载失败" compact action-text="重试" @action="loadLostFound" />
            </div>
            <div v-else-if="lostFound.length" class="home__lost-list">
              <router-link
                v-for="item in lostFound"
                :key="item.id"
                :to="`/lost-found/${item.id}`"
                class="home__lost-item"
              >
                <status-tag :value="item.type" />
                <span class="home__lost-title">{{ item.title }}</span>
                <span class="home__lost-loc text-tertiary">
                  <n-icon :size="12" :component="PhMapPin" />
                  {{ item.location }}
                </span>
              </router-link>
            </div>
            <empty-state v-else title="暂无失物信息" compact />
          </div>
        </aside>
      </div>
    </section>

    <!-- 右侧悬浮操作条 -->
    <div class="home__float">
      <button
        v-show="showBackTop"
        class="home__float-btn pressable"
        type="button"
        aria-label="回到顶部"
        @click="backToTop"
      >
        <n-icon :size="18" :component="PhArrowUp" />
      </button>
      <button
        class="home__float-btn home__float-btn--primary pressable"
        type="button"
        aria-label="发布闲置"
        @click="router.push('/market/publish')"
      >
        <n-icon :size="20" :component="PhPlus" />
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.home {
  // ---------------- Hero ----------------
  &__hero {
    position: relative;
    overflow: hidden;
    border-bottom: 1px solid $color-border;
    background:
      radial-gradient(720px 320px at 85% 0%, rgba(22, 163, 74, 0.1), transparent 70%),
      linear-gradient(180deg, #eef7f1 0%, $color-bg 100%);
  }

  // 背景图容器:占容器宽度 92%、右缘与布局对齐,图片 cover 裁切铺满,
  // 让画面向左延伸到标题身后,不再留大片空白;两端渐隐融进底色
  &__hero-photo {
    position: absolute;
    top: 0;
    bottom: 0;
    right: 24px;
    width: 92%;
    overflow: hidden;
    -webkit-mask-image: linear-gradient(
      90deg,
      transparent 0%,
      #000 24%,
      #000 90%,
      rgba(0, 0, 0, 0.5) 100%
    );
    mask-image: linear-gradient(
      90deg,
      transparent 0%,
      #000 24%,
      #000 90%,
      rgba(0, 0, 0, 0.5) 100%
    );
  }

  &__hero-img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: left center;
  }

  // 轻蒙层:左侧加强保证标语可读,右侧轻纱淡化,上下轻压保证层次
  &__hero-scrim {
    position: absolute;
    inset: 0;
    background:
      linear-gradient(
        90deg,
        rgba(255, 255, 255, 0.75) 0%,
        rgba(255, 255, 255, 0.4) 16%,
        rgba(255, 255, 255, 0) 34%,
        rgba(255, 255, 255, 0) 72%,
        rgba(255, 255, 255, 0.24) 100%
      ),
      linear-gradient(180deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0) 22%, rgba(255, 255, 255, 0.22) 100%);
  }

  &__hero-inner {
    position: relative;
    padding-top: 56px;
    padding-bottom: 56px;
  }

  &__hero-copy {
    position: relative;
    z-index: 1;
  }

  &__hero-pill {
    display: inline-flex;
    align-items: center;
    padding: 5px 14px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;
    color: $color-accent;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(22, 163, 74, 0.28);
  }

  &__hero-title {
    margin: 18px 0 0;
    font-size: 40px;
    line-height: 1.26;
    font-weight: 800;
    letter-spacing: -0.02em;
    color: $color-text;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.6);
  }

  &__hero-lead {
    margin: 14px 0 0;
    font-size: 15px;
    line-height: 1.8;
    color: $color-text-secondary;
  }

  &__search {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 28px;
    max-width: 560px;
    padding: 6px 6px 6px 16px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: 999px;
    box-shadow: $shadow-soft;
    transition: $transition-fast;

    &:focus-within {
      border-color: rgba(22, 163, 74, 0.5);
      box-shadow: 0 0 0 4px rgba(22, 163, 74, 0.1);
    }
  }

  &__search-icon {
    flex-shrink: 0;
    color: $color-text-tertiary;
  }

  &__search-input {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    background: transparent;
    font-size: 14px;
    font-family: inherit;
    color: $color-text;

    &::placeholder {
      color: $color-text-tertiary;
    }
  }

  &__search-btn {
    flex-shrink: 0;
    padding: 10px 26px;
    border: none;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 600;
    letter-spacing: 0.08em;
    color: #fff;
    background: $color-accent;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      background: $color-accent-hover;
    }
  }

  &__hot {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px 14px;
    margin-top: 14px;
    font-size: 12.5px;
  }

  &__hot-label {
    color: $color-text-tertiary;
  }

  &__hot-word {
    padding: 0;
    border: none;
    background: transparent;
    font-size: 12.5px;
    color: $color-text-secondary;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
    }
  }

  // ---------------- 快捷操作 ----------------
  &__quick {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    padding-top: 32px;
  }

  &__quick-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 18px 20px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    box-shadow: $shadow-soft;
    cursor: pointer;
    text-align: left;
    transition: $transition;

    &:hover {
      transform: translateY(-2px);
      border-color: rgba(22, 163, 74, 0.4);
      box-shadow: $shadow-hover;
    }
  }

  &__quick-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    flex-shrink: 0;
    border-radius: 12px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__quick-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;

    strong {
      font-size: 14px;
      font-weight: 650;
      color: $color-text;
    }

    small {
      font-size: 11.5px;
      color: $color-text-tertiary;
      white-space: nowrap;
    }
  }

  &__quick-arrow {
    margin-left: auto;
    flex-shrink: 0;
    color: $color-text-tertiary;
  }

  // ---------------- 通用 section ----------------
  &__section {
    padding-top: 48px;

    &--last {
      padding-bottom: 72px;
    }
  }

  &__section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 20px;

    &--compact {
      margin-bottom: 12px;
    }
  }

  &__section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: $color-text;

    &--sm {
      font-size: 16px;
    }

    &--fire {
      gap: 8px;
    }
  }

  &__section-bar {
    width: 4px;
    height: 18px;
    border-radius: 2px;
    background: $color-accent;
  }

  &__section-sub {
    font-size: 12px;
    font-weight: 400;
    color: $color-text-tertiary;
  }

  &__fire {
    color: #f97316;
  }

  &__section-more {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 10px;
    border: none;
    background: transparent;
    font-size: 13px;
    color: $color-text-secondary;
    cursor: pointer;
    border-radius: 8px;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
      background: $color-accent-soft;
    }
  }

  &__section-error {
    border: 1px dashed $color-border-strong;
    border-radius: $radius-md;
    background: $color-surface;
  }

  // ---------------- 分类 ----------------
  &__cats {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 14px;
  }

  &__cat {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px 8px 16px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    cursor: pointer;
    transition: $transition;

    &:hover {
      transform: translateY(-2px);
      border-color: rgba(22, 163, 74, 0.4);
      box-shadow: $shadow-soft;

      .home__cat-icon {
        color: #fff;
        background: $color-accent;
      }
    }

    &--ghost {
      cursor: default;

      &:hover {
        transform: none;
        border-color: $color-border;
        box-shadow: none;
      }
    }
  }

  &__cat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 14px;
    color: $color-accent;
    background: $color-accent-soft;
    transition: $transition-fast;
  }

  &__cat-name {
    font-size: 13px;
    color: $color-text;
  }

  // ---------------- 热门商品 ----------------
  &__sorts {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  &__sort {
    padding: 6px 12px;
    border: none;
    border-radius: 999px;
    background: transparent;
    font-size: 13px;
    color: $color-text-secondary;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
      background: $color-accent-soft;
    }
  }

  &__products {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }

  // ---------------- 最新上架 ----------------
  &__latest {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 14px;
  }

  &__latest-item {
    display: flex;
    flex-direction: column;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    overflow: hidden;
    transition: $transition;

    &:hover {
      transform: translateY(-2px);
      border-color: rgba(22, 163, 74, 0.4);
      box-shadow: $shadow-soft;
    }
  }

  &__latest-cover {
    width: 100%;
    aspect-ratio: 1 / 1;
    object-fit: cover;
    background: $color-surface-soft;

    &--ghost {
      background: linear-gradient(135deg, $color-surface-soft 0%, #e9edf1 100%);
    }
  }

  &__latest-body {
    padding: 10px 12px 12px;
  }

  &__latest-title {
    margin: 0;
    font-size: 13px;
    font-weight: 600;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__latest-meta {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 8px;
    margin: 6px 0 0;
    font-size: 12px;
  }

  &__latest-price {
    font-size: 15px;
    font-weight: 700;
    color: $color-price;
  }

  // ---------------- 底部双栏 ----------------
  &__bottom {
    display: grid;
    grid-template-columns: minmax(0, 1.65fr) minmax(0, 1fr);
    gap: 28px;
    align-items: start;
  }

  &__activity-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  &__aside {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__panel {
    padding: 18px 20px 12px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-md;
  }

  &__news-list {
    display: flex;
    flex-direction: column;
  }

  &__news-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 9px 0;
    font-size: 13px;
    border-bottom: 1px dashed $color-border;

    &:last-child {
      border-bottom: none;
    }

    &:hover .home__news-title {
      color: $color-accent;
    }
  }

  &__news-arrow {
    flex-shrink: 0;
    color: $color-accent;
  }

  &__news-title {
    flex: 1;
    min-width: 0;
    font-size: 13px;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: $transition-fast;
  }

  &__news-date {
    flex-shrink: 0;
    font-size: 12px;
    color: $color-text-tertiary;
  }

  &__lost-list {
    display: flex;
    flex-direction: column;
  }

  &__lost-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    font-size: 13px;
    border-bottom: 1px dashed $color-border;

    &:last-child {
      border-bottom: none;
    }

    &:hover .home__lost-title {
      color: $color-accent;
    }
  }

  &__lost-title {
    flex: 1;
    min-width: 0;
    font-weight: 550;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: $transition-fast;
  }

  &__lost-loc {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    max-width: 40%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  // ---------------- 悬浮操作条 ----------------
  &__float {
    position: fixed;
    right: 28px;
    bottom: 96px;
    z-index: 25;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__float-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border: 1px solid $color-border;
    border-radius: 12px;
    color: $color-text-secondary;
    background: $color-surface;
    box-shadow: $shadow-soft;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
      border-color: rgba(22, 163, 74, 0.4);
    }

    &--primary {
      border: none;
      color: #fff;
      background: #5cd183;

      &:hover {
        color: #fff;
        background: #45c16e;
      }
    }
  }

  // ---------------- 响应式 ----------------
  @media (max-width: 1200px) {
    &__products {
      grid-template-columns: repeat(3, 1fr);
    }

    &__latest {
      grid-template-columns: repeat(4, 1fr);
    }

    &__cats {
      grid-template-columns: repeat(4, 1fr);
    }
  }

  @media (max-width: 992px) {
    &__hero-inner {
      padding-top: 44px;
      padding-bottom: 44px;
    }

    &__quick {
      grid-template-columns: repeat(2, 1fr);
    }

    &__products {
      grid-template-columns: repeat(2, 1fr);
    }

    &__latest {
      grid-template-columns: repeat(3, 1fr);
    }

    &__bottom {
      grid-template-columns: 1fr;
    }

    &__float {
      right: 16px;
      bottom: 72px;
    }
  }

  @media (max-width: 768px) {
    &__hero-title {
      font-size: 30px;
    }

    &__search-btn {
      padding: 10px 18px;
      letter-spacing: 0;
    }

    &__quick {
      gap: 12px;
    }

    &__quick-item {
      padding: 14px;
    }

    &__cats {
      grid-template-columns: repeat(3, 1fr);
    }

    &__latest {
      grid-template-columns: repeat(2, 1fr);
    }

    &__activity-grid {
      grid-template-columns: 1fr;
    }

    &__sorts {
      display: none;
    }
  }

  @media (max-width: 560px) {
    &__products {
      grid-template-columns: 1fr;
    }
  }
}
</style>
