<script setup lang="ts">
import { computed } from 'vue'
import { NTag } from 'naive-ui'

type TagType = 'default' | 'success' | 'warning' | 'error' | 'info' | 'primary'

const props = defineProps<{
  value: string
  domain?: 'product' | 'activity' | 'lostFound' | 'user' | 'notification'
}>()

const map: Record<string, { label: string; type: TagType }> = {
  // 商品
  ON_SALE: { label: '在售', type: 'success' },
  OFF_SHELF: { label: '已下架', type: 'default' },
  SOLD: { label: '已售出', type: 'warning' },
  // 活动
  UPCOMING: { label: '即将开始', type: 'success' },
  ONGOING: { label: '进行中', type: 'primary' },
  FINISHED: { label: '已结束', type: 'default' },
  // 失物招领
  LOST: { label: '失物', type: 'warning' },
  FOUND: { label: '招领', type: 'success' },
  OPEN: { label: '进行中', type: 'primary' },
  RESOLVED: { label: '已解决', type: 'default' },
  // 用户
  ACTIVE: { label: '正常', type: 'success' },
  DISABLED: { label: '已禁用', type: 'error' },
  USER: { label: '普通用户', type: 'default' },
  ADMIN: { label: '管理员', type: 'warning' },
  // 通知
  SYSTEM: { label: '系统', type: 'default' },
  MARKET: { label: '市场', type: 'primary' },
  ACTIVITY: { label: '活动', type: 'success' },
  LOST_FOUND: { label: '失物', type: 'warning' },
  // 通用
  PUBLISHED: { label: '已发布', type: 'success' },
  UNPUBLISHED: { label: '未发布', type: 'default' },
}

const display = computed<TagType>(() => map[props.value]?.type ?? 'default')
const label = computed(() => map[props.value]?.label ?? props.value)
</script>

<template>
  <n-tag :type="display" size="small" :bordered="false" round>
    {{ label }}
  </n-tag>
</template>
