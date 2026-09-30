<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

echarts.use([
  BarChart,
  LineChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  CanvasRenderer,
])

const props = withDefaults(
  defineProps<{
    option: EChartsOption
    height?: number
  }>(),
  {
    height: 280,
  },
)

const container = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

function render(): void {
  if (container.value) {
    chart = echarts.init(container.value)
    chart.setOption(props.option)
  }
}

onMounted(() => {
  render()
  if (container.value) {
    observer = new ResizeObserver(() => {
      chart?.resize()
    })
    observer.observe(container.value)
  }
})

watch(
  () => props.option,
  () => {
    chart?.setOption(props.option, true)
  },
  { deep: true },
)

onBeforeUnmount(() => {
  observer?.disconnect()
  chart?.dispose()
})
</script>

<template>
  <div ref="container" class="chart-panel" :style="{ height: `${height}px` }" />
</template>

<style scoped lang="scss">
.chart-panel {
  width: 100%;
}
</style>
