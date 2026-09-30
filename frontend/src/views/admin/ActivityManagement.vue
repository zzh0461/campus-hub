<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  type DataTableColumns,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import { PhPlus, PhMagnifyingGlass } from '@phosphor-icons/vue'
import {
  createAdminActivity,
  deleteAdminActivity,
  getAdminActivities,
  updateAdminActivity,
} from '@/api/admin'
import { getActivityCategories } from '@/api/activity'
import type { Activity, ActivityCategory, ActivityPublishParams } from '@/types/activity'
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
} = usePagination<Activity>()

const keyword = ref('')
const categories = ref<ActivityCategory[]>([])

const modalOpen = ref(false)
const editing = ref<Activity | null>(null)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)
// 图片上传组件：提交成功后调用 commit() 标记"本次上传的图已被采用"，取消时组件才能放心清理
const uploadRef = ref<{ commit: () => void } | null>(null)

const form = reactive({
  title: '',
  description: '',
  categoryId: null as number | null,
  location: '',
  startTime: null as number | null,
  endTime: null as number | null,
  maxParticipants: null as number | null,
  cover: '',
  organizer: '',
})

const coverList = computed({
  get: () => (form.cover ? [form.cover] : []),
  set: (value: string[]) => {
    form.cover = value[0] ?? ''
  },
})

const rules: FormRules = {
  title: { required: true, max: 60, message: '请输入活动标题', trigger: ['input', 'blur'] },
  // NSelect/NDatePicker 绑定的是 number(分类ID/时间戳)，规则必须声明 type: 'number'，
  // 否则 async-validate 默认按 string 校验，数字值永远判为"未选择"
  categoryId: { required: true, type: 'number', message: '请选择活动分类', trigger: ['change'] },
  location: { required: true, message: '请填写活动地点', trigger: ['input', 'blur'] },
  startTime: { required: true, type: 'number', message: '请选择开始时间', trigger: ['change'] },
  endTime: { required: true, type: 'number', message: '请选择结束时间', trigger: ['change'] },
  maxParticipants: {
    required: true,
    type: 'number',
    message: '请填写人数上限',
    trigger: ['input', 'blur'],
  },
  description: { required: true, min: 20, message: '活动介绍至少 20 个字', trigger: ['input', 'blur'] },
}

async function fetchList(): Promise<void> {
  await load((p) => getAdminActivities({ ...p, keyword: keyword.value.trim() || undefined }))
}

/** 搜索:关键词变了必须回到第 1 页,否则会留在越界页码上搜不到东西 */
async function handleSearch(): Promise<void> {
  await reset(fetchList)
}

function openCreate(): void {
  editing.value = null
  Object.assign(form, {
    title: '',
    description: '',
    categoryId: null,
    location: '',
    startTime: null,
    endTime: null,
    maxParticipants: null,
    cover: '',
    organizer: '',
  })
  modalOpen.value = true
}

