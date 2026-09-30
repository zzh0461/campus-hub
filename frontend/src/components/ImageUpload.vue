<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { NButton, NIcon, NSpin } from 'naive-ui'
import { PhCloudArrowUp, PhTrash } from '@phosphor-icons/vue'
import { deleteImage, uploadImage } from '@/api/upload'
import { emitToast } from '@/utils/events'

const props = withDefaults(
  defineProps<{
    modelValue: string[]
    max?: number
    tip?: string
  }>(),
  {
    max: 6,
    tip: '支持 JPG / PNG / WebP,单张不超过 5MB',
  },
)

const emit = defineEmits<{ (e: 'update:modelValue', value: string[]): void }>()

const images = computed<string[]>({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const canUpload = computed(() => images.value.length < props.max)

// 记录"本次会话刚上传"的 URL：移除缩略图时只删这些临时文件，
// 编辑页带入的已发布图片不在此集合，移除时不删，避免取消编辑误删正式图片
const sessionUploaded = new Set<string>()

const fileInput = ref<HTMLInputElement | null>(null)
const uploadingCount = ref(0)
const isUploading = computed(() => uploadingCount.value > 0)

function triggerPick(): void {
  fileInput.value?.click()
}

/**
 * 处理文件选择：逐张校验并上传。
 * 不用 n-upload 的原因：它的 custom-request 若不回调 onSuccess/onError，
 * 内部队列会把文件一直挂在 pending 态，后续交互可能把"已从表单删除"的文件
 * 重新提交上传——表现为"删掉的图片又复活"。原生 input 用完即重置，无隐藏状态。
 */
async function handleFiles(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  // 立即重置 input，同一个文件下次还能再选（浏览器对同路径文件默认不触发 change）
  input.value = ''
  if (!files.length) return

  for (const file of files) {
    if (images.value.length >= props.max) {
      emitToast('warning', `最多上传 ${props.max} 张图片`)
      break
    }
    if (file.size > 5 * 1024 * 1024) {
      emitToast('error', '图片大小不能超过 5MB')
      continue
    }
    uploadingCount.value += 1
    try {
      const result = await uploadImage(file)
      // 等待期间用户可能删了其他图/满了，追加前重查上限
      if (images.value.length >= props.max) {
        void deleteImage(result.url).catch(() => {})
        emitToast('warning', `最多上传 ${props.max} 张图片`)
        break
      }
      sessionUploaded.add(result.url)
      images.value = [...images.value, result.url]
    } catch {
      emitToast('error', '图片上传失败,请重试')
    } finally {
      uploadingCount.value -= 1
    }
  }
}

function removeAt(index: number): void {
  const url = images.value[index]
  images.value = images.value.filter((_, i) => i !== index)
  // 仅清理本次会话上传的临时文件；删除失败不影响前端移除操作
  if (sessionUploaded.has(url)) {
    sessionUploaded.delete(url)
    deleteImage(url).catch(() => {})
  }
}

/**
 * 组件卸载时清理"本次上传但未被采用"的文件：
 * 判断依据不是"还在不在表单里"——取消发布时表单里同样留着刚上传的 URL，
 * 组件自己无法区分"提交成功"和"中途放弃"，只有父组件知道。
 * 因此父组件在提交成功后必须调用 commit()（暴露方法）标记"已采用"；
 * 未被 commit 的会话文件在这里统一删除，不留孤儿。
 */
onBeforeUnmount(() => {
  sessionUploaded.forEach((url) => {
    deleteImage(url).catch(() => {})
  })
  sessionUploaded.clear()
})

/**
 * 提交成功后由父组件调用：本次上传的文件已被业务数据采用，
 * 卸载时的孤儿清理会跳过它们
 */
function commit(): void {
  sessionUploaded.clear()
}

defineExpose({ commit })
</script>

<template>
  <div class="image-upload">
    <div class="image-upload__grid">
      <figure v-for="(url, index) in images" :key="url" class="image-upload__item">
        <img :src="url" alt="已上传图片" loading="lazy" />
        <button
          class="image-upload__remove pressable"
          type="button"
          aria-label="删除图片"
          @click="removeAt(index)"
        >
          <n-icon :size="15" :component="PhTrash" />
        </button>
      </figure>
      <n-button
        v-if="canUpload"
        dashed
        size="large"
        class="image-upload__trigger"
        :disabled="isUploading"
        @click="triggerPick"
      >
        <n-spin v-if="isUploading" :size="18" />
        <n-icon v-else :size="20" :component="PhCloudArrowUp" />
        {{ isUploading ? '上传中…' : '上传图片' }}
      </n-button>
      <input
        ref="fileInput"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        hidden
        @change="handleFiles"
      />
    </div>
    <p class="image-upload__tip text-tertiary">{{ tip }}({{ images.length }}/{{ max }})</p>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.image-upload {
  &__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(108px, 1fr));
    gap: 12px;
  }

  &__item {
    position: relative;
    margin: 0;
    aspect-ratio: 1;
    border-radius: $radius-sm;
    overflow: hidden;
    border: 1px solid $color-border;
    background: $color-surface-soft;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__remove {
    position: absolute;
    top: 6px;
    right: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border: none;
    border-radius: 8px;
    color: #fff;
    background: rgba(28, 25, 23, 0.72);
    cursor: pointer;
    opacity: 0;
    transition: $transition-fast;
  }

  &__item:hover &__remove {
    opacity: 1;
  }

  &__trigger {
    width: 100%;
    height: 108px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    border-radius: $radius-sm;
  }

  &__tip {
    margin: 10px 0 0;
    font-size: 12px;
  }
}
</style>
