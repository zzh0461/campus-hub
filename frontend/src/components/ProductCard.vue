<script setup lang="ts">
import { computed } from 'vue'
import { NIcon } from 'naive-ui'
import { PhEye, PhHeart, PhTag } from '@phosphor-icons/vue'
import type { Product } from '@/types/market'
import { formatPrice, formatRelative } from '@/utils/format'
import StatusTag from './StatusTag.vue'
import UserAvatar from './UserAvatar.vue'

const props = defineProps<{
  product: Product
}>()

const cover = computed(() => props.product.images[0] || '')
</script>

<template>
  <router-link
    :to="`/market/products/${product.id}`"
    class="product-card lift"
    :aria-label="product.title"
  >
    <div class="product-card__media">
      <img v-if="cover" :src="cover" :alt="product.title" loading="lazy" />
      <div v-else class="product-card__placeholder" />
      <div class="product-card__status">
        <status-tag :value="product.status" />
      </div>
    </div>
    <div class="product-card__body">
      <h3 class="product-card__title">{{ product.title }}</h3>
      <div class="product-card__price-row">
        <span class="product-card__price num">{{ formatPrice(product.price) }}</span>
        <span v-if="product.originalPrice > product.price" class="product-card__origin num">
          {{ formatPrice(product.originalPrice) }}
        </span>
      </div>
      <div class="product-card__chips">
        <span class="product-card__chip">
          <n-icon :size="12" :component="PhTag" />
          {{ product.categoryName }}
        </span>
        <span class="product-card__time text-tertiary">{{ formatRelative(product.createdAt) }}</span>
      </div>
      <div class="product-card__seller">
        <user-avatar :avatar="product.sellerAvatar" :name="product.sellerName" :size="22" />
        <span class="product-card__seller-name">{{ product.sellerName }}</span>
        <div class="product-card__stats">
          <n-icon :size="13" :component="PhHeart" />
          <span class="num">{{ product.favoriteCount }}</span>
          <n-icon :size="13" :component="PhEye" />
          <span class="num">{{ product.viewCount }}</span>
        </div>
      </div>
    </div>
  </router-link>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.product-card {
  display: flex;
  flex-direction: column;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;

  &:hover {
    border-color: rgba(22, 163, 74, 0.35);
  }

  &__media {
    position: relative;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    background: $color-surface-soft;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    }
  }

  &:hover &__media img {
    transform: scale(1.05);
  }

  &__placeholder {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, $color-surface-soft 0%, #e9edf1 100%);
  }

  &__status {
    position: absolute;
    top: 10px;
    left: 10px;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 14px 16px 14px;
    flex: 1;
  }

  &__title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    line-height: 1.45;
    color: $color-text;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 43px;
  }

  &__price-row {
    display: flex;
    align-items: baseline;
    gap: 8px;
  }

  &__price {
    font-size: 19px;
    font-weight: 700;
    letter-spacing: -0.01em;
    color: $color-price;
  }

  &__origin {
    font-size: 12px;
    color: $color-text-tertiary;
    text-decoration: line-through;
  }

  &__chips {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
  }

  &__chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 2px 8px;
    border-radius: 6px;
    color: $color-accent;
    background: $color-accent-soft;
  }

  &__time {
    font-size: 12px;
  }

  &__seller {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-top: 10px;
    border-top: 1px solid $color-border;
    margin-top: auto;
  }

  &__seller-name {
    font-size: 12px;
    color: $color-text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__stats {
    margin-left: auto;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 4px;
    color: $color-text-tertiary;
    font-size: 12px;

    span + .n-icon {
      margin-left: 6px;
    }
  }
}
</style>
