<script setup lang="ts">
import { h, ref } from 'vue'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NPopconfirm,
  NSpace,
  type DataTableColumns,
  useMessage,
} from 'naive-ui'
import { PhMagnifyingGlass } from '@phosphor-icons/vue'
import {
  deleteAdminLostFound,
  getAdminLostFound,
  updateLostFoundStatus,
} from '@/api/admin'
import type { LostFoundItem } from '@/types/lostFound'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

const message = useMessage()

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

const keyword = ref('')

async function fetchList(): Promise<void> {
  await load((p) => getAdminLostFound({ ...p, keyword: keyword.value.trim() || undefined }))
}

/** 搜索:关键词变了必须回到第 1 页,否则会留在越界页码上搜不到东西 */
async function handleSearch(): Promise<void> {
  await reset(fetchList)
}

async function toggleStatus(item: LostFoundItem): Promise<void> {
  const next = item.status === 'OPEN' ? 'RESOLVED' : 'OPEN'
  try {
    await updateLostFoundStatus(item.id, next)
    item.status = next
    message.success(next === 'RESOLVED' ? '已标记为已解决' : '已重新开启')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleDelete(item: LostFoundItem): Promise<void> {
  try {
    await deleteAdminLostFound(item.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((l) => l.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

const columns: DataTableColumns<LostFoundItem> = [
  {
    title: '信息',
    key: 'title',
    width: 300,
    render: (row) =>
      h('div', { class: 'lost-cell' }, [
        h('div', { class: 'lost-cell__text' }, [
          h('strong', row.title),
          h('span', row.location),
        ]),
      ]),
  },
  {
    title: '类型',
    key: 'type',
    width: 90,
    render: (row) => h('span', null, row.type === 'LOST' ? '失物' : '招领'),
  },
  { title: '发布者', key: 'publisherName', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h('span', null, row.status === 'OPEN' ? '进行中' : '已解决'),
  },
  {
    title: '发布时间',
    key: 'createdAt',
    width: 170,
    render: (row) => h('span', { class: 'num' }, formatDateTime(row.createdAt)),
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) =>
      h(NSpace, { size: 4 }, {
        default: () => [
          h(
            NPopconfirm,
            { onPositiveClick: () => toggleStatus(row) },
            {
              trigger: () =>
                h(
                  NButton,
                  { size: 'small', type: row.status === 'OPEN' ? 'success' : 'default', ghost: true },
                  { default: () => (row.status === 'OPEN' ? '标记已解决' : '重新开启') },
                ),
              default: () =>
                row.status === 'OPEN'
                  ? '确认将该记录标记为已解决?'
                  : '确认重新开启该记录?',
            },
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' }),
              default: () => '删除后不可恢复,确认删除该记录?',
            },
          ),
        ],
      }),
  },
]

bind(fetchList)
</script>

<template>
  <div class="manage-page">
    <div class="manage-toolbar">
      <n-input
        v-model:value="keyword"
        placeholder="搜索标题或地点"
        clearable
        class="manage-toolbar__search"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon :component="PhMagnifyingGlass" />
        </template>
      </n-input>
      <n-button type="primary" @click="handleSearch">搜索</n-button>
    </div>

    <div v-if="loading" class="manage-loading">
      <loading-state :rows="8" variant="panel" />
    </div>
    <div v-else-if="error" class="manage-error">
      <empty-state
        title="列表加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <n-data-table
      v-else
      :columns="columns"
      :data="result?.records ?? []"
      :row-key="(row: LostFoundItem) => row.id"
      :bordered="false"
      class="manage-table"
    />

    <pagination
      v-if="total > 0"
      v-model:page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)"
    />
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.manage-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 420px) auto;
  gap: 12px;
  margin-bottom: 18px;
}

.manage-loading {
  padding: 24px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
}

.manage-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.manage-table {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;
}

:deep(.lost-cell__text) {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

:deep(.lost-cell__text strong) {
  font-size: 13px;
  color: $color-text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.lost-cell__text span) {
  font-size: 12px;
  color: $color-text-tertiary;
}
</style>
