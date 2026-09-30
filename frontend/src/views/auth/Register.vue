<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NCheckbox,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import {
  PhArrowRight,
  PhCalendarBlank,
  PhDeviceMobile,
  PhLifebuoy,
  PhLockKey,
  PhShoppingCartSimple,
  PhSparkle,
  PhUser,
} from '@phosphor-icons/vue'
import { useAuthStore } from '@/stores/auth'
import type { RegisterParams } from '@/types/auth'
import campusBg from '@/assets/auth-campus.jpg'
import buildingsBg from '@/assets/auth-buildings.png'
import { focusFirstError } from '@/utils/form'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const agreed = ref(false)

const form = reactive<RegisterParams>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
})

const features = [
  { icon: PhShoppingCartSimple, title: '二手交易', desc: '闲置好物,快速找到买家' },
  { icon: PhCalendarBlank, title: '校园活动', desc: '讲座、社团、赛事,一键报名' },
  { icon: PhLifebuoy, title: '失物招领', desc: '丢失物品,快速发布寻找' },
]

// 下面三个函数只负责「格式 / 一致性」判定,空值一律放过 —— 必填交给声明式的 required 规则。
// 坑:async-validator 里只要规则带了自定义 validator,同一对象上的 required 会被完全忽略
// (见 getValidationMethod),所以「必填」和「格式」必须拆成两条规则,不能塞进同一个对象,
// 否则用户不填就直接提交时什么提示都不会弹。
function validateUsername(_rule: unknown, value: string): Promise<void> {
  if (!value) return Promise.resolve()
  if (!/^[a-zA-Z0-9_]{4,20}$/.test(value)) {
    return Promise.reject(new Error('用户名需为 4-20 位字母、数字或下划线'))
  }
  return Promise.resolve()
}

function validatePhone(_rule: unknown, value: string): Promise<void> {
  if (!value) return Promise.resolve()
  if (!/^1\d{10}$/.test(value.replace(/\s/g, ''))) {
    return Promise.reject(new Error('请输入 11 位有效手机号'))
  }
  return Promise.resolve()
}

function validateConfirmPassword(_rule: unknown, value: string): Promise<void> {
  if (!value) return Promise.resolve()
  if (value !== form.password) {
    return Promise.reject(new Error('两次输入的密码不一致'))
  }
  return Promise.resolve()
}

const rules: FormRules = {
  // 必填走 input+blur(边填边把红字清掉);格式/长度走 blur,免得刚敲第一个字符就报错
  username: [
    { required: true, message: '请输入用户名', trigger: ['input', 'blur'] },
    { key: 'username-format', validator: validateUsername, trigger: ['blur'] },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: ['input', 'blur'] }],
  phone: [
    { required: true, message: '请输入手机号', trigger: ['input', 'blur'] },
    { key: 'phone-format', validator: validatePhone, trigger: ['blur'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['input', 'blur'] },
    { key: 'password-length', min: 6, max: 24, message: '密码长度为 6-24 位', trigger: ['blur'] },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: ['input', 'blur'] },
    { key: 'confirm-match', validator: validateConfirmPassword, trigger: ['input', 'blur'] },
  ],
}

// 先填「确认密码」再回头改「密码」时,确认框那条结论会过期,这里同步复核一次
watch(
  () => form.password,
  () => {
    if (!form.confirmPassword) return
    void formRef.value
      ?.validate(undefined, (rule) => (rule as { key?: string }).key === 'confirm-match')
      .catch(() => undefined)
  },
)

