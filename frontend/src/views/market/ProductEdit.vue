<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
import { getProduct, getProductCategories, updateProduct } from '@/api/market'
import type { ProductCategory } from '@/types/market'
import { focusFirstError } from '@/utils/form'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(true)
const error = ref(false)
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
    message: '描述至少 10 个字',
    trigger: ['input', 'blur'],
  },
}

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const [product, categoryList] = await Promise.all([
      getProduct(Number(route.params.id)),
      getProductCategories(),
    ])
    form.title = product.title
    form.categoryId = product.categoryId
    form.price = product.price
    form.description = product.description
    form.images = [...product.images]
    categories.value = categoryList
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

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
    await updateProduct(Number(route.params.id), {
      title: form.title.trim(),
      categoryId: form.categoryId as number,
      price: form.price as number,
      description: form.description.trim(),
      images: form.images,
    })
    message.success('保存成功')
    uploadRef.value?.commit()
    void router.push(`/market/products/${route.params.id}`)
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <page-container title="编辑商品" subtitle="修改商品信息后保存即可生效" max-width="820">
    <div v-if="loading" class="edit-loading">
      <loading-state :rows="5" variant="panel" />
    </div>
    <div v-else-if="error" class="edit-error">
      <empty-state
        title="商品加载失败"
        description="该商品可能不存在或已被删除"
        action-text="返回我的商品"
        @action="router.push('/market/my')"
      />
    </div>
    <div v-else class="publish-form panel">
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
            placeholder="描述成色、入手渠道、购买时间、支持的面交地点等"
          />
        </n-form-item>

        <n-form-item label="商品图片">
          <image-upload ref="uploadRef" v-model="form.images" :max="6" class="publish-form__upload" />
        </n-form-item>

        <div class="publish-form__actions">
          <n-button size="large" @click="router.back()">取消</n-button>
          <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            保存修改
          </n-button>
        </div>
      </n-form>
    </div>
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.edit-loading {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: 24px;
}

.edit-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

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
