<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NDatePicker,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NTabs,
  NTabPane,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import { PhPlus, PhCalendarBlank, PhMapPin } from '@phosphor-icons/vue'
import {
  cancelActivityRegistration,
  createActivity,
  deleteActivity,
  getActivityCategories,
  getMyActivities,
  getMyPublishedActivities,
  updateActivity,
} from '@/api/activity'
import type { Activity, ActivityCategory, ActivityPublishParams } from '@/types/activity'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import { focusFirstError } from '@/utils/form'
import { usePagination } from '@/utils/usePagination'

const message = useMessage()
const authStore = useAuthStore()

/** 当前页签：joined=我参与的(报名的) / published=我发布的(主办的) */
const activeTab = ref<'joined' | 'published'>('joined')

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

const cancellingId = ref<number | null>(null)
const deletingId = ref<number | null>(null)
const categories = ref<ActivityCategory[]>([])

// ---------- 发布/编辑弹窗 ----------
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
  // 否则 async-validate 默认按 string 校验，数字值永远判为"未选择"（后台表单同款问题的教训）
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
  // 页签不同走不同接口：joined=我参与的 / published=我发布的
  await load((p) =>
    activeTab.value === 'joined' ? getMyActivities(p) : getMyPublishedActivities(p),
  )
}

function handleTabChange(value: string): void {
  activeTab.value = value as 'joined' | 'published'
  void reset(fetchList)
}

