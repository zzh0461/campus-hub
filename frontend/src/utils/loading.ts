import { ref } from 'vue'

/** 全局请求计数,>0 时展示顶部加载条 */
const loadingCount = ref(0)
const visible = ref(false)

let hideTimer: ReturnType<typeof setTimeout> | undefined

export function startLoading(): void {
  loadingCount.value += 1
  visible.value = true
  if (hideTimer) clearTimeout(hideTimer)
}

export function endLoading(): void {
  loadingCount.value = Math.max(0, loadingCount.value - 1)
  if (loadingCount.value === 0) {
    // 保证加载条至少有短暂的可感知过程,避免闪烁
    hideTimer = setTimeout(() => {
      visible.value = false
    }, 240)
  }
}

export function isLoadingVisible(): boolean {
  return visible.value
}
