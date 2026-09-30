<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import { getProductCategories, publishProduct } from '@/api/market'
import type { ProductCategory } from '@/types/market'
import { focusFirstError } from '@/utils/form'

const router = useRouter()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const categories = ref<ProductCategory[]>([])
// 图片上传组件：提交成功后调用 commit() 标记"本次上传的图已被采用"，离开页面时组件才能放心清理
const uploadRef = ref<{ commit: () => void } | null>(null)

const form = reactive({
  title: '',
  categoryId: null as number | null,
  price: null as number | null,
  description: '',
  images: [] as string[],
})

const rules: FormRules = {
  title: {
    required: true,
    max: 60,
    message: '请输入商品标题(60 字以内)',
    trigger: ['input', 'blur'],
  },
  categoryId: { required: true, type: 'number', message: '请选择商品分类', trigger: ['change'] },
  price: {
    required: true,
    type: 'number',
    message: '请输入价格',
    trigger: ['input', 'blur'],
  },
  description: {
    required: true,
    min: 10,
    message: '描述至少 10 个字,写清楚成色、入手渠道等信息更容易成交',
    trigger: ['input', 'blur'],
  },
}

onMounted(async () => {
  try {
    categories.value = await getProductCategories()
  } catch {
    categories.value = []
  }
})

async function handleSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    // 出错项可能在视口外,滚过去再返回,避免用户以为「点了没反应」
    void focusFirstError(formRef.value)
    return
  }
  submitting.value = true
  try {
    const product = await publishProduct({
      title: form.title.trim(),
      categoryId: form.categoryId as number,
      price: form.price as number,
      description: form.description.trim(),
      images: form.images,
    })
    message.success('发布成功')
    uploadRef.value?.commit()
    void router.push(`/market/products/${product.id}`)
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <page-container
    title="发布商品"
    subtitle="认真描述成色与入手渠道,更容易快速成交"
    max-width="820"
  >
    <div class="publish-form panel">
      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item path="title" label="商品标题">
          <n-input
            v-model:value="form.title"
            placeholder="如:ikbc C87 红轴机械键盘 95 新"
            :maxlength="60"
            show-count
          />
        </n-form-item>

        <div class="publish-form__grid">
          <n-form-item path="categoryId" label="商品分类">
            <n-select
              v-model:value="form.categoryId"
              placeholder="请选择分类"
              :options="categories.map((c) => ({ label: c.name, value: c.id }))"
            />
          </n-form-item>
          <n-form-item path="price" label="价格(元)">
            <n-input-number
              v-model:value="form.price"
              placeholder="0.00"
              :min="0"
              :precision="2"
              class="publish-form__price"
            />
          </n-form-item>
        </div>

        <n-form-item path="description" label="商品描述">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="6"
            placeholder="描述成色、入手渠道、购买时间、支持的面交地点等,不少于 10 个字"
          />
        </n-form-item>

        <n-form-item label="商品图片">
          <image-upload ref="uploadRef" v-model="form.images" :max="6" class="publish-form__upload" />
        </n-form-item>

        <div class="publish-form__actions">
          <n-button size="large" @click="router.back()">取消</n-button>
          <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            发布商品
          </n-button>
        </div>
      </n-form>
    </div>
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.publish-form {
  padding: 32px;

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  &__price {
    width: 100%;
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
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
