<script setup lang="ts">
import brandMark from '@/assets/brand-mark.png'

// 品牌图腾:校门 + 购物袋 + 树叶 + 循环箭头。
// 图标自带透明底与多色,统一摆在白色圆角方块里,尺寸由调用方给。
withDefaults(defineProps<{ size?: number }>(), { size: 22 })
</script>

<template>
  <img
    class="brand-mark"
    :src="brandMark"
    :width="size"
    :height="size"
    :style="{ width: `${size}px`, height: `${size}px` }"
    alt=""
    aria-hidden="true"
    draggable="false"
  />
</template>

<style scoped lang="scss">
.brand-mark {
  display: block;
  object-fit: contain;
  user-select: none;
  pointer-events: none;
}
</style>
