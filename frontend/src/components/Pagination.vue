<script setup lang="ts">
import { NPagination } from 'naive-ui'
import { PAGE_SIZE_OPTIONS } from '@/utils/usePagination'

const props = defineProps<{
  pageNum: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  (e: 'update:pageNum', value: number): void
  (e: 'update:pageSize', value: number): void
}>()

/** 页码变化：只在真的变了才抛出去，避免重复请求同一页 */
function handleUpdatePage(value: number): void {
  if (value !== props.pageNum) emit('update:pageNum', value)
}

/**
 * 换每页条数：调用方（列表页）负责重置页码并重新拉取，
 * 这里只把变更抛出去，避免同时改 pageNum / pageSize 触发两次请求。
 */
function handleUpdatePageSize(value: number): void {
  if (value !== props.pageSize) emit('update:pageSize', value)
}
</script>

<template>
  <div class="pagination-bar">
    <span class="pagination-bar__total text-tertiary">共 {{ total }} 条</span>
    <n-pagination
      :page="pageNum"
      :page-size="pageSize"
      :item-count="total"
      :page-sizes="PAGE_SIZE_OPTIONS"
      show-size-picker
      @update:page="handleUpdatePage"
      @update:page-size="handleUpdatePageSize"
    />
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 28px;
  flex-wrap: wrap;

  &__total {
    font-size: 13px;
  }
}
</style>
