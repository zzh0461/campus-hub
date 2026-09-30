<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NIcon, NInput, NInputNumber, NSelect } from 'naive-ui'
import { PhPlus, PhMagnifyingGlass } from '@phosphor-icons/vue'
import { getProductCategories, getProducts, searchProducts } from '@/api/market'
import type { Product, ProductCategory, ProductSort } from '@/types/market'
import { usePagination } from '@/utils/usePagination'

const route = useRoute()
const router = useRouter()

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
} = usePagination<Product>({ initialPageSize: 12 })

const categories = ref<ProductCategory[]>([])

const filters = reactive({
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
  categoryId:
    typeof route.query.categoryId === 'string' && !Number.isNaN(Number(route.query.categoryId))
      ? Number(route.query.categoryId)
      : null,
  minPrice: null as number | null,
  maxPrice: null as number | null,
  sort: (typeof route.query.sort === 'string' &&
  ['latest', 'priceAsc', 'priceDesc'].includes(route.query.sort)
    ? route.query.sort
    : 'latest') as ProductSort,
})

const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '价格从低到高', value: 'priceAsc' },
  { label: '价格从高到低', value: 'priceDesc' },
]

async function loadCategories(): Promise<void> {
  try {
    categories.value = await getProductCategories()
  } catch {
    categories.value = []
  }
}

async function fetchList(): Promise<void> {
  // 有关键词走 ES 搜索接口,否则走普通分页接口
  await load((common) =>
    filters.keyword.trim().length > 0
      ? searchProducts({
          keyword: filters.keyword.trim(),
          categoryId: filters.categoryId ?? undefined,
          ...common,
        })
      : getProducts({
          ...common,
          keyword: undefined,
          categoryId: filters.categoryId ?? undefined,
          minPrice: filters.minPrice ?? undefined,
          maxPrice: filters.maxPrice ?? undefined,
        }),
  )
}

async function handleSearch(): Promise<void> {
  // 筛选条件或关键词变了必须回到第 1 页,否则会留在越界页码上(空列表)
  await reset(fetchList)
}

function selectCategory(id: number | null): void {
  filters.categoryId = id
  void handleSearch()
}

bind(fetchList)

onMounted(() => {
  void loadCategories()
})
</script>

<template>
  <page-container title="二手市场" subtitle="校园闲置好物,毕业回血与淘货圣地">
    <template #extra>
      <n-button type="primary" @click="router.push('/market/publish')">
        <template #icon>
          <n-icon :component="PhPlus" />
        </template>
        发布商品
      </n-button>
    </template>

    <div class="market-filter">
      <div class="market-filter__row">
        <n-input
          v-model:value="filters.keyword"
          placeholder="搜索商品,如「机械键盘」"
          clearable
          size="large"
          class="market-filter__keyword"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="PhMagnifyingGlass" />
          </template>
        </n-input>
        <n-button type="primary" size="large" class="market-filter__search-btn" @click="handleSearch">
          搜索
        </n-button>
      </div>
      <div class="market-filter__row market-filter__row--cats">
        <button
          type="button"
          class="market-filter__cat"
          :class="{ 'market-filter__cat--active': filters.categoryId === null }"
          @click="selectCategory(null)"
        >
          全部
        </button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="market-filter__cat"
          :class="{ 'market-filter__cat--active': filters.categoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </button>
      </div>
      <div class="market-filter__row">
        <div class="market-filter__price">
          <n-input-number
            v-model:value="filters.minPrice"
            placeholder="最低价"
            :min="0"
            class="market-filter__price-input"
          />
          <span class="text-tertiary">—</span>
          <n-input-number
            v-model:value="filters.maxPrice"
            placeholder="最高价"
            :min="0"
            class="market-filter__price-input"
          />
        </div>
        <n-select
          v-model:value="filters.sort"
          :options="sortOptions"
          class="market-filter__sort"
          @update:value="handleSearch"
        />
        <n-button quaternary @click="handleSearch">应用筛选</n-button>
      </div>
    </div>

    <p class="market-result text-tertiary">
      <template v-if="filters.keyword">
        搜索「{{ filters.keyword }}」,共 {{ total }} 件商品
      </template>
      <template v-else>共 {{ total }} 件在售商品</template>
    </p>

    <div v-if="loading">
      <loading-state :rows="6" variant="grid" />
    </div>

    <div v-else-if="error" class="market-error">
      <empty-state
        title="商品加载失败"
        description="请检查网络或后端服务后重试"
        action-text="重新加载"
        @action="fetchList"
      />
    </div>

    <div v-else-if="result?.records.length" class="market-grid">
      <product-card
        v-for="(item, index) in result?.records ?? []"
        :key="item.id"
        :product="item"
        class="stagger-item"
        :style="{ '--i': index }"
      />
    </div>

    <empty-state
      v-else
      title="没有找到合适的商品"
      description="试试调整关键词或价格区间"
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

.market-filter {
  padding: 20px;
  margin-bottom: 18px;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  box-shadow: $shadow-soft;

  &__row {
    display: flex;
    gap: 12px;
    align-items: center;

    & + & {
      margin-top: 14px;
      padding-top: 14px;
      border-top: 1px solid $color-border;
    }
  }

  &__keyword {
    flex: 1;
  }

  &__search-btn {
    padding: 0 28px;
    font-weight: 600;
  }

  &__row--cats {
    flex-wrap: wrap;
    gap: 8px;
  }

  &__cat {
    padding: 6px 14px;
    border: 1px solid $color-border;
    border-radius: 999px;
    background: $color-surface;
    font-size: 13px;
    color: $color-text-secondary;
    cursor: pointer;
    transition: $transition-fast;

    &:hover {
      color: $color-accent;
      border-color: rgba(22, 163, 74, 0.45);
    }

    &--active {
      color: #fff;
      background: $color-accent;
      border-color: $color-accent;

      &:hover {
        color: #fff;
      }
    }
  }

  &__price {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__price-input {
    width: 150px;
  }

  &__sort {
    width: 170px;
    margin-left: auto;
  }

  @media (max-width: 768px) {
    &__row {
      flex-wrap: wrap;
    }

    &__sort,
    &__price {
      flex: 1;
      min-width: 140px;
    }

    &__keyword {
      flex-basis: 100%;
    }
  }
}

.market-result {
  margin: 4px 2px 16px;
  font-size: 13px;
}

.market-error {
  border: 1px dashed $color-border-strong;
  border-radius: $radius-md;
}

.market-grid {
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
</style>
