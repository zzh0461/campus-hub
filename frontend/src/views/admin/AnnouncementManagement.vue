<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NSwitch,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import { PhPlus, PhMagnifyingGlass } from '@phosphor-icons/vue'
import {
  createAnnouncement,
  deleteAnnouncement,
  getAdminAnnouncements,
  updateAnnouncement,
} from '@/api/admin'
import type { Announcement, AnnouncementPublishParams } from '@/types/announcement'
import { formatDateTime } from '@/utils/format'
import { focusFirstError } from '@/utils/form'
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
} = usePagination<Announcement>()

const keyword = ref('')

const modalOpen = ref(false)
const editing = ref<Announcement | null>(null)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)

const form = reactive<AnnouncementPublishParams>({
  title: '',
  content: '',
  category: '',
  published: true,
})

const categoryOptions = [
  { label: '通知公告', value: '通知公告' },
  { label: '教务信息', value: '教务信息' },
  { label: '后勤服务', value: '后勤服务' },
  { label: '校园活动', value: '校园活动' },
]

const rules: FormRules = {
  title: { required: true, max: 60, message: '请输入公告标题', trigger: ['input', 'blur'] },
  category: { required: true, message: '请选择公告分类', trigger: ['change'] },
  content: { required: true, min: 20, message: '公告内容至少 20 个字', trigger: ['input', 'blur'] },
}

async function fetchList(): Promise<void> {
  await load((p) => getAdminAnnouncements({ ...p, keyword: keyword.value.trim() || undefined }))
}

/** 搜索:关键词变了必须回到第 1 页,否则会留在越界页码上搜不到东西 */
async function handleSearch(): Promise<void> {
  await reset(fetchList)
}

function openCreate(): void {
  editing.value = null
  Object.assign(form, { title: '', content: '', category: '', published: true })
  modalOpen.value = true
}

function openEdit(item: Announcement): void {
  editing.value = item
  Object.assign(form, {
    title: item.title,
    content: item.content,
    category: item.category,
    published: item.published,
  })
  modalOpen.value = true
}

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    // 出错项可能在视口外,滚过去再返回,避免用户以为「点了没反应」
    void focusFirstError(formRef.value)
    return
  }
  submitting.value = true
  try {
    if (editing.value) {
      await updateAnnouncement(editing.value.id, {
        title: form.title.trim(),
        content: form.content.trim(),
        category: form.category,
        published: form.published,
      })
      message.success('公告已更新')
    } else {
      await createAnnouncement({
        title: form.title.trim(),
        content: form.content.trim(),
        category: form.category,
        published: form.published,
      })
      message.success('公告已发布')
    }
    modalOpen.value = false
    void fetchList()
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item: Announcement): Promise<void> {
  try {
    await deleteAnnouncement(item.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((a) => a.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

const columns: DataTableColumns<Announcement> = [
  {
    title: '标题',
    key: 'title',
    ellipsis: { tooltip: true },
    render: (row) => h('span', { class: 'ann-title' }, row.title),
  },
  { title: '分类', key: 'category', width: 120, render: (row) => h('span', row.category) },
  { title: '作者', key: 'author', width: 110 },
  {
    title: '发布时间',
    key: 'createdAt',
    width: 170,
    render: (row) => h('span', { class: 'num' }, formatDateTime(row.createdAt)),
  },
  {
    title: '浏览',
    key: 'viewCount',
    width: 90,
    render: (row) => h('span', { class: 'num' }, row.viewCount),
  },
  {
    title: '发布状态',
    key: 'published',
    width: 110,
    render: (row) => h('span', null, row.published ? '已发布' : '未发布'),
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
            { size: 'small', onClick: () => openEdit(row) },
            { default: () => '编辑' },
          ),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' }),
              default: () => '确认删除这条公告?',
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
        placeholder="搜索公告标题"
        clearable
        class="manage-toolbar__search"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <n-icon :component="PhMagnifyingGlass" />
        </template>
      </n-input>
      <n-button type="primary" @click="handleSearch">搜索</n-button>
      <n-button type="primary" @click="openCreate">
        <template #icon>
          <n-icon :component="PhPlus" />
        </template>
        新建公告
      </n-button>
    </div>

    <div v-if="loading" class="manage-loading">
      <loading-state :rows="8" variant="panel" />
    </div>
    <div v-else-if="error" class="manage-error">
      <empty-state
        title="公告列表加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <n-data-table
      v-else
      :columns="columns"
      :data="result?.records ?? []"
      :row-key="(row: Announcement) => row.id"
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

    <n-modal
      v-model:show="modalOpen"
      preset="card"
      :title="editing ? '编辑公告' : '新建公告'"
      style="width: 640px; max-width: 94vw"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item path="title" label="标题">
          <n-input v-model:value="form.title" :maxlength="60" show-count />
        </n-form-item>
        <n-form-item path="category" label="分类">
          <n-select v-model:value="form.category" :options="categoryOptions" />
        </n-form-item>
        <n-form-item path="content" label="内容">
          <n-input
            v-model:value="form.content"
            type="textarea"
            :rows="8"
            placeholder="支持换行排版,多段内容请用空行分隔"
          />
        </n-form-item>
        <n-form-item label="立即发布">
          <n-switch v-model:value="form.published" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="manage-modal-foot">
          <n-button @click="modalOpen = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.manage-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 420px) auto auto;
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

.manage-modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.ann-title) {
  font-weight: 600;
  color: $color-text;
}
</style>
