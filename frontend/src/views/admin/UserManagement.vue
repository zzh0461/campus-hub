<script setup lang="ts">
import { h, ref } from 'vue'
import {
  NAvatar,
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
import { deleteUser, getAdminUsers, updateUserStatus } from '@/api/admin'
import type { User, UserStatus } from '@/types/user'
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
} = usePagination<User>()

const keyword = ref('')

async function fetchList(): Promise<void> {
  await load((p) => getAdminUsers({ ...p, keyword: keyword.value.trim() || undefined }))
}

/** 搜索:关键词变了必须回到第 1 页,否则会留在越界页码上搜不到东西 */
async function handleSearch(): Promise<void> {
  await reset(fetchList)
}

async function toggleStatus(user: User): Promise<void> {
  const next: UserStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    const updated = await updateUserStatus(user.id, next)
    Object.assign(user, updated)
    message.success(next === 'ACTIVE' ? '已启用该用户' : '已禁用该用户')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function handleDelete(user: User): Promise<void> {
  try {
    await deleteUser(user.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((u) => u.id !== user.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除该用户')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

const columns: DataTableColumns<User> = [
  {
    title: '用户',
    key: 'user',
    width: 220,
    render: (row) =>
      h('div', { class: 'user-cell' }, [
        h(NAvatar, {
          round: true,
          size: 34,
          src: row.avatar || undefined,
        }, { default: () => row.nickname.slice(0, 1) }),
        h('div', { class: 'user-cell__text' }, [
          h('strong', row.nickname),
          h('span', `@${row.username}`),
        ]),
      ]),
  },
  { title: '手机号', key: 'phone', width: 140, render: (row) => h('span', { class: 'num' }, row.phone) },
  {
    title: '角色',
    key: 'role',
    width: 110,
    render: (row) => h('span', null, [h('span', null, row.role === 'ADMIN' ? '管理员' : '普通用户')]),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h('span', null, [h('span', null, row.status === 'ACTIVE' ? '正常' : '已禁用')]),
  },
  {
    title: '注册时间',
    key: 'createdAt',
    width: 170,
    render: (row) => h('span', { class: 'num' }, formatDateTime(row.createdAt)),
  },
  {
    title: '操作',
    key: 'actions',
    width: 170,
    render: (row) =>
      h(NSpace, { size: 4 }, {
        default: () => [
          row.role !== 'ADMIN'
            ? h(
                NPopconfirm,
                {
                  onPositiveClick: () => toggleStatus(row),
                },
                {
                  trigger: () =>
                    h(
                      NButton,
                      { size: 'small', type: row.status === 'ACTIVE' ? 'warning' : 'success', ghost: true },
                      { default: () => (row.status === 'ACTIVE' ? '禁用' : '启用') },
                    ),
                  default: () =>
                    row.status === 'ACTIVE'
                      ? '禁用后该用户将无法登录,确认继续?'
                      : '确认恢复该用户的正常访问?',
                },
              )
            : null,
          row.role !== 'ADMIN'
            ? h(
                NPopconfirm,
                {
                  onPositiveClick: () => handleDelete(row),
                },
                {
                  trigger: () =>
                    h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' }),
                  default: () => '删除后不可恢复,确认删除该用户?',
                },
              )
            : null,
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
        placeholder="搜索用户名 / 昵称 / 手机号"
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
        title="用户列表加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <n-data-table
      v-else
      :columns="columns"
      :data="result?.records ?? []"
      :row-key="(row: User) => row.id"
      :bordered="false"
      :loading="false"
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

:deep(.user-cell) {
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.user-cell__text) {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

:deep(.user-cell__text strong) {
  font-size: 13px;
  color: $color-text;
}

:deep(.user-cell__text span) {
  font-size: 12px;
  color: $color-text-tertiary;
}
</style>
