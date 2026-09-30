<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NIcon,
  NInput,
  NTabs,
  NTabPane,
} from 'naive-ui'
import {
  PhPlus,
  PhMapPin,
  PhMagnifyingGlass,
} from '@phosphor-icons/vue'
import { getLostFoundList } from '@/api/lostFound'
import type { LostFoundItem, LostFoundType } from '@/types/lostFound'
import { formatRelative } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

const router = useRouter()

const {
  result,
  pageNum,
  pageSize,
  total,
  loading,
  error,
  load,
  handlePageSizeChange,
  reset,
  bind,
} = usePagination<LostFoundItem>()

const type = ref<LostFoundType | 'ALL'>('ALL')
const keyword = ref('')

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '失物', value: 'LOST' },
  { label: '招领', value: 'FOUND' },
]

async function fetchList(): Promise<void> {
  await load((p) =>
    getLostFoundList({
      ...p,
      type: type.value === 'ALL' ? undefined : type.value,
      keyword: keyword.value.trim() || undefined,
    }),
  )
}

function handleTabChange(value: string): void {
  type.value = value as LostFoundType | 'ALL'
  void reset(fetchList)
}

function handleSearch(): void {
  // 筛选条件变了必须回到第 1 页,否则会留在越界页码上(空列表)
  void reset(fetchList)
}

bind(fetchList)
</script>

<template>
  <page-container title="失物招领" subtitle="丢失的物品,和拾到的善意,都从这里开始">
    <template #extra>
      <n-button type="primary" @click="router.push('/lost-found/publish')">
        <template #icon>
          <n-icon :component="PhPlus" />
        </template>
        发布信息
      </n-button>
    </template>

    <div class="lost-filter">
      <n-input
        v-model:value="keyword"
        placeholder="搜索物品、地点关键词"
        clearable
        class="lost-filter__search"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon :component="PhMagnifyingGlass" />
        </template>
      </n-input>
      <n-button type="primary" @click="handleSearch">搜索</n-button>
    </div>

    <n-tabs :value="type" type="line" animated @update:value="handleTabChange">
      <n-tab-pane v-for="tab in tabs" :key="tab.value" :name="tab.value" :tab="tab.label" />
    </n-tabs>

    <div v-if="loading">
      <loading-state :rows="6" variant="list" />
    </div>
    <div v-else-if="error" class="lost-error">
      <empty-state
        title="信息加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <div v-else-if="result?.records.length" class="lost-list">
      <router-link
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        :to="`/lost-found/${item.id}`"
        class="lost-item lift stagger-item"
        :style="{ '--i': index }"
      >
        <div class="lost-item__thumb">
          <img
            v-if="item.images[0]"
            :src="item.images[0]"
            :alt="item.title"
            loading="lazy"
          />
          <div v-else class="lost-item__placeholder">
            <n-icon :size="26" :component="item.type === 'LOST' ? PhMagnifyingGlass : PhPlus" />
          </div>
          <div class="lost-item__type">
            <status-tag :value="item.type" />
          </div>
        </div>
        <div class="lost-item__main">
          <h3 class="lost-item__title">{{ item.title }}</h3>
          <p class="lost-item__desc">{{ item.description.slice(0, 80) }}…</p>
          <div class="lost-item__meta">
            <span class="lost-item__location">
              <n-icon :size="13" :component="PhMapPin" />
              {{ item.location }}
            </span>
            <span class="text-tertiary">{{ formatRelative(item.createdAt) }}</span>
          </div>
        </div>
        <div class="lost-item__publisher">
          <user-avatar :avatar="item.publisherAvatar" :name="item.publisherName" :size="28" />
          <span class="text-secondary">{{ item.publisherName }}</span>
        </div>
      </router-link>
    </div>
    <empty-state
      v-else
      title="没有找到相关信息"
      description="换个关键词试试,或发布一条新的失物/招领信息"
      action-text="发布信息"
      @action="router.push('/lost-found/publish')"
    />

    <pagination
      v-if="total > 0"
      v-model:page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)"
    />
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.lost-filter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  margin-bottom: 6px;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.lost-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.lost-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lost-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 14px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  transition: $transition;

  &:hover {
    border-color: rgba(22, 163, 74, 0.4);
  }

  &__thumb {
    position: relative;
    width: 108px;
    height: 80px;
    flex-shrink: 0;
    border-radius: $radius-sm;
    overflow: hidden;
    background: $color-surface-soft;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    color: $color-text-tertiary;
    background: linear-gradient(135deg, $color-surface-soft, #e9edf1);
  }

  &__type {
    position: absolute;
    top: 6px;
    left: 6px;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__title {
    margin: 0 0 4px;
    font-size: 15px;
    font-weight: 600;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__desc {
    margin: 0 0 8px;
    font-size: 13px;
    color: $color-text-secondary;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 14px;
    font-size: 12px;
  }

  &__location {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: $color-text-secondary;
  }

  &__publisher {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    flex-shrink: 0;
  }

  @media (max-width: 640px) {
    flex-wrap: wrap;

    &__thumb {
      width: 100%;
      height: 160px;
    }

    &__publisher {
      display: none;
    }
  }
}
</style>
