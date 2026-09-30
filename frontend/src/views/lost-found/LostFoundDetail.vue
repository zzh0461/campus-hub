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
  PhCheckCircle,
  PhChatCircleDots,
  PhLockSimple,
  PhMapPin,
  PhClock,
} from '@phosphor-icons/vue'
import { deleteLostFound, getLostFoundDetail } from '@/api/lostFound'
import { useAuthStore } from '@/stores/auth'
import type { ContactItem, ContactVisibility } from '@/types/common'
import type { LostFoundItem } from '@/types/lostFound'
import { formatDateTime, formatRelative } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref(false)
const detail = ref<LostFoundItem | null>(null)
const deleting = ref(false)

/** 联系方式弹窗显隐 */
const contactModalVisible = ref(false)

const canDelete = computed(() => {
  if (!detail.value) return false
  return detail.value.publisherId === authStore.user?.id || authStore.isAdmin
})

// 是否已解决：已解决时在标题上方显示高亮提示条（代替头部那个不醒目的灰色状态标签）
const isResolved = computed(() => detail.value?.status === 'RESOLVED')

/**
 * 联系方式可见性(口径与后端一致:仅登录用户可见):
 * - 未登录 → anonymous,弹窗引导登录
 * - 已登录 → granted,完整明文展示发布者填写的联系方式
 */
const contactVisibility = computed<ContactVisibility>(() =>
  authStore.isLoggedIn ? 'granted' : 'anonymous',
)

/** 弹窗内展示的联系方式:发布者填的是自由文本(电话/QQ/微信),原样完整展示 */
const contactItems = computed<ContactItem[]>(() => {
  const contact = detail.value?.contact
  if (contactVisibility.value !== 'granted' || !contact) return []
  return [{ label: '联系方式', value: contact, copyTip: '联系方式已复制' }]
})

const contactSubtitle = computed(() =>
  detail.value ? `发布于 ${formatDateTime(detail.value.createdAt)}` : '',
)

