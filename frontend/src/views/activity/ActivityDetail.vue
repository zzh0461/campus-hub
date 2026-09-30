<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NIcon,
  NPopconfirm,
  NProgress,
  useMessage,
} from 'naive-ui'
import {
  PhArrowLeft,
  PhCalendarBlank,
  PhMapPin,
  PhUser,
  PhClock,
} from '@phosphor-icons/vue'
import {
  cancelActivityRegistration,
  getActivity,
  registerActivity,
} from '@/api/activity'
import { useAuthStore } from '@/stores/auth'
import type { Activity } from '@/types/activity'
import { formatDateTime } from '@/utils/format'
import { emitToast } from '@/utils/events'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref(false)
const activity = ref<Activity | null>(null)
const submitting = ref(false)

const seatPercent = computed(() => {
  if (!activity.value) return 0
  return Math.min(
    100,
    Math.round((activity.value.currentParticipants / activity.value.maxParticipants) * 100),
  )
})

const buttonDisabled = computed(() => {
  if (!activity.value) return true
  if (activity.value.registered) return false
  if (activity.value.remainingParticipants <= 0) return true
  if (activity.value.status === 'FINISHED') return true
  return false
})

const buttonLabel = computed(() => {
  if (!activity.value) return ''
  if (activity.value.registered) return '已报名'
  if (activity.value.status === 'FINISHED') return '活动已结束'
  if (activity.value.remainingParticipants <= 0) return '名额已满'
  if (activity.value.status === 'ONGOING') return '报名参加'
  return '报名参加'
})

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    activity.value = await getActivity(Number(route.params.id))
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function handleRegister(): Promise<void> {
  if (!authStore.isLoggedIn) {
    emitToast('warning', '请先登录')
    void router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!activity.value || submitting.value) return
  submitting.value = true
  try {
    activity.value = await registerActivity(activity.value.id)
    message.success('报名成功,可在「我的活动」中查看')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}

async function handleCancel(): Promise<void> {
  if (!activity.value || submitting.value) return
  submitting.value = true
  try {
    activity.value = await cancelActivityRegistration(activity.value.id)
    message.success('已取消报名,名额已释放')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <page-container max-width="980">
    <n-button text class="detail-back" @click="router.back()">
      <template #icon>
        <n-icon :component="PhArrowLeft" />
      </template>
      返回
    </n-button>

    <div v-if="loading">
      <div class="skeleton-block" style="height: 280px; border-radius: 20px" />
      <div class="activity-detail-loading">
        <div class="skeleton-block" style="height: 28px; width: 60%" />
        <div class="skeleton-block" style="height: 120px" />
      </div>
    </div>

    <div v-else-if="error" class="detail-error">
      <empty-state
        title="活动加载失败"
        description="该活动可能已下架或删除"
        action-text="回到活动列表"
        @action="router.push('/activities')"
      />
    </div>

    <article v-else-if="activity" class="activity-detail">
      <div class="activity-detail__cover">
        <img :src="activity.cover" :alt="activity.title" />
        <div class="activity-detail__tag">
          <status-tag :value="activity.status" />
        </div>
      </div>

      <div class="activity-detail__body">
        <div class="activity-detail__head">
          <div>
            <span class="activity-detail__category">{{ activity.categoryName }}</span>
            <h1 class="activity-detail__title">{{ activity.title }}</h1>
          </div>
          <div class="activity-detail__register">
            <n-popconfirm v-if="activity.registered" @positive-click="handleCancel">
              <template #trigger>
                <n-button
                  type="primary"
                  ghost
                  size="large"
                  :loading="submitting"
                  :disabled="activity.status === 'FINISHED'"
                >
                  已报名
                </n-button>
              </template>
              确认取消报名?名额将立即释放给其他同学。
            </n-popconfirm>
            <n-button
              v-else
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="buttonDisabled"
              @click="handleRegister"
            >
              {{ buttonLabel }}
            </n-button>
          </div>
        </div>

        <div class="activity-detail__info">
          <div class="activity-detail__info-item">
            <span class="activity-detail__info-icon">
              <n-icon :size="17" :component="PhCalendarBlank" />
            </span>
            <div>
              <strong>开始时间</strong>
              <span>{{ formatDateTime(activity.startTime) }}</span>
            </div>
          </div>
          <div class="activity-detail__info-item">
            <span class="activity-detail__info-icon">
              <n-icon :size="17" :component="PhClock" />
            </span>
            <div>
              <strong>结束时间</strong>
              <span>{{ formatDateTime(activity.endTime) }}</span>
            </div>
          </div>
          <div class="activity-detail__info-item">
            <span class="activity-detail__info-icon">
              <n-icon :size="17" :component="PhMapPin" />
            </span>
            <div>
              <strong>活动地点</strong>
              <span>{{ activity.location }}</span>
            </div>
          </div>
          <div class="activity-detail__info-item">
            <span class="activity-detail__info-icon">
              <n-icon :size="17" :component="PhUser" />
            </span>
            <div>
              <strong>主办方</strong>
              <span>{{ activity.organizer }}</span>
            </div>
          </div>
        </div>

        <div class="activity-detail__seats panel">
          <div class="activity-detail__seats-head">
            <span>报名进度</span>
            <span class="num">
              {{ activity.currentParticipants }} / {{ activity.maxParticipants }} 人
            </span>
          </div>
          <n-progress
            type="line"
            :percentage="seatPercent"
            :height="6"
            color="#16a34a"
            rail-color="#e8eaee"
            :show-indicator="false"
          />
          <p class="activity-detail__seats-remain text-secondary">
            剩余名额
            <strong class="num text-accent">{{ activity.remainingParticipants }}</strong> 人
            <template v-if="activity.remainingParticipants === 0">
              ,名额已满,关注下次活动吧
            </template>
          </p>
        </div>

        <section class="activity-detail__section">
          <h2 class="activity-detail__section-title">活动介绍</h2>
          <p class="rich-text activity-detail__desc">{{ activity.description }}</p>
        </section>
      </div>
    </article>
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.detail-back {
  margin-bottom: 18px;
}

.detail-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.activity-detail-loading {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 24px;
}

.activity-detail {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  overflow: hidden;

  &__cover {
    position: relative;
    aspect-ratio: 21 / 8;
    background: $color-surface-soft;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__tag {
    position: absolute;
    top: 16px;
    left: 16px;
  }

  &__body {
    padding: 30px 36px 40px;
  }

  &__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 24px;
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
    margin: 10px 0 0;
    font-size: 26px;
    line-height: 1.35;
    font-weight: 750;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__register {
    flex-shrink: 0;
    padding-top: 4px;
  }

  &__info {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
    margin-top: 24px;
  }

  &__info-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid $color-border;
    border-radius: $radius-sm;

    div {
      display: flex;
      flex-direction: column;
      gap: 1px;
    }

    strong {
      font-size: 12px;
      color: $color-text-tertiary;
      font-weight: 500;
    }

    span {
      font-size: 13px;
      color: $color-text;
    }
  }

  &__info-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    border-radius: 10px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__seats {
    margin-top: 22px;
    padding: 18px 20px;
    box-shadow: none;
  }

  &__seats-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    font-weight: 600;
    color: $color-text;
    margin-bottom: 10px;
  }

  &__seats-remain {
    margin: 10px 0 0;
    font-size: 13px;
  }

  &__section {
    margin-top: 30px;
  }

  &__section-title {
    margin: 0 0 10px;
    font-size: 16px;
    font-weight: 650;
    color: $color-text;
  }

  &__desc {
    margin: 0;
    font-size: 14px;
  }

  @media (max-width: 768px) {
    &__head {
      flex-direction: column;
    }

    &__body {
      padding: 22px 18px 30px;
    }

    &__info {
      grid-template-columns: 1fr;
    }
  }
}
</style>
