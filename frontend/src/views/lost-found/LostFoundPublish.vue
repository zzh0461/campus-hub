<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NRadioButton,
  NRadioGroup,
  type FormInst,
  type FormRules,
  useMessage,
} from 'naive-ui'
import { publishLostFound } from '@/api/lostFound'
import type { LostFoundType } from '@/types/lostFound'
import { focusFirstError } from '@/utils/form'

const router = useRouter()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
// 图片上传组件：提交成功后调用 commit() 标记"本次上传的图已被采用"，离开页面时组件才能放心清理
const uploadRef = ref<{ commit: () => void } | null>(null)

const form = reactive({
  type: 'LOST' as LostFoundType,
  title: '',
  description: '',
  location: '',
  contact: '',
  images: [] as string[],
})

const rules: FormRules = {
  title: {
    required: true,
    max: 60,
    message: '请输入标题(60 字以内)',
    trigger: ['input', 'blur'],
  },
  location: { required: true, message: '请填写地点,方便同学定位', trigger: ['input', 'blur'] },
  contact: { required: true, message: '请留下联系电话或 QQ', trigger: ['input', 'blur'] },
  description: {
    required: true,
    min: 10,
    message: '描述至少 10 个字,写清楚物品特征',
    trigger: ['input', 'blur'],
  },
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
    const item = await publishLostFound({
      type: form.type,
      title: form.title.trim(),
      description: form.description.trim(),
      location: form.location.trim(),
      contact: form.contact.trim(),
      images: form.images,
    })
    message.success('发布成功,祝早日物归原主')
    uploadRef.value?.commit()
    void router.push(`/lost-found/${item.id}`)
  } catch {
    // 错误提示由 request 层统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <page-container title="发布失物/招领" subtitle="描述越具体,找到的概率越高" max-width="820">
    <div class="lost-publish panel">
      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item label="类型">
          <n-radio-group v-model:value="form.type">
            <n-radio-button value="LOST">我丢了东西</n-radio-button>
            <n-radio-button value="FOUND">我捡到了东西</n-radio-button>
          </n-radio-group>
        </n-form-item>

        <n-form-item path="title" label="标题">
          <n-input
            v-model:value="form.title"
            :placeholder="form.type === 'LOST' ? '如:黑色钱包(内含校园卡)遗失在图书馆三楼' : '如:拾到 AirPods 耳机盒(东操场)'"
            :maxlength="60"
            show-count
          />
        </n-form-item>

        <n-form-item path="location" label="地点">
          <n-input v-model:value="form.location" placeholder="如:图书馆三楼 / 东区操场看台" />
        </n-form-item>

        <n-form-item path="description" label="详细描述">
          <n-input
            v-model:value="form.description"
            type="textarea"
            :rows="5"
            placeholder="描述物品外观、特征、时间等信息,不少于 10 个字"
          />
        </n-form-item>

        <n-form-item path="contact" label="联系方式">
          <n-input v-model:value="form.contact" placeholder="电话 / QQ / 微信" />
        </n-form-item>

        <n-form-item label="图片(选填)">
          <image-upload ref="uploadRef" v-model="form.images" :max="4" />
        </n-form-item>

        <div class="lost-publish__actions">
          <n-button size="large" @click="router.back()">取消</n-button>
          <n-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            发布
          </n-button>
        </div>
      </n-form>
    </div>
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.lost-publish {
  padding: 32px;

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 8px;
  }

  @media (max-width: 640px) {
    padding: 20px;
  }
}
</style>
