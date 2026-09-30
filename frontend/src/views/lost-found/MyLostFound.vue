<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NIcon,
  NPopconfirm,
  NTabs,
  NTabPane,
  useMessage,
} from 'naive-ui'
import {
  PhPlus,
  PhCheckCircle,
  PhMapPin,
  PhArrowClockwise,
  PhMagnifyingGlass,
  PhTrash,
} from '@phosphor-icons/vue'
import { deleteLostFound, getMyLostFoundList, updateLostFoundStatus } from '@/api/lostFound'
import type { LostFoundItem, LostFoundStatus, LostFoundType } from '@/types/lostFound'
import { formatRelative } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

const router = useRouter()
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

const type = ref<LostFoundType | 'ALL'>('ALL')
const deletingId = ref<number | null>(null)
const updatingId = ref<number | null>(null)

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '失物', value: 'LOST' },
  { label: '招领', value: 'FOUND' },
]

async function fetchList(): Promise<void> {
  await load((p) =>
    getMyLostFoundList({
      ...p,
      type: type.value === 'ALL' ? undefined : type.value,
    }),
  )
}

async function remove(item: LostFoundItem): Promise<void> {
  deletingId.value = item.id
  try {
    await deleteLostFound(item.id)
    if (result.value) {
      result.value.records = result.value.records.filter((a) => a.id !== item.id)
      total.value = Math.max(0, total.value - 1)
    }
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    deletingId.value = null
  }
}

async function toggleStatus(item: LostFoundItem, status: LostFoundStatus): Promise<void> {
  updatingId.value = item.id
  try {
    const updated = await updateLostFoundStatus(item.id, status)
    // 就地刷新该条状态标签，不必整页重拉
    const target = result.value?.records.find((a) => a.id === item.id)
    if (target) target.status = updated.status
    message.success(status === 'RESOLVED' ? '已标记为已解决' : '已重新打开')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    updatingId.value = null
  }
}

function handleTabChange(value: string): void {
  type.value = value as LostFoundType | 'ALL'
  void reset(fetchList)
}

bind(fetchList)
</script>

<template>
  <page-container title="我的失物招领" subtitle="你发布过的失物与招领信息都在这里">
    <template #extra>
      <n-button type="primary" @click="router.push('/lost-found/publish')">
        <template #icon>
          <n-icon :component="PhPlus" />
        </template>
        发布信息
        </n-button>
    </template>

    <n-tabs :value="type" type="line" animated @update:value="handleTabChange">
      <n-tab-pane v-for="tab in tabs" :key="tab.value" :name="tab.value" :tab="tab.label" />
    </n-tabs>

    <div v-if="loading">
      <loading-state :rows="5" variant="list" />
    </div>
    <div v-else-if="error" class="my-error">
      <empty-state
        title="信息加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <div v-else-if="result?.records.length" class="my-list">
      <div
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        class="my-item panel stagger-item"
        :style="{ '--i': index }"
      >
        <router-link :to="`/lost-found/${item.id}`" class="my-item__thumb">
          <img v-if="item.images[0]" :src="item.images[0]" :alt="item.title" loading="lazy" />
          <div v-else class="my-item__placeholder">
            <n-icon :size="24" :component="PhMagnifyingGlass" />
          </div>
          <div class="my-item__type">
            <status-tag :value="item.type" />
          </div>
        </router-link>
        <div class="my-item__main">
          <router-link :to="`/lost-found/${item.id}`" class="my-item__title">
            {{ item.title }}
          </router-link>
          <p class="my-item__desc">{{ item.description?.slice(0, 60) }}…</p>
          <div class="my-item__meta">
            <span>
              <n-icon :size="14" :component="PhMapPin" />
              {{ item.location }}
            </span>
            <span class="text-tertiary">{{ formatRelative(item.createdAt) }}</span>
            <status-tag :value="item.status" />
          </div>
        </div>
        <div class="my-item__actions">
          <n-button size="small" @click="router.push(`/lost-found/${item.id}`)">查看详情</n-button>
          <n-popconfirm
            @positive-click="toggleStatus(item, item.status === 'OPEN' ? 'RESOLVED' : 'OPEN')"
          >
            <template #trigger>
              <n-button size="small" quaternary :loading="updatingId === item.id">
                <template #icon>
                  <n-icon :component="item.status === 'OPEN' ? PhCheckCircle : PhArrowClockwise" />
                </template>
                {{ item.status === 'OPEN' ? '标记已解决' : '重新打开' }}
              </n-button>
            </template>
            {{ item.status === 'OPEN' ? '确认标记为已解决?表示失物已找回或招领已被认领。' : '确认重新打开?表示该信息再次生效。' }}
          </n-popconfirm>
          <n-popconfirm @positive-click="remove(item)">
            <template #trigger>
              <n-button size="small" quaternary :loading="deletingId === item.id">
                <template #icon>
                  <n-icon :component="PhTrash" />
                </template>
                删除
              </n-button>
            </template>
            确认删除这条信息?删除后不可恢复。
          </n-popconfirm>
        </div>
      </div>
    </div>
    <empty-state
      v-else
      title="还没有发布过失物招领"
      description="丢了东西或拾到宝贝,发一条信息让它更快回到主人身边"
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

.my-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.my-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.my-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 14px;
  box-shadow: none;

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
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &:hover {
      color: $color-accent;
    }
  }

  &__desc {
    margin: 0;
    font-size: 13px;
    color: $color-text-secondary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 14px;
    font-size: 12px;
    color: $color-text-secondary;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  @media (max-width: 640px) {
    flex-wrap: wrap;

    &__thumb {
      width: 100%;
      height: 160px;
    }

    &__actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