// ---------- 报名相关（"我参与的"页签） ----------
async function cancel(item: Activity): Promise<void> {
  cancellingId.value = item.id
  try {
    await cancelActivityRegistration(item.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((a) => a.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已取消报名')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    cancellingId.value = null
  }
}

// ---------- 发布/编辑/删除（"我发布的"页签） ----------
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
    // 主办方默认用当前用户昵称，替社团/组织办活动时可改
    organizer: authStore.user?.nickname ?? '',
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
    // 留空则后端自动取发布者昵称
    organizer: form.organizer.trim() || undefined,
  }
  submitting.value = true
  try {
    if (editing.value) {
      await updateActivity(editing.value.id, payload)
      message.success('活动已更新')
    } else {
      await createActivity(payload)
      message.success('活动已发布')
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
  deletingId.value = item.id
  try {
    await deleteActivity(item.id)
    if (!result.value) return
    result.value.records = result.value.records.filter((a) => a.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    message.success('已删除')
  } catch {
    // 错误提示由 request 层统一处理（已有人报名等业务拦截会在这里提示）
  } finally {
    deletingId.value = null
  }
}

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
  <page-container title="我的活动" subtitle="报名的活动和自己主办的活动都在这里">
    <template #extra>
      <n-button v-if="activeTab === 'published'" type="primary" @click="openCreate">
        <template #icon>
          <n-icon :component="PhPlus" />
        </template>
        发布活动
      </n-button>
    </template>

    <n-tabs :value="activeTab" type="line" animated @update:value="handleTabChange">
      <n-tab-pane name="joined" tab="我参与的" />
      <n-tab-pane name="published" tab="我发布的" />
    </n-tabs>

    <div v-if="loading">
      <loading-state :rows="5" variant="list" />
    </div>
    <div v-else-if="error" class="my-error">
      <empty-state
        title="活动加载失败"
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
        <router-link :to="`/activities/${item.id}`" class="my-item__cover">
          <img :src="item.cover" :alt="item.title" loading="lazy" />
          <div class="my-item__status">
            <status-tag :value="item.status" />
          </div>
        </router-link>
        <div class="my-item__main">
          <router-link :to="`/activities/${item.id}`" class="my-item__title">
            {{ item.title }}
          </router-link>
          <div class="my-item__meta">
            <span>
              <n-icon :size="14" :component="PhCalendarBlank" />
              {{ formatDateTime(item.startTime) }}
            </span>
            <span>
              <n-icon :size="14" :component="PhMapPin" />
              {{ item.location }}
            </span>
          </div>
          <span class="my-item__seats text-secondary">
            {{ activeTab === 'joined' ? '报名' : '已报名' }}
            <span class="num">{{ item.currentParticipants }}/{{ item.maxParticipants }}</span>
            人 · 主办方 {{ item.organizer }}
          </span>
        </div>
        <div class="my-item__actions" @click.stop>
          <!-- 我参与的：取消报名 -->
          <template v-if="activeTab === 'joined'">
            <n-button size="small" @click="$router.push(`/activities/${item.id}`)">查看详情</n-button>
            <n-popconfirm v-if="item.status !== 'FINISHED'" @positive-click="cancel(item)">
              <template #trigger>
                <n-button size="small" quaternary :loading="cancellingId === item.id">
                  取消报名
                </n-button>
              </template>
              确认取消报名?名额将立即释放。
            </n-popconfirm>
          </template>
          <!-- 我发布的：编辑/删除 -->
          <template v-else>
            <n-button size="small" @click="$router.push(`/activities/${item.id}`)">查看详情</n-button>
            <n-button size="small" @click="openEdit(item)">编辑</n-button>
            <n-popconfirm @positive-click="handleDelete(item)">
              <template #trigger>
                <n-button size="small" quaternary type="error" :loading="deletingId === item.id">
                  删除
                </n-button>
              </template>
              确认删除该活动?已有人报名时将无法删除。
            </n-popconfirm>
          </template>
        </div>
      </div>
    </div>
    <empty-state
      v-else-if="activeTab === 'joined'"
      title="还没有报名活动"
      description="去看看最近有哪些有趣的活动吧"
      action-text="浏览活动"
      @action="$router.push('/activities')"
    />
    <empty-state
      v-else
      title="还没有发布过活动"
      description="讲座、社团、运动赛事……你也可以成为主办方"
      action-text="发布第一个活动"
      @action="openCreate"
    />

    <pagination
      v-if="total > 0"
      v-model:page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)"
    />

    <!-- 发布/编辑活动弹窗（主办方自动取当前用户昵称，无需填写） -->
    <n-modal
      v-model:show="modalOpen"
      preset="card"
      :title="editing ? '编辑活动' : '发布活动'"
      style="width: 640px; max-width: 94vw"
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
        <div class="my-form-grid">
          <n-form-item path="categoryId" label="分类">
            <n-select
              v-model:value="form.categoryId"
              placeholder="请选择分类"
              :options="categories.map((c) => ({ label: c.name, value: c.id }))"
            />
          </n-form-item>
          <n-form-item path="maxParticipants" label="人数上限">
            <n-input-number v-model:value="form.maxParticipants" :min="1" :max="10000" class="my-form-full" />
          </n-form-item>
        </div>
        <n-form-item path="location" label="活动地点">
          <n-input v-model:value="form.location" placeholder="如:图书馆报告厅 / 东区操场" />
        </n-form-item>
        <n-form-item path="organizer" label="主办方">
          <n-input
            v-model:value="form.organizer"
            placeholder="默认用你的昵称,替社团/组织办活动可修改"
          />
        </n-form-item>
        <div class="my-form-grid">
          <n-form-item path="startTime" label="开始时间">
            <n-date-picker v-model:value="form.startTime" type="datetime" clearable class="my-form-full" />
          </n-form-item>
          <n-form-item path="endTime" label="结束时间">
            <n-date-picker v-model:value="form.endTime" type="datetime" clearable class="my-form-full" />
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
        <div class="my-modal-foot">
          <n-button @click="modalOpen = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editing ? '保存' : '发布' }}
          </n-button>
        </div>
      </template>
    </n-modal>
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

  &__cover {
    position: relative;
    width: 176px;
    height: 104px;
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

  &__status {
    position: absolute;
    top: 8px;
    left: 8px;
  }

  &__main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__title {
    font-size: 16px;
    font-weight: 650;
    color: $color-text;

    &:hover {
      color: $color-accent;
    }
  }

  &__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 13px;
    color: $color-text-secondary;

    span {
      display: inline-flex;
      align-items: center;
      gap: 5px;
    }
  }

  &__seats {
    font-size: 12px;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  @media (max-width: 640px) {
    flex-wrap: wrap;

    &__cover {
      width: 100%;
      height: 160px;
    }

    &__actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}

.my-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.my-form-full {
  width: 100%;
}

.my-modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