/** 详情页只在登录后才拿得到 contact,未登录时点卡片就是"引导登录" */
function openContactModal(): void {
  if (!detail.value) return
  contactModalVisible.value = true
}

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    detail.value = await getLostFoundDetail(Number(route.params.id))
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function handleDelete(): Promise<void> {
  if (!detail.value || deleting.value) return
  deleting.value = true
  try {
    await deleteLostFound(detail.value.id)
    message.success('已删除')
    void router.push('/lost-found')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <page-container max-width="880">
    <n-button text class="detail-back" @click="router.back()">
      <template #icon>
        <n-icon :component="PhArrowLeft" />
      </template>
      返回
    </n-button>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton-block" style="height: 260px" />
      <div class="skeleton-block" style="height: 28px; width: 70%" />
    </div>
    <div v-else-if="error" class="detail-error">
      <empty-state
        title="信息加载失败"
        description="该记录可能已被删除"
        action-text="回到失物招领"
        @action="router.push('/lost-found')"
      />
    </div>
    <div v-else-if="detail" class="lost-detail">
      <div v-if="detail.images.length" class="lost-detail__gallery">
        <img v-for="image in detail.images" :key="image" :src="image" :alt="detail.title" />
      </div>

      <div class="lost-detail__body panel">
        <div class="lost-detail__head">
          <div class="lost-detail__tags">
            <status-tag :value="detail.type" />
            <status-tag v-if="!isResolved" :value="detail.status" />
          </div>
          <div v-if="canDelete" class="lost-detail__actions">
            <n-popconfirm @positive-click="handleDelete">
              <template #trigger>
                <n-button size="small" type="error" ghost :loading="deleting">删除</n-button>
              </template>
              确认删除这条信息?删除后无法恢复。
            </n-popconfirm>
          </div>
        </div>

        <!-- 已解决高亮提示条：把"已解决"三个字用品牌绿底白字强调，一眼可见 -->
        <div v-if="isResolved" class="lost-detail__resolved">
          <n-icon :size="18" :component="PhCheckCircle" />
          <span>
            该信息<strong class="lost-detail__resolved-word">已解决</strong>：失物已找回或招领已被认领，仅供回顾参考
          </span>
        </div>

        <h1 class="lost-detail__title">{{ detail.title }}</h1>

        <div class="lost-detail__meta">
          <span>
            <n-icon :size="15" :component="PhMapPin" />
            {{ detail.location }}
          </span>
          <span>
            <n-icon :size="15" :component="PhClock" />
            {{ formatRelative(detail.createdAt) }}发布
          </span>
        </div>

        <section class="lost-detail__section">
          <h2 class="lost-detail__section-title">详细信息</h2>
          <p class="rich-text lost-detail__desc">{{ detail.description }}</p>
        </section>

        <section class="lost-detail__section">
          <h2 class="lost-detail__section-title">联系方式</h2>
          <button
            type="button"
            class="lost-detail__contact pressable"
            @click="openContactModal"
          >
            <n-icon
              :size="18"
              :component="contactVisibility === 'granted' ? PhChatCircleDots : PhLockSimple"
            />
            <span class="lost-detail__contact-text">
              {{ contactVisibility === 'granted' ? '查看发布者联系方式' : '登录后查看发布者联系方式' }}
            </span>
            <span class="lost-detail__contact-more">
              {{ contactVisibility === 'granted' ? '展开' : '去登录' }}
            </span>
          </button>
        </section>

        <section class="lost-detail__section">
          <h2 class="lost-detail__section-title">发布者</h2>
          <div class="lost-detail__publisher">
            <user-avatar :avatar="detail.publisherAvatar" :name="detail.publisherName" :size="40" />
            <div>
              <strong>{{ detail.publisherName }}</strong>
              <span class="text-tertiary">发布于 {{ formatDateTime(detail.createdAt) }}</span>
            </div>
          </div>
        </section>
      </div>
    </div>

    <contact-dialog
      v-model:show="contactModalVisible"
      :name="detail?.publisherName || ''"
      :avatar="detail?.publisherAvatar || ''"
      :subtitle="contactSubtitle"
      :contacts="contactItems"
      :visibility="contactVisibility"
      :gate-hint="'联系方式仅对已登录用户开放,请先登录后查看。'"
      tip="联系时请说明物品特征,谨防冒领与私下转账。"
    />
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.detail-back {
  margin-bottom: 18px;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.lost-detail {
  &__gallery {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 12px;
    margin-bottom: 18px;

    img {
      width: 100%;
      aspect-ratio: 4 / 3;
      object-fit: cover;
      border-radius: $radius-md;
      border: 1px solid $color-border;
    }
  }

  &__body {
    padding: 28px 32px;
    box-shadow: none;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
  }

  &__tags {
    display: flex;
    gap: 8px;
  }

  &__resolved {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 14px;
    padding: 10px 14px;
    border: 1px solid rgba(22, 163, 74, 0.35);
    border-radius: $radius-sm;
    background: $color-accent-soft;
    color: $color-text;
    font-size: 13px;
  }

  &__resolved-word {
    margin: 0 2px;
    padding: 1px 10px;
    border-radius: 999px;
    background: $color-accent;
    color: #fff;
    font-weight: 700;
  }

  &__title {
    margin: 14px 0 10px;
    font-size: 24px;
    font-weight: 750;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    padding-bottom: 18px;
    border-bottom: 1px solid $color-border;
    font-size: 13px;
    color: $color-text-secondary;

    span {
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
  }

  &__section {
    margin-top: 24px;
  }

  &__section-title {
    margin: 0 0 8px;
    font-size: 15px;
    font-weight: 650;
    color: $color-text;
  }

  &__desc {
    margin: 0;
    font-size: 14px;
  }

  // 联系方式入口卡片:点开弹窗后才展示明文,未登录时用锁图标提示需要登录
  &__contact {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    padding: 13px 16px;
    border: 1px dashed $color-border-strong;
    border-radius: $radius-sm;
    background: $color-surface-soft;
    font-size: 14px;
    color: $color-accent;
    text-align: left;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      border-color: rgba(22, 163, 74, 0.5);
      background: $color-accent-soft;
    }
  }

  &__contact-text {
    flex: 1;
    min-width: 0;
    font-weight: 600;
    color: $color-text;
  }

  &__contact-more {
    flex-shrink: 0;
    font-size: 12px;
    font-weight: 600;
    color: $color-accent;
  }

  &__publisher {
    display: flex;
    align-items: center;
    gap: 12px;

    div {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    strong {
      font-size: 14px;
      color: $color-text;
    }

    span {
      font-size: 12px;
    }
  }

  @media (max-width: 640px) {
    &__body {
      padding: 20px 18px;
    }
  }
}
</style>
