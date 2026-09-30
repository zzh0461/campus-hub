export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastPayload {
  type: ToastType
  content: string
}

export function emitToast(type: ToastType, content: string): void {
  window.dispatchEvent(
    new CustomEvent<ToastPayload>('campushub:toast', {
      detail: { type, content },
    }),
  )
}

export function emitUnauthorized(): void {
  window.dispatchEvent(new CustomEvent('campushub:unauthorized'))
}

const NOTIFICATIONS_CHANGED = 'campushub:notifications-changed'

/** 通知数据发生变更（已读/全部已读/删除）后广播，顶栏铃铛监听并刷新角标 */
export function emitNotificationsChanged(): void {
  window.dispatchEvent(new CustomEvent(NOTIFICATIONS_CHANGED))
}

/** 订阅通知变更事件，返回取消订阅函数（供 onBeforeUnmount 清理，防内存泄漏） */
export function onNotificationsChanged(handler: () => void): () => void {
  window.addEventListener(NOTIFICATIONS_CHANGED, handler)
  return () => window.removeEventListener(NOTIFICATIONS_CHANGED, handler)
}