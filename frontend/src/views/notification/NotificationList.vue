<script setup lang="ts">
import { ref } from 'vue'
import {
  NButton,
  NIcon,
  NPopconfirm,
  NTabs,
  NTabPane,
  useMessage,
} from 'naive-ui'
import { PhChecks, PhTrash } from '@phosphor-icons/vue'
import {
  deleteNotification,
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '@/api/notification'
import type { Notification } from '@/types/notification'
import { formatDateTime } from '@/utils/format'
import { emitNotificationsChanged } from '@/utils/events'
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
} = usePagination<Notification>()

const readFilter = ref<'all' | 'unread'>('all')
const detailShow = ref(false)
const detailItem = ref<Notification | null>(null)

async function fetchList(): Promise<void> {
  await load((p) =>
    getNotifications({
      ...p,
      read: readFilter.value === 'unread' ? false : undefined,
    }),
  )
}

function handleTabChange(value: string): void {
  readFilter.value = value as 'all' | 'unread'
  void reset(fetchList)
}

/** 点击卡片查看详情：弹出全文弹窗 */
function openDetail(item: Notification): void {
  detailItem.value = item
  detailShow.value = true
}

/** 详情弹窗打开后自动标记已读（未读时由弹窗回调），并同步角标 */
async function handleDetailRead(item: Notification): Promise<void> {
  if (item.read) return
  try {
    await markNotificationRead(item.id)
    item.read = true
    emitNotificationsChanged()
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleRead(item: Notification): Promise<void> {
  if (item.read) return
  try {
    await markNotificationRead(item.id)
    item.read = true
    emitNotificationsChanged()
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleReadAll(): Promise<void> {
  try {
    await markAllNotificationsRead()
    if (result.value) {
      result.value.records.forEach((item) => {
        item.read = true
      })
    }
    emitNotificationsChanged()
    message.success('全部标记为已读')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleDelete(item: Notification): Promise<void> {
  try {
    await deleteNotification(item.id)
    if (result.value) {
      result.value.records = result.value.records.filter((n) => n.id !== item.id)
      total.value = Math.max(0, total.value - 1)
    }
    // 删掉的如果是未读，角标数字也要跟着变
    emitNotificationsChanged()
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

bind(fetchList)
</script>

<template>
  <page-container title="通知中心" subtitle="交易动态、活动提醒与系统消息">
    <template #extra>
      <n-button @click="handleReadAll">
        <template #icon>
          <n-icon :component="PhChecks" />
        </template>
        全部已读
      </n-button>
    </template>

    <n-tabs :value="readFilter" type="line" animated @update:value="handleTabChange">
      <n-tab-pane name="all" tab="全部通知" />
      <n-tab-pane name="unread" tab="未读" />
    </n-tabs>

    <div v-if="loading">
      <loading-state :rows="6" variant="list" />
    </div>
    <div v-else-if="error" class="notice-error">
      <empty-state
        title="通知加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <div v-else-if="result?.records.length" class="notice-list">
      <div
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        class="notice-item panel stagger-item"
        :class="{ 'notice-item--unread': !item.read }"
        :style="{ '--i': index }"
        @click="openDetail(item)"
      >
        <div class="notice-item__icon">
          <status-tag :value="item.type" />
          <span v-if="!item.read" class="notice-item__dot" aria-label="未读" />
        </div>
        <div class="notice-item__main">
          <div class="notice-item__head">
            <h3 class="notice-item__title">{{ item.title }}</h3>
            <span class="notice-item__time text-tertiary">
              {{ formatDateTime(item.createdAt) }}
            </span>
          </div>
          <p class="notice-item__content">{{ item.content }}</p>
        </div>
        <div class="notice-item__actions" @click.stop>
          <n-button size="tiny" quaternary @click="openDetail(item)">
            查看详情
          </n-button>
          <n-button
            v-if="!item.read"
            size="tiny"
            quaternary
            @click="handleRead(item)"
          >
            标为已读
          </n-button>
          <n-popconfirm @positive-click="handleDelete(item)">
            <template #trigger>
              <n-button size="tiny" quaternary type="error">
                <template #icon>
                  <n-icon :component="PhTrash" />
                </template>
                删除
              </n-button>
            </template>
            确认删除这条通知?
          </n-popconfirm>
        </div>
      </div>
    </div>
    <empty-state
      v-else
      title="暂无通知"
      description="报名活动、发布商品后,动态会第一时间通知你"
    />

    <pagination
      v-if="total > 0"
      v-model:page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)"
    />

    <!-- 通知详情弹窗：点击卡片/查看详情打开，未读自动标记已读 -->
    <notification-detail-modal
      v-model:show="detailShow"
      :notification="detailItem"
      @read="handleDetailRead"
    />
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.notice-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 18px;
  box-shadow: none;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    border-color: rgba(22, 163, 74, 0.35);
  }

  &--unread {
    border-left: 3px solid $color-accent;
    background: rgba(22, 163, 74, 0.025);
  }

  &__icon {
    position: relative;
    flex-shrink: 0;
    padding-top: 2px;
  }

  &__dot {
    position: absolute;
    top: -2px;
    right: -6px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: $color-danger;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 14px;
  }

  &__title {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: $color-text;
  }

  &__time {
    flex-shrink: 0;
    font-size: 12px;
  }

  &__content {
    margin: 5px 0 0;
    font-size: 13px;
    color: $color-text-secondary;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__actions {
    display: flex;
    gap: 4px;
    flex-shrink: 0;
    padding-top: 2px;
  }

  @media (max-width: 640px) {
    flex-wrap: wrap;

    &__actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
