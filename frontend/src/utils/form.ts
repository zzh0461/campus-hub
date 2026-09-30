import { nextTick } from 'vue'
import type { FormInst } from 'naive-ui'

/**
 * 提交校验失败后,把第一个出错的表单项滚进视口并聚焦。
 *
 * naive-ui 只负责在字段旁边渲染红字提示;长表单或弹窗里,出错项常常落在视口之外,
 * 用户点完提交看不到任何反馈,就会以为「没提示 / 点了没反应」。
 */
export async function focusFirstError(form: FormInst | null | undefined): Promise<void> {
  // 错误态是在校验 Promise reject 之后才渲染的,先等一拍 DOM
  await nextTick()
  // FormInst 的类型里没暴露 $el,运行时组件实例上一定有
  const root = (form as unknown as { $el?: HTMLElement } | null | undefined)?.$el
  const item = root
    ?.querySelector<HTMLElement>('.n-form-item-feedback--error')
    ?.closest<HTMLElement>('.n-form-item')
  if (!item) return
  item.scrollIntoView({ behavior: 'smooth', block: 'center' })
  item.querySelector<HTMLElement>('input, textarea')?.focus({ preventScroll: true })
}
