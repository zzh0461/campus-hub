<script setup lang="ts">
import { computed, ref } from 'vue'
import { NAvatar } from 'naive-ui'

const props = withDefaults(
    defineProps<{
      avatar?: string
      name?: string
      size?: number
    }>(),
    {
      avatar: '',
      name: '?',
      size: 36,
    },
)

// 兜底后再取首字符：name 可能是 null（后端降级）、undefined（字段缺失）或空串，
// Vue 的 prop 默认值只对 undefined 生效，null 会穿透进来，直接 .slice 会抛 TypeError
const initial = computed(() => (props.name || '?').slice(0, 1))

// 图片加载失败时退回首字符样式。
// 注意：n-avatar 的默认插槽(文字)优先级高于 src——文字和 src 不能同时给，
// 否则图片永远不渲染，所以这里用 v-if 在"图片/文字"两种渲染间二选一
const failed = ref(false)
const showImage = computed(() => !!props.avatar && !failed.value)
</script>

<template>
  <n-avatar
    v-if="showImage"
    round
    :size="size"
    :src="avatar"
    @error="failed = true"
  />
  <n-avatar v-else round :size="size" class="user-avatar">
    {{ initial }}
  </n-avatar>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.user-avatar {
  background: $color-accent-soft;
  color: $color-accent;
  font-weight: 600;
  border: 1px solid rgba(22, 163, 74, 0.22);
}
</style>
