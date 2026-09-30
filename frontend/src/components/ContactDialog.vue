<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NIcon, NModal, useMessage } from 'naive-ui'
import {
  PhCopy,
  PhPhone,
  PhSignIn,
  PhUserCircle,
  PhWechatLogo,
} from '@phosphor-icons/vue'
import type { ContactItem, ContactVisibility } from '@/types/common'

const props = withDefaults(
  defineProps<{
    show: boolean
    /** 被介绍的人（卖家 / 发布者） */
    name: string
    avatar?: string
    /** 简介、注册时间等附注行，直接渲染在名字下方 */
    subtitle?: string
    /** 联系方式列表；为空时展示「未填写」 */
    contacts?: ContactItem[]
    /** 可见性：anonymous=未登录，granted=已登录可见 */
    visibility?: ContactVisibility
    /** 未登录时的引导文案，由调用方按业务给 */
    gateHint?: string
    loading?: boolean
    /** 底部安全提示 */
    tip?: string
  }>(),
  {
    avatar: '',
    subtitle: '',
    contacts: () => [],
    visibility: 'granted',
    gateHint: '',
    loading: false,
    tip: '',
  },
)

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

const router = useRouter()
const message = useMessage()

/** 过滤掉空值，避免渲染出「手机号：-」这类噪音 */
const visibleContacts = computed(() =>
  props.contacts.filter((item) => item.value && item.value.trim().length > 0),
)

const hasContact = computed(() => visibleContacts.value.length > 0)

async function copyOne(item: ContactItem): Promise<void> {
  try {
    await navigator.clipboard.writeText(item.value)
    message.success(item.copyTip || `${item.label}已复制`)
  } catch {
    message.warning('复制失败,请手动选择文本复制')
  }
}

async function copyAll(): Promise<void> {
  if (!hasContact.value) {
    message.warning('对方未填写联系方式')
    return
  }
  const text = visibleContacts.value.map((item) => `${item.label}:${item.value}`).join('\n')
  try {
    await navigator.clipboard.writeText(text)
    message.success('联系方式已复制')
  } catch {
    message.warning('复制失败,请手动选择文本复制')
  }
}

function goLogin(): void {
  emit('update:show', false)
  void router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
}
</script>

<template>
  <n-modal
    :show="show"
    preset="card"
    class="contact-dialog"
    :bordered="false"
    :title="`${name || '对方'}的联系方式`"
    style="max-width: 440px"
    @update:show="emit('update:show', $event)"
  >
    <div class="contact-dialog__body">
      <div class="contact-dialog__head">
        <user-avatar :avatar="avatar" :name="name" :size="60" />
        <div class="contact-dialog__identity">
          <h3 class="contact-dialog__name">{{ name || '匿名同学' }}</h3>
          <p v-if="subtitle" class="contact-dialog__subtitle">{{ subtitle }}</p>
        </div>
      </div>

      <!-- 加载中 -->
      <template v-if="loading">
        <div class="skeleton-block" style="height: 74px; margin-top: 16px" />
        <div class="skeleton-block" style="height: 40px; margin-top: 14px" />
      </template>

      <!-- 未登录：登录后才可见 -->
      <template v-else-if="visibility === 'anonymous'">
        <div class="contact-dialog__state">
          <n-icon :size="26" :component="PhSignIn" />
          <p class="contact-dialog__state-title">登录后查看联系方式</p>
          <p class="contact-dialog__state-desc">
            登录后即可看到对方完整联系方式,方便线下验货与当面交易。
          </p>
          <n-button type="primary" block @click="goLogin">去登录</n-button>
        </div>
      </template>

      <!-- 已登录：完整明文展示 -->
      <template v-else>
        <p v-if="!hasContact" class="contact-dialog__empty">
          对方还没有留下联系方式。
        </p>

        <ul v-else class="contact-dialog__list">
          <li v-for="item in visibleContacts" :key="`${item.label}-${item.value}`">
            <span class="contact-dialog__label">
              <n-icon :size="15" :component="item.label.includes('微信') ? PhWechatLogo : PhPhone" />
              {{ item.label }}
            </span>
            <span class="contact-dialog__value num">{{ item.value }}</span>
            <button
              type="button"
              class="contact-dialog__copy pressable"
              :title="`复制${item.label}`"
              @click="copyOne(item)"
            >
              <n-icon :size="15" :component="PhCopy" />
            </button>
          </li>
        </ul>

        <div v-if="hasContact" class="contact-dialog__actions">
          <n-button type="primary" block @click="copyAll">
            <template #icon>
              <n-icon :component="PhCopy" />
            </template>
            复制联系方式
          </n-button>
        </div>
      </template>

      <p v-if="tip" class="contact-dialog__tip text-tertiary">
        <n-icon :size="13" :component="PhUserCircle" />
        {{ tip }}
      </p>
    </div>
  </n-modal>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.contact-dialog {
  &__body {
    display: flex;
    flex-direction: column;
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__identity {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  &__name {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: $color-text;
  }

  &__subtitle {
    margin: 0;
    font-size: 12px;
    line-height: 1.6;
    color: $color-text-tertiary;
  }

  // 未解锁 / 未登录的引导卡片
  &__state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    margin-top: 16px;
    padding: 22px 18px 18px;
    border: 1px dashed $color-border-strong;
    border-radius: $radius-md;
    background: $color-surface;
    text-align: center;
    color: $color-accent;

    .n-button {
      margin-top: 6px;
    }
  }

  &__state-title {
    margin: 4px 0 0;
    font-size: 15px;
    font-weight: 700;
    color: $color-text;
  }

  &__state-desc {
    margin: 0;
    font-size: 13px;
    line-height: 1.75;
    color: $color-text-secondary;
  }

  // 已解锁：明文联系方式列表
  &__list {
    list-style: none;
    margin: 18px 0 0;
    padding: 6px 14px;
    border: 1px solid rgba(22, 163, 74, 0.35);
    border-radius: $radius-md;
    background: $color-accent-soft;

    li {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;

      & + li {
        border-top: 1px solid rgba(22, 163, 74, 0.18);
      }
    }
  }

  &__label {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    flex-shrink: 0;
    font-size: 12px;
    font-weight: 600;
    color: $color-accent;
  }

  &__value {
    flex: 1;
    min-width: 0;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 0.03em;
    color: $color-text;
    word-break: break-all;
    // 点击即可整段选中，方便手动复制
    user-select: all;
  }

  &__copy {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    border: 1px solid rgba(22, 163, 74, 0.35);
    border-radius: 8px;
    background: $color-surface;
    color: $color-accent;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      background: $color-accent;
      border-color: $color-accent;
      color: #fff;
    }
  }

  &__empty {
    margin: 18px 0 0;
    padding: 18px 16px;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    background: $color-surface;
    font-size: 13px;
    line-height: 1.75;
    color: $color-text-secondary;
    text-align: center;
  }

  &__actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 16px;
  }

  &__tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    margin: 14px 0 0;
    font-size: 12px;
    line-height: 1.7;
    text-align: center;
  }
}
</style>