function openEdit(item: Activity): void {
  editing.value = item
  Object.assign(form, {
    title: item.title,
    description: item.description,
    categoryId: item.categoryId,
    location: item.location,
    startTime: new Date(item.startTime).getTime(),
    endTime: new Date(item.endTime).getTime(),
    maxParticipants: item.maxParticipants,
    cover: item.cover,
    organizer: item.organizer,
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
  const payload: ActivityPublishParams = {
    title: form.title.trim(),
    description: form.description.trim(),
    categoryId: form.categoryId as number,
    location: form.location.trim(),
    startTime: new Date(form.startTime as number).toISOString(),
    endTime: new Date(form.endTime as number).toISOString(),
    maxParticipants: form.maxParticipants as number,
    cover: form.cover,
    organizer: form.organizer.trim() || undefined,
  }
  submitting.value = true
  try {
    if (editing.value) {
      await updateAdminActivity(editing.value.id, payload)
      message.success('活动已更新')
    } else {
      await createAdminActivity(payload)
      message.success('活动已创建')
    }
    uploadRef.value?.commit()
    modalOpen.value = false
    void fetchList()
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item: Activity): Promise<void> {
  try {
    await deleteAdminActivity(item.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((a) => a.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

const columns: DataTableColumns<Activity> = [
  {
    title: '活动',
    key: 'activity',
    width: 280,
    render: (row) =>
      h('div', { class: 'activity-cell' }, [
        h('img', { class: 'activity-cell__img', src: row.cover, alt: row.title, loading: 'lazy' }),
        h('div', { class: 'activity-cell__text' }, [
          h('strong', row.title),
          h('span', row.categoryName),
        ]),
      ]),
  },
  { title: '地点', key: 'location', width: 160, ellipsis: { tooltip: true } },
  {
    title: '开始时间',
    key: 'startTime',
    width: 170,
    render: (row) => h('span', { class: 'num' }, formatDateTime(row.startTime)),
  },
  {
    title: '名额',
    key: 'participants',
    width: 120,
    render: (row) =>
      h('span', { class: 'num text-secondary' }, `${row.currentParticipants}/${row.maxParticipants}`),
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h('span', null, row.status === 'UPCOMING' ? '即将开始' : row.status === 'ONGOING' ? '进行中' : '已结束'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row) =>
      h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            {
              trigger: () =>
                h(NButton, { size: 'small', type: 'error', ghost: true }, { default: () => '删除' }),
              default: () => '删除后不可恢复,确认删除该活动?',
            },
          ),
        ],
      }),
  },
]

bind(fetchList)

onMounted(async () => {
  try {
    categories.value = await getActivityCategories()
  } catch {
    categories.value = []
  }
})
</script>

<template>
  <div class="manage-page">
    <div class="manage-toolbar">
      <n-input
        v-model:value="keyword"
        placeholder="搜索活动标题"
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
        新建活动
      </n-button>
    </div>

    <div v-if="loading" class="manage-loading">
      <loading-state :rows="8" variant="panel" />
    </div>
    <div v-else-if="error" class="manage-error">
      <empty-state
        title="活动列表加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <n-data-table
      v-else
      :columns="columns"
      :data="result?.records ?? []"
      :row-key="(row: Activity) => row.id"
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
      :title="editing ? '编辑活动' : '新建活动'"
      style="width: 680px; max-width: 94vw"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item path="title" label="活动标题">
          <n-input v-model:value="form.title" :maxlength="60" show-count />
        </n-form-item>
        <div class="manage-form-grid">
          <n-form-item path="categoryId" label="分类">
            <n-select
              v-model:value="form.categoryId"
              placeholder="请选择分类"
              :options="categories.map((c) => ({ label: c.name, value: c.id }))"
            />
          </n-form-item>
          <n-form-item path="maxParticipants" label="人数上限">
            <n-input-number v-model:value="form.maxParticipants" :min="1" :max="10000" class="manage-form-full" />
          </n-form-item>
        </div>
        <n-form-item path="location" label="活动地点">
          <n-input v-model:value="form.location" placeholder="如:图书馆报告厅 / 东区操场" />
        </n-form-item>
        <n-form-item path="organizer" label="主办方">
          <n-input
            v-model:value="form.organizer"
            placeholder="如:校团委·学生会;留空则署名「平台管理员」"
          />
        </n-form-item>
        <div class="manage-form-grid">
          <n-form-item path="startTime" label="开始时间">
            <n-date-picker v-model:value="form.startTime" type="datetime" clearable class="manage-form-full" />
          </n-form-item>
          <n-form-item path="endTime" label="结束时间">
            <n-date-picker v-model:value="form.endTime" type="datetime" clearable class="manage-form-full" />
          </n-form-item>
        </div>
        <n-form-item path="description" label="活动介绍">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="5"
            placeholder="活动背景、流程、注意事项等,至少 20 个字"
          />
        </n-form-item>
        <n-form-item label="封面图(选填)">
          <image-upload ref="uploadRef" v-model="coverList" :max="1" tip="建议 16:9 横图,仅上传一张" />
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

.manage-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.manage-form-full {
  width: 100%;
}

.manage-modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.activity-cell) {
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.activity-cell__img) {
  width: 72px;
  height: 44px;
  flex-shrink: 0;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid $color-border;
}

:deep(.activity-cell__text) {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

:deep(.activity-cell__text strong) {
  font-size: 13px;
  color: $color-text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.activity-cell__text span) {
  font-size: 12px;
  color: $color-text-tertiary;
}
</style>
