<script setup lang="ts">
import { reactive } from 'vue'
import { NButton, NIcon, NInput, NSelect } from 'naive-ui'
import { PhMagnifyingGlass } from '@phosphor-icons/vue'
import { getAnnouncements } from '@/api/announcement'
import type { Announcement } from '@/types/announcement'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

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
} = usePagination<Announcement>()

const filters = reactive({
  keyword: '',
  category: '',
})

// 公告分类为已知的固定分类;若后端提供分类接口可替换
const categoryOptions = [
  { label: '全部公告', value: '' },
  { label: '通知公告', value: '通知公告' },
  { label: '教务信息', value: '教务信息' },
  { label: '后勤服务', value: '后勤服务' },
  { label: '校园活动', value: '校园活动' },
]

async function fetchList(): Promise<void> {
  await load((p) =>
    getAnnouncements({
      ...p,
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
    }),
  )
}

async function handleSearch(): Promise<void> {
  await reset(fetchList)
}

bind(fetchList)
</script>

<template>
  <page-container title="校园公告" subtitle="平台通知、教务信息与后勤服务动态">
    <div class="announcement-filter">
      <n-input
        v-model:value="filters.keyword"
        placeholder="搜索公告标题或内容"
        clearable
        class="announcement-filter__search"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon :component="PhMagnifyingGlass" />
        </template>
      </n-input>
      <n-select
        v-model:value="filters.category"
        :options="categoryOptions"
        class="announcement-filter__category"
        @update:value="handleSearch"
      />
      <n-button type="primary" @click="handleSearch">搜索</n-button>
    </div>

    <div v-if="loading" class="announcement-list">
      <loading-state :rows="8" variant="list" />
    </div>

    <div v-else-if="error" class="announcement-list__error">
      <empty-state
        title="公告加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>

    <div v-else-if="result?.records.length" class="announcement-list">
      <router-link
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        :to="`/announcements/${item.id}`"
        class="announcement-row lift stagger-item"
        :style="{ '--i': index }"
      >
        <div class="announcement-row__main">
          <div class="announcement-row__meta">
            <span class="announcement-row__category">{{ item.category }}</span>
            <span class="text-tertiary">{{ item.author }}</span>
            <span class="text-tertiary">{{ formatDateTime(item.createdAt) }}</span>
          </div>
          <h3 class="announcement-row__title">{{ item.title }}</h3>
          <p class="announcement-row__excerpt">{{ item.content.slice(0, 120) }}…</p>
        </div>
        <span class="announcement-row__views num text-tertiary">{{ item.viewCount }} 次浏览</span>
      </router-link>
    </div>

    <empty-state
      v-else
      title="没有找到相关公告"
      description="换个关键词或分类试试"
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

.announcement-filter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px auto;
  gap: 12px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__error {
    border: 1px dashed $color-border-strong;
    border-radius: $radius-md;
  }
}

.announcement-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 22px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  transition: $transition;

  &:hover {
    border-color: rgba(22, 163, 74, 0.4);
  }

  &__main {
    min-width: 0;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
  }

  &__category {
    padding: 1px 8px;
    border-radius: 6px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__title {
    margin: 8px 0 4px;
    font-size: 16px;
    font-weight: 650;
    color: $color-text;
  }

  &__excerpt {
    margin: 0;
    font-size: 13px;
    color: $color-text-secondary;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__views {
    flex-shrink: 0;
    font-size: 12px;
    white-space: nowrap;
    padding-top: 2px;
  }

  @media (max-width: 768px) {
    &__views {
      display: none;
    }
  }
}
</style>
