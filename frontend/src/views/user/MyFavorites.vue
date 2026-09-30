<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NButton, NIcon, useMessage } from 'naive-ui'
import { PhHeart } from '@phosphor-icons/vue'
import { getMyFavorites } from '@/api/user'
import { unfavoriteProduct } from '@/api/market'
import type { Product } from '@/types/market'
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
  bind,
} = usePagination<Product>({ initialPageSize: 12 })

async function fetchList(): Promise<void> {
  // 卖家昵称/头像由 market-service 跨服务填充，经 user-service 的 ProductVO 契约透传，
  // 收藏列表这一次响应里就带齐了，不需要再逐个回查商品详情
  await load((p) => getMyFavorites(p))
}

async function handleUnfavorite(product: Product): Promise<void> {
  try {
    await unfavoriteProduct(product.id)
    if (result.value) {
      result.value.records = result.value.records.filter((p) => p.id !== product.id)
      total.value = Math.max(0, total.value - 1)
    }
    message.success('已取消收藏')
  } catch {
    // 错误提示由 request 层统一处理
  }
}

bind(fetchList)
</script>

<template>
  <page-container title="我的收藏" subtitle="收藏的好物,想好了就早点下手">
    <div v-if="loading">
      <loading-state :rows="6" variant="grid" />
    </div>
    <div v-else-if="error" class="favorite-error">
      <empty-state
        title="收藏加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>
    <div v-else-if="result?.records.length" class="favorite-grid">
      <div
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        class="favorite-item stagger-item"
        :style="{ '--i': index }"
      >
        <product-card :product="item" />
        <n-button
          size="small"
          quaternary
          class="favorite-item__remove"
          @click="handleUnfavorite(item)"
        >
          <template #icon>
            <n-icon :component="PhHeart" />
          </template>
          取消收藏
        </n-button>
      </div>
    </div>
    <empty-state
      v-else
      title="还没有收藏商品"
      description="在商品详情页点击收藏,就能在这里随时查看"
      action-text="去逛逛市场"
      @action="router.push('/market')"
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

.favorite-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 992px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 560px) {
    grid-template-columns: 1fr;
  }
}

.favorite-item {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &__remove {
    align-self: center;
    color: $color-text-secondary;
  }
}
</style>