function handleAgreement(): void {
  message.info('平台协议整理中,敬请期待')
}

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    // 出错项可能在视口外,滚过去再返回,避免用户以为「点了没反应」
    void focusFirstError(formRef.value)
    return
  }
  if (!agreed.value) {
    message.warning('请先阅读并同意用户协议与隐私政策')
    return
  }
  loading.value = true
  try {
    await authStore.register(form)
    message.success('注册成功,请登录')
    void router.push('/login')
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-shell">
      <div class="auth-card">
        <!-- 左:校园插画 + 翡翠渐变蒙层(与登录页同一语言) -->
        <section class="auth-card__brand">
          <img class="auth-card__bg" :src="campusBg" alt="" aria-hidden="true" />
          <div class="auth-card__scrim" aria-hidden="true"></div>

          <div class="auth-card__brand-content">
            <div class="auth-card__lockup">
              <span class="auth-card__logo" aria-hidden="true">
                <brand-mark :size="24" />
              </span>
              <span class="auth-card__name">CampusHub</span>
            </div>

            <h1 class="auth-card__headline">校园生活<br />一站式入口</h1>
            <p class="auth-card__sub">二手交易 · 活动报名 · 失物招领 · 校园公告</p>
            <p class="auth-card__sub-sub">让校园里的信息、交易和活动,在一个平台完成。</p>

            <ul class="auth-card__features">
              <li v-for="item in features" :key="item.title">
                <span class="auth-card__feature-icon" aria-hidden="true">
                  <n-icon :size="17" :component="item.icon" />
                </span>
                <span class="auth-card__feature-text">
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.desc }}</small>
                </span>
              </li>
            </ul>
          </div>
        </section>

        <!-- 右:表单区 -->
        <section class="auth-card__panel">
          <img class="auth-card__watermark" :src="buildingsBg" alt="" aria-hidden="true" />

          <div class="auth-card__panel-head">
            <h2 class="auth-card__form-title">创建账号</h2>
            <p class="auth-card__switch">
              已有账号?
              <router-link to="/login" class="auth-card__link">直接登录</router-link>
            </p>
          </div>
          <p class="auth-card__form-subtitle">加入 CampusHub,发布闲置、报名活动、互助失物。</p>

          <n-form ref="formRef" :model="form" :rules="rules" size="large" label-placement="top">
            <n-form-item path="username">
              <template #label>
                <span class="auth-card__field-label">
                  <n-icon :size="13" :component="PhUser" />
                  用户名
                </span>
              </template>
              <n-input v-model:value="form.username" placeholder="请输入 4-20 位字母、数字或下划线" clearable>
                <template #prefix>
                  <n-icon :component="PhUser" />
                </template>
              </n-input>
            </n-form-item>
            <n-form-item path="nickname">
              <template #label>
                <span class="auth-card__field-label">
                  <n-icon :size="13" :component="PhSparkle" />
                  昵称
                </span>
              </template>
              <n-input v-model:value="form.nickname" placeholder="怎么称呼你" clearable>
                <template #prefix>
                  <n-icon :component="PhSparkle" />
                </template>
              </n-input>
            </n-form-item>
            <!-- 手机号校验的报错必须能显示出来,不能关掉 feedback -->
            <n-form-item path="phone">
              <template #label>
                <span class="auth-card__field-label">
                  <n-icon :size="13" :component="PhDeviceMobile" />
                  手机号
                </span>
              </template>
              <n-input v-model:value="form.phone" placeholder="请输入手机号" clearable>
                <template #prefix>
                  <n-icon :component="PhDeviceMobile" />
                </template>
              </n-input>
            </n-form-item>
            <p class="auth-card__field-help">用于账号找回与交易联系</p>
            <div class="auth-card__field-grid">
              <n-form-item path="password">
                <template #label>
                  <span class="auth-card__field-label">
                    <n-icon :size="13" :component="PhLockKey" />
                    密码
                  </span>
                </template>
                <n-input
                  v-model:value="form.password"
                  type="password"
                  show-password-on="click"
                  placeholder="6-24 位密码"
                >
                  <template #prefix>
                    <n-icon :component="PhLockKey" />
                  </template>
                </n-input>
              </n-form-item>
              <n-form-item path="confirmPassword">
                <template #label>
                  <span class="auth-card__field-label">
                    <n-icon :size="13" :component="PhLockKey" />
                    确认密码
                  </span>
                </template>
                <n-input
                  v-model:value="form.confirmPassword"
                  type="password"
                  show-password-on="click"
                  placeholder="再次输入密码"
                >
                  <template #prefix>
                    <n-icon :component="PhLockKey" />
                  </template>
                </n-input>
              </n-form-item>
            </div>

            <div class="auth-card__agreement">
              <n-checkbox v-model:checked="agreed" />
              <span>
                我已阅读并同意
                <button type="button" class="auth-card__agreement-link" @click="handleAgreement">
                  《用户协议》
                </button>
                和
                <button type="button" class="auth-card__agreement-link" @click="handleAgreement">
                  《隐私政策》
                </button>
              </span>
            </div>

            <n-button type="primary" size="large" block :loading="loading" @click="handleSubmit">
              注册并加入
              <template #icon>
                <n-icon :component="PhArrowRight" />
              </template>
            </n-button>
          </n-form>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
// 版式与两页共享,差异项用 CSS 变量注入
@use '@/styles/auth-page.scss' as *;

.auth-card {
  --auth-columns: minmax(0, 0.98fr) minmax(440px, 1.02fr);
  --auth-brand-min-h: 720px;
  // 底部给右下角建筑插画留出位置,避免按钮压住
  --auth-panel-pad: 40px 44px 176px;
}
</style>
