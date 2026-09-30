<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { NButton, NIcon, NInput, NSelect } from 'naive-ui'
import { PhMagnifyingGlass } from '@phosphor-icons/vue'
import { getActivities, getActivityCategories } from '@/api/activity'
import type { Activity, ActivityCategory, ActivityStatus } from '@/types/activity'
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
} = usePagination<Activity>({ initialPageSize: 12 })

const categories = ref<ActivityCategory[]>([])

const filters = reactive({
  keyword: '',
  categoryId: null as number | null,
  status: '' as ActivityStatus | '',
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '即将开始', value: 'UPCOMING' },
  { label: '进行中', value: 'ONGOING' },
  { label: '已结束', value: 'FINISHED' },
]

async function loadCategories(): Promise<void> {
  try {
    categories.value = await getActivityCategories()
  } catch {
    categories.value = []
  }
}

async function fetchList(): Promise<void> {
  await load((p) =>
    getActivities({
      ...p,
      keyword: filters.keyword.trim() || undefined,
      categoryId: filters.categoryId ?? undefined,
      status: filters.status || undefined,
    }),
  )
}

function handleSearch(): void {
  // 筛选条件变了必须回到第 1 页,否则会留在越界页码上(空列表)
  void reset(fetchList)
}

bind(fetchList)

onMounted(() => {
  void loadCategories()
})
</script>

<template>
  <page-container title="校园活动" subtitle="讲座、赛事、社团与志愿服务,丰富你的课余生活">
    <div class="activity-filter">
      <n-input
        v-model:value="filters.keyword"
        placeholder="搜索活动名称或内容"
        clearable
        class="activity-filter__search"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon :component="PhMagnifyingGlass" />
        </template>
      </n-input>
      <n-select
        v-model:value="filters.categoryId"
        placeholder="全部分类"
        clearable
        :options="categories.map((c) => ({ label: c.name, value: c.id }))"
        class="activity-filter__category"
      />
      <n-select
        v-model:value="filters.status"
        :options="statusOptions"
        class="activity-filter__status"
      />
      <n-button type="primary" @click="handleSearch">搜索</n-button>
    </div>

    <p class="activity-result text-tertiary">共 {{ total }} 个活动</p>

    <div v-if="loading">
      <loading-state :rows="6" variant="grid" />
    </div>
    <div v-else-if="error" class="activity-error">
      <empty-state
        title="活动加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <div v-else-if="result?.records.length" class="activity-grid">
      <activity-card
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        :activity="item"
        class="stagger-item"
        :style="{ '--i': index }"
      />
    </div>
    <empty-state
      v-else
      title="没有找到相关活动"
      description="换个关键词或筛选条件试试"
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

.activity-filter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 200px 160px auto;
  gap: 12px;
  margin-bottom: 12px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.activity-result {
  margin: 4px 2px 16px;
  font-size: 13px;
}

.activity-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media (max-width: 992px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 560px) {
    grid-template-columns: 1fr;
  }
}
</style>
