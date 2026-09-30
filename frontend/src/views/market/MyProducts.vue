<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NIcon,
  NPopconfirm,
  NTabs,
  NTabPane,
  useMessage,
} from 'naive-ui'
import { PhPencilSimple, PhImage } from '@phosphor-icons/vue'
import { deleteProduct, getMyProducts, updateProduct } from '@/api/market'
import type { Product, ProductStatus } from '@/types/market'
import { formatDateTime, formatPrice } from '@/utils/format'
import { usePagination } from '@/utils/usePagination'

const router = useRouter()
const message = useMessage()

const {
  result,
  pageNum,
  pageSize,
  total,
  loading,
  error,
  load,
  handlePageSizeChange,
  reset,
  bind,
} = usePagination<Product>()

const status = ref<ProductStatus | 'ALL'>('ALL')

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '在售', value: 'ON_SALE' },
  { label: '下架', value: 'OFF_SHELF' },
  { label: '已售出', value: 'SOLD' },
]

async function fetchList(): Promise<void> {
  await load((p) =>
    getMyProducts({
      ...p,
      status: status.value === 'ALL' ? undefined : status.value,
    }),
  )
}

function handleTabChange(value: string): void {
  status.value = value as ProductStatus | 'ALL'
  void reset(fetchList)
}

async function toggleStatus(product: Product): Promise<void> {
  const nextStatus: ProductStatus = product.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE'
  try {
    await updateProduct(product.id, {
      title: product.title,
      categoryId: product.categoryId,
      price: product.price,
      description: product.description,
      images: product.images,
      status: nextStatus,
    })
    product.status = nextStatus
    message.success(nextStatus === 'ON_SALE' ? '已上架' : '已下架')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function markSold(product: Product): Promise<void> {
  // 标记已售复用更新接口：只把 status 改成 SOLD，其余字段原样回传
  try {
    const updated = await updateProduct(product.id, {
      title: product.title,
      categoryId: product.categoryId,
      price: product.price,
      description: product.description,
      images: product.images,
      status: 'SOLD',
    })
    product.status = updated.status
    message.success('已标记为售出')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

async function removeItem(product: Product): Promise<void> {
  try {
    await deleteProduct(product.id)
    message.success('已删除')
    void fetchList()
  } catch {
    // 错误提示由 request 层统一处理
  }
}

bind(fetchList)
</script>

<template>
  <page-container title="我的商品" subtitle="管理你发布的闲置商品">
    <template #extra>
      <n-button type="primary" @click="router.push('/market/publish')">发布新商品</n-button>
    </template>

    <n-tabs :value="status" type="line" animated @update:value="handleTabChange">
      <n-tab-pane v-for="tab in tabs" :key="tab.value" :name="tab.value" :tab="tab.label" />
    </n-tabs>

    <div v-if="loading">
      <loading-state :rows="5" variant="panel" />
    </div>
    <div v-else-if="error" class="my-error">
      <empty-state
        title="商品加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <div v-else-if="result?.records.length" class="my-list">
      <div
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        class="my-item panel stagger-item"
        :style="{ '--i': index }"
      >
        <button
          type="button"
          class="my-item__img pressable"
          @click="router.push(`/market/products/${item.id}`)"
        >
          <img v-if="item.images[0]" :src="item.images[0]" :alt="item.title" loading="lazy" />
          <n-icon v-else :size="26" :component="PhImage" />
        </button>
        <div class="my-item__main">
          <router-link :to="`/market/products/${item.id}`" class="my-item__title">
            {{ item.title }}
          </router-link>
          <div class="my-item__meta text-tertiary">
            <span>{{ item.categoryName }}</span>
            <span>{{ formatDateTime(item.createdAt) }}</span>
            <span class="num">{{ item.viewCount }} 浏览 · {{ item.favoriteCount }} 收藏</span>
          </div>
          <div class="my-item__bottom">
            <status-tag :value="item.status" />
            <span class="my-item__price num">{{ formatPrice(item.price) }}</span>
          </div>
        </div>
        <div class="my-item__actions">
          <n-button size="small" @click="router.push(`/market/edit/${item.id}`)">
            <template #icon>
              <n-icon :component="PhPencilSimple" />
            </template>
            编辑
          </n-button>
          <n-popconfirm
            v-if="item.status !== 'SOLD'"
            @positive-click="toggleStatus(item)"
          >
            <template #trigger>
              <n-button size="small" quaternary>
                {{ item.status === 'ON_SALE' ? '下架' : '上架' }}
              </n-button>
            </template>
            {{ item.status === 'ON_SALE' ? '确认下架该商品?' : '确认重新上架该商品?' }}
          </n-popconfirm>
          <n-popconfirm
            v-if="item.status !== 'SOLD'"
            @positive-click="markSold(item)"
          >
            <template #trigger>
              <n-button size="small" quaternary>标记已售</n-button>
            </template>
            确认标记为已售出?标记后将从在售列表移除
          </n-popconfirm>
          <n-popconfirm @positive-click="removeItem(item)">
            <template #trigger>
              <n-button size="small" quaternary type="error">删除</n-button>
            </template>
            确认删除该商品?此操作不可恢复
          </n-popconfirm>
        </div>
      </div>
    </div>
    <empty-state
      v-else
      title="还没有发布过商品"
      description="把闲置挂出来,让它们在校园里找到新主人"
      action-text="发布第一件商品"
      @action="router.push('/market/publish')"
    />

    <pagination
      v-if="total > 0"
      v-model:page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:page-size="(size: number) => handlePageSizeChange(size, fetchList)"
    />
  </page-container>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.my-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.my-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.my-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 14px;
  box-shadow: none;

  &__img {
    width: 96px;
    height: 72px;
    flex-shrink: 0;
    border: none;
    border-radius: $radius-sm;
    overflow: hidden;
    padding: 0;
    background: $color-surface-soft;
    color: $color-text-tertiary;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: $color-text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &:hover {
      color: $color-accent;
    }
  }

  &__meta {
    display: flex;
    gap: 14px;
    font-size: 12px;
    flex-wrap: wrap;
  }

  &__bottom {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__price {
    font-size: 16px;
    font-weight: 700;
    color: $color-danger;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-shrink: 0;
  }

  @media (max-width: 640px) {
    flex-wrap: wrap;

    &__actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
