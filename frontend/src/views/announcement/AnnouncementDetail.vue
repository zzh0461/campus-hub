<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon } from 'naive-ui'
import { PhArrowLeft, PhEye } from '@phosphor-icons/vue'
import { getAnnouncement } from '@/api/announcement'
import type { Announcement } from '@/types/announcement'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref(false)
const detail = ref<Announcement | null>(null)

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    detail.value = await getAnnouncement(Number(route.params.id))
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <page-container max-width="860">
    <n-button text class="detail-back" @click="router.back()">
      <template #icon>
        <n-icon :component="PhArrowLeft" />
      </template>
      返回
    </n-button>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton-block" style="height: 32px; width: 60%" />
      <div class="skeleton-block" style="height: 16px; width: 30%" />
      <div class="skeleton-block" style="height: 260px" />
    </div>

    <div v-else-if="error" class="detail-error">
      <empty-state
        title="公告加载失败"
        description="该公告可能已删除,或网络暂时不可用"
        action-text="重新加载"
        @action="load"
      />
    </div>

    <article v-else-if="detail" class="detail panel">
      <header class="detail__head">
        <div class="detail__meta">
          <span class="detail__category">{{ detail.category }}</span>
          <span>{{ detail.author }}</span>
          <span class="text-tertiary">{{ formatDateTime(detail.createdAt) }}</span>
        </div>
        <h1 class="detail__title">{{ detail.title }}</h1>
        <div class="detail__stats">
          <n-icon :size="14" :component="PhEye" />
          <span class="num text-tertiary">{{ detail.viewCount }} 次浏览</span>
        </div>
      </header>
      <div class="detail__divider" />
      <div class="rich-text detail__content">{{ detail.content }}</div>
      <footer class="detail__foot text-tertiary">
        最后更新:{{ formatDateTime(detail.updatedAt) }}
      </footer>
    </article>
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
  padding: 32px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
}

.detail-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-lg;
}

.detail {
  padding: 40px 44px;

  &__meta {
    display: flex;
    align-items: center;
    gap: 14px;
    font-size: 13px;
    color: $color-text-secondary;
  }

  &__category {
    padding: 2px 10px;
    border-radius: 7px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__title {
    margin: 16px 0 8px;
    font-size: 28px;
    line-height: 1.4;
    font-weight: 750;
    letter-spacing: -0.01em;
    color: $color-text;
  }

  &__stats {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
  }

  &__divider {
    height: 1px;
    background: $color-border;
    margin: 24px 0;
  }

  &__content {
    font-size: 15px;
  }

  &__foot {
    margin-top: 32px;
    padding-top: 16px;
    border-top: 1px solid $color-border;
    font-size: 12px;
  }

  @media (max-width: 768px) {
    padding: 24px 20px;

    &__title {
      font-size: 22px;
    }
  }
}
</style>
