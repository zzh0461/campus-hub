<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import type { User } from '@/types/user'
import { formatDate } from '@/utils/format'
import { focusFirstError } from '@/utils/form'

const message = useMessage()
const authStore = useAuthStore()
const userStore = useUserStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(true)
const error = ref(false)
const saving = ref(false)
// 图片上传组件：保存成功后调用 commit() 标记"本次上传的头像已被采用"，离开页面时组件才能放心清理
const uploadRef = ref<{ commit: () => void } | null>(null)

const form = reactive({
  nickname: '',
  phone: '',
  avatar: '',
  bio: '',
})

const profile = computed<User | null>(() => userStore.profile ?? authStore.user)

const avatarList = computed({
  get: () => (form.avatar ? [form.avatar] : []),
  set: (value: string[]) => {
    form.avatar = value[0] ?? ''
  },
})

const rules: FormRules = {
  nickname: { required: true, message: '请输入昵称', trigger: ['input', 'blur'] },
  // 必填与格式拆成两条:带自定义 validator 的规则会让同一对象上的 required 失效
  phone: [
    { required: true, message: '请输入手机号', trigger: ['input', 'blur'] },
    {
      key: 'phone-format',
      validator: (_rule, value: string) => {
        if (!value) return Promise.resolve()
        if (!/^1\d{10}$/.test(value.replace(/\s/g, ''))) {
          return Promise.reject(new Error('请输入 11 位有效手机号'))
        }
        return Promise.resolve()
      },
      trigger: ['blur'],
    },
  ],
  bio: { max: 120, message: '简介不超过 120 字', trigger: ['input', 'blur'] },
}

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const user = await userStore.fetchProfile(true)
    form.nickname = user.nickname
    form.phone = user.phone
    form.avatar = user.avatar
    form.bio = user.bio
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function handleSave(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    // 出错项可能在视口外,滚过去再返回,避免用户以为「点了没反应」
    void focusFirstError(formRef.value)
    return
  }
  saving.value = true
  try {
    await userStore.saveProfile({
      nickname: form.nickname.trim(),
      phone: form.phone.trim(),
      avatar: form.avatar,
      bio: form.bio.trim(),
    })
    uploadRef.value?.commit()
    message.success('资料已更新')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <page-container title="个人中心" subtitle="管理你的公开资料与账号信息">
    <div v-if="loading">
      <loading-state :rows="4" variant="panel" />
    </div>
    <div v-else-if="error" class="profile-error">
      <empty-state
        title="资料加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="load"
      />
    </div>
    <div v-else-if="profile" class="profile-grid">
      <aside class="profile-card panel">
        <div class="profile-card__avatar">
          <user-avatar :avatar="profile.avatar" :name="profile.nickname" :size="72" />
        </div>
        <h2 class="profile-card__name">{{ profile.nickname }}</h2>
        <p class="profile-card__username text-tertiary">@{{ profile.username }}</p>
        <div class="profile-card__role">
          <status-tag :value="profile.role" />
          <status-tag :value="profile.status" />
        </div>
        <dl class="profile-card__info">
          <div>
            <dt>注册时间</dt>
            <dd class="num">{{ formatDate(profile.createdAt) }}</dd>
          </div>
          <div>
            <dt>手机号</dt>
            <dd>{{ profile.phone }}</dd>
          </div>
        </dl>
      </aside>

      <section class="profile-form panel">
        <h3 class="profile-form__title">编辑资料</h3>
        <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-placement="top"
        >
          <n-form-item label="头像">
            <image-upload ref="uploadRef" v-model="avatarList" :max="1" tip="建议使用 1:1 正方形图片" />
          </n-form-item>
          <div class="profile-form__grid">
            <n-form-item path="nickname" label="昵称">
              <n-input v-model:value="form.nickname" placeholder="同学怎么称呼你" />
            </n-form-item>
            <n-form-item path="phone" label="手机号">
              <n-input v-model:value="form.phone" placeholder="用于交易联系与账号找回" />
            </n-form-item>
          </div>
          <n-form-item path="bio" label="个人简介">
            <n-input
              v-model:value="form.bio"
              type="textarea"
              :rows="3"
              :maxlength="120"
              show-count
              placeholder="介绍一下自己,例如专业、爱好、常出没的地方"
            />
          </n-form-item>
          <div class="profile-form__actions">
            <n-button type="primary" size="large" :loading="saving" @click="handleSave">
              保存修改
            </n-button>
          </div>
        </n-form>
      </section>
    </div>
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.profile-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.profile-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 24px;
  align-items: start;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  padding: 28px 24px;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  &__name {
    margin: 14px 0 0;
    font-size: 19px;
    font-weight: 700;
    color: $color-text;
  }

  &__username {
    margin: 2px 0 12px;
    font-size: 13px;
  }

  &__role {
    display: flex;
    gap: 6px;
  }

  &__info {
    width: 100%;
    margin: 24px 0 0;
    padding: 0;
    border-top: 1px solid $color-border;

    div {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid $color-border;
    }

    dt {
      font-size: 12px;
      color: $color-text-tertiary;
    }

    dd {
      margin: 0;
      font-size: 13px;
      color: $color-text;
    }
  }
}

.profile-form {
  padding: 28px 30px;
  box-shadow: none;

  &__title {
    margin: 0 0 20px;
    font-size: 17px;
    font-weight: 700;
    color: $color-text;
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    padding-top: 8px;
  }

  @media (max-width: 640px) {
    padding: 20px;

    &__grid {
      grid-template-columns: 1fr;
      gap: 0;
    }
  }
}
</style>
