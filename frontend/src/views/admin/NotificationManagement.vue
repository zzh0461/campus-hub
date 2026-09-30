<script setup lang="ts">
import { h, ref } from 'vue'
import {
  NButton,
  NDataTable,
  NIcon,
  NPopconfirm,
  NSpace,
  type DataTableColumns,
  useMessage,
} from 'naive-ui'
import { PhChecks } from '@phosphor-icons/vue'
import {
  deleteNotification,
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '@/api/notification'
import type { Notification } from '@/types/notification'
import { formatDateTime } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

// 注意:后端未提供 /api/admin/notifications 专用接口,
// 本页暂时复用用户侧通知接口(见 README 说明),后端就绪后可替换为管理接口。

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
  bind,
} = usePagination<Notification>()

const detailShow = ref(false)
const detailItem = ref<Notification | null>(null)

async function fetchList(): Promise<void> {
  await load((p) => getNotifications(p))
}

/** 打开通知详情弹窗 */
function openDetail(item: Notification): void {
  detailItem.value = item
  detailShow.value = true
}

/** 详情弹窗打开后未读自动标记已读 */
async function handleDetailRead(item: Notification): Promise<void> {
  if (item.read) return
  try {
    await markNotificationRead(item.id)
    item.read = true
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleRead(item: Notification): Promise<void> {
  if (item.read) return
  try {
    await markNotificationRead(item.id)
    item.read = true
    message.success('已标记为已读')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleReadAll(): Promise<void> {
  try {
    await markAllNotificationsRead()
    if (!result.value) return
    result.value.records.forEach((item) => {
      item.read = true
    })
    message.success('全部标记为已读')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleDelete(item: Notification): Promise<void> {
  try {
    await deleteNotification(item.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((n) => n.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

const columns: DataTableColumns<Notification> = [
  {
    title: '标题',
    key: 'title',
    width: 220,
    render: (row) => h('span', { class: 'notice-title' }, row.title),
  },
  {
    title: '类型',
    key: 'type',
    width: 100,
    render: (row) =>
      h(
        'span',
        null,
        row.type === 'SYSTEM'
          ? '系统'
          : row.type === 'MARKET'
            ? '市场'
            : row.type === 'ACTIVITY'
              ? '活动'
              : '失物',
      ),
  },
  { title: '内容', key: 'content', ellipsis: { tooltip: true } },
  {
    title: '时间',
    key: 'createdAt',
    width: 170,
    render: (row) => h('span', { class: 'num' }, formatDateTime(row.createdAt)),
  },
  {
    title: '状态',
    key: 'read',
    width: 90,
    render: (row) => h('span', null, row.read ? '已读' : '未读'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row) =>
      h(NSpace, { size: 4 }, {
        default: () => [
          h(
            NButton,
            { size: 'small', onClick: () => openDetail(row) },
            { default: () => '查看详情' },
          ),
          row.read
            ? null
            : h(
                NButton,
                { size: 'small', onClick: () => handleRead(row) },
                { default: () => '标为已读' },
              ),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' }),
              default: () => '确认删除这条通知?',
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
      <div class="manage-toolbar__hint text-tertiary">系统内所有用户的站内通知</div>
      <n-button type="primary" @click="handleReadAll">
        <template #icon>
          <n-icon :component="PhChecks" />
        </template>
        全部已读
      </n-button>
    </div>

    <div v-if="loading" class="manage-loading">
      <loading-state :rows="8" variant="panel" />
    </div>
    <div v-else-if="error" class="manage-error">
      <empty-state
        title="通知列表加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <n-data-table
      v-else
      :columns="columns"
      :data="result?.records ?? []"
      :row-key="(row: Notification) => row.id"
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

    <!-- 通知详情弹窗：未读打开自动标记已读 -->
    <notification-detail-modal
      v-model:show="detailShow"
      :notification="detailItem"
      @read="handleDetailRead"
    />
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.manage-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;

  &__hint {
    font-size: 13px;
  }
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

:deep(.notice-title) {
  font-weight: 600;
  color: $color-text;
}
</style>
