<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
  PhChatTeardropDots,
  PhLockKey,
  PhMapPin,
  PhShieldCheck,
  PhShoppingCartSimple,
  PhUser,
  PhUserCircle,
  PhWechatLogo,
} from '@phosphor-icons/vue'
import { useAuthStore } from '@/stores/auth'
import type { LoginParams } from '@/types/auth'
import { mockDemoAccounts } from '@/mocks'
import campusBg from '@/assets/auth-campus.jpg'
import buildingsBg from '@/assets/auth-buildings.png'
import { focusFirstError } from '@/utils/form'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const remember = ref(false)

const form = reactive<LoginParams>({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: ['input', 'blur'] },
  password: { required: true, message: '请输入密码', trigger: ['input', 'blur'] },
}

const features = [
  { icon: PhShoppingCartSimple, title: '二手交易', desc: '闲置好物,快速找到买家' },
  { icon: PhCalendarBlank, title: '校园活动', desc: '讲座、社团、赛事,一键报名' },
  { icon: PhMapPin, title: '失物招领', desc: '丢失物品,快速发布寻找' },
]

// "记住账号"只记住用户名,绝不落盘密码
const REMEMBER_KEY = 'campushub:remembered-username'

onMounted(() => {
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    form.username = saved
    remember.value = true
  }
})

function fillDemo(username: string, password: string): void {
  form.username = username
  form.password = password
}

function handleForgot(): void {
  message.info('如需重置密码,请联系平台管理员处理')
}

function handleSocial(name: string): void {
  message.info(`${name}登录暂未接入,敬请期待`)
}

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    // 出错项可能在视口外,滚过去再返回,避免用户以为「点了没反应」
    void focusFirstError(formRef.value)
    return
  }
  loading.value = true
  try {
    await authStore.login(form)
    if (remember.value) {
      localStorage.setItem(REMEMBER_KEY, form.username)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    message.success('登录成功,欢迎回来')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    void router.push(redirect)
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
        <!-- 左:校园插画 + 翡翠渐变蒙层 -->
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
            <h2 class="auth-card__form-title">登录</h2>
            <p class="auth-card__switch">
              还没有账号?
              <router-link to="/register" class="auth-card__link">立即注册</router-link>
            </p>
          </div>
          <p class="auth-card__form-subtitle">使用校园账号登录 CampusHub</p>

          <div class="auth-card__demo">
            <p class="auth-card__demo-label">测试账号(仅体验使用)</p>
            <div class="auth-card__demo-list">
              <button
                v-for="demo in mockDemoAccounts"
                :key="demo.username"
                type="button"
                class="auth-card__demo-chip"
                @click="fillDemo(demo.username, demo.password)"
              >
                <n-icon
                  :size="13"
                  :component="demo.username === 'admin' ? PhShieldCheck : PhUserCircle"
                />
                {{ demo.label }} {{ demo.username }}
              </button>
            </div>
          </div>

          <n-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleSubmit">
            <n-form-item path="username" :show-label="false">
              <n-input v-model:value="form.username" placeholder="请输入用户名" clearable>
                <template #prefix>
                  <n-icon :component="PhUser" />
                </template>
                <template #suffix>
                  <span class="auth-card__input-hint">校园账号</span>
                </template>
              </n-input>
            </n-form-item>
            <n-form-item path="password" :show-label="false">
              <n-input
                v-model:value="form.password"
                type="password"
                show-password-on="click"
                placeholder="请输入密码"
              >
                <template #prefix>
                  <n-icon :component="PhLockKey" />
                </template>
              </n-input>
            </n-form-item>

            <div class="auth-card__form-row">
              <n-checkbox v-model:checked="remember">记住账号</n-checkbox>
              <button type="button" class="auth-card__text-btn" @click="handleForgot">
                忘记密码?
              </button>
            </div>

            <n-button type="primary" size="large" block :loading="loading" @click="handleSubmit">
              登 录
              <template #icon>
                <n-icon :component="PhArrowRight" />
              </template>
            </n-button>
          </n-form>

          <div class="auth-card__divider">
            <span>或使用其他方式登录</span>
          </div>
          <div class="auth-card__social">
            <button
              type="button"
              class="auth-card__social-btn auth-card__social-btn--wechat"
              title="微信登录"
              @click="handleSocial('微信')"
            >
              <n-icon :size="20" :component="PhWechatLogo" />
            </button>
            <button
              type="button"
              class="auth-card__social-btn auth-card__social-btn--qq"
              title="QQ 登录"
              @click="handleSocial('QQ')"
            >
              <n-icon :size="20" :component="PhChatTeardropDots" />
            </button>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
// 版式与两页共享,差异项用 CSS 变量注入
@use '@/styles/auth-page.scss' as *;

.auth-card {
  --auth-columns: minmax(0, 1.02fr) minmax(420px, 0.98fr);
  --auth-brand-min-h: 660px;
  --auth-panel-pad: 44px 44px 40px;
}
</style>
