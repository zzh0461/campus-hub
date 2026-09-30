<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NIcon } from 'naive-ui'
import {
  PhTote,
  PhCalendarBlank,
  PhLifebuoy,
  PhUsers,
  PhChartLineUp,
  PhTrendUp,
} from '@phosphor-icons/vue'
import type { EChartsOption } from 'echarts'
import { getDashboard } from '@/api/dashboard'
import type { DashboardData } from '@/types/dashboard'

const loading = ref(true)
const error = ref(false)
const data = ref<DashboardData | null>(null)

const statCards = computed(() => {
  if (!data.value) return []
  const stats = data.value.stats
  return [
    { label: '用户总数', value: stats.userTotal, icon: PhUsers, tone: 'green' },
    { label: '商品总数', value: stats.productTotal, icon: PhTote, tone: 'amber' },
    { label: '活动总数', value: stats.activityTotal, icon: PhCalendarBlank, tone: 'blue' },
    { label: '失物记录', value: stats.lostFoundTotal, icon: PhLifebuoy, tone: 'rose' },
    { label: '今日新增用户', value: stats.todayNewUsers, icon: PhTrendUp, tone: 'green' },
    { label: '今日新增商品', value: stats.todayNewProducts, icon: PhTrendUp, tone: 'amber' },
    { label: '今日活动报名', value: stats.todayRegistrations, icon: PhTrendUp, tone: 'blue' },
  ]
})

function lineOption(points: Array<{ date: string; value: number }>, name: string): EChartsOption {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1f2329',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
    },
    grid: { left: 8, right: 12, top: 26, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: points.map((p) => p.date),
      axisLine: { lineStyle: { color: '#d8dce2' } },
      axisTick: { show: false },
      axisLabel: { color: '#9aa1ab', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#e8eaee' } },
      axisLabel: { color: '#9aa1ab', fontSize: 11 },
    },
    series: [
      {
        name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: points.map((p) => p.value),
        lineStyle: { width: 2.5, color: '#16a34a' },
        itemStyle: { color: '#16a34a' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(14,122,92,0.22)' },
              { offset: 1, color: 'rgba(14,122,92,0)' },
            ],
          },
        },
      },
    ],
  }
}

const userGrowthOption = computed<EChartsOption>(() =>
  lineOption(data.value?.userGrowth ?? [], '用户增长'),
)
const productTrendOption = computed<EChartsOption>(() =>
  lineOption(data.value?.productTrend ?? [], '商品发布'),
)
const registrationTrendOption = computed<EChartsOption>(() =>
  lineOption(data.value?.registrationTrend ?? [], '活动报名'),
)

const lostFoundOption = computed<EChartsOption>(() => {
  const stats = data.value?.lostFoundStats ?? []
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1f2329',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#6b7280', fontSize: 12 },
      itemWidth: 12,
      itemHeight: 12,
    },
    series: [
      {
        name: '失物招领',
        type: 'pie',
        radius: ['46%', '70%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 6 },
        label: { show: false },
        data: stats.map((item) => ({
          name: item.type === 'LOST' ? '失物' : '招领',
          value: item.count,
        })),
        color: ['#b3372a', '#16a34a'],
      },
    ],
  }
})

async function load(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    data.value = await getDashboard()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="dashboard">
    <div v-if="loading">
      <loading-state :rows="6" variant="panel" />
    </div>
    <div v-else-if="error" class="dashboard__error">
      <empty-state
        title="数据加载失败"
        description="请检查后端服务或稍后重试"
        action-text="重新加载"
        @action="load"
      />
    </div>
    <template v-else>
      <div class="dashboard__stats">
        <div
          v-for="(card, index) in statCards"
          :key="card.label"
          class="dashboard__stat"
          :class="`dashboard__stat--${card.tone}`"
          :style="{ '--i': index }"
        >
          <div class="dashboard__stat-icon">
            <n-icon :size="20" :component="card.icon" />
          </div>
          <div class="dashboard__stat-text">
            <span class="dashboard__stat-value num">{{ card.value }}</span>
            <span class="dashboard__stat-label">{{ card.label }}</span>
          </div>
        </div>
      </div>

      <div class="dashboard__charts">
        <section class="dashboard__chart panel">
          <header class="dashboard__chart-head">
            <h3>用户增长(近 14 天)</h3>
            <n-icon :size="16" :component="PhChartLineUp" class="text-tertiary" />
          </header>
          <chart-panel :option="userGrowthOption" :height="260" />
        </section>
        <section class="dashboard__chart panel">
          <header class="dashboard__chart-head">
            <h3>商品发布趋势(近 14 天)</h3>
            <n-icon :size="16" :component="PhTote" class="text-tertiary" />
          </header>
          <chart-panel :option="productTrendOption" :height="260" />
        </section>
        <section class="dashboard__chart panel">
          <header class="dashboard__chart-head">
            <h3>活动报名趋势(近 14 天)</h3>
            <n-icon :size="16" :component="PhCalendarBlank" class="text-tertiary" />
          </header>
          <chart-panel :option="registrationTrendOption" :height="260" />
        </section>
        <section class="dashboard__chart panel">
          <header class="dashboard__chart-head">
            <h3>失物招领统计</h3>
            <n-icon :size="16" :component="PhLifebuoy" class="text-tertiary" />
          </header>
          <chart-panel :option="lostFoundOption" :height="260" />
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.dashboard {
  &__error {
    border: 1px dashed $color-border-strong;
    border-radius: $radius-md;
    background: $color-surface;
  }

  &__stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 14px;
    margin-bottom: 24px;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 18px 20px;
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: $radius-md;
    border-top: 3px solid transparent;
    animation: fade-rise 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
    animation-delay: calc(var(--i) * 50ms);

    &--green {
      border-top-color: $color-accent;

      .dashboard__stat-icon {
        color: $color-accent;
        background: $color-accent-soft;
      }
    }

    &--amber {
      border-top-color: $color-warning;

      .dashboard__stat-icon {
        color: $color-warning;
        background: $color-warning-soft;
      }
    }

    &--blue {
      border-top-color: $color-info;

      .dashboard__stat-icon {
        color: $color-info;
        background: $color-info-soft;
      }
    }

    &--rose {
      border-top-color: $color-danger;

      .dashboard__stat-icon {
        color: $color-danger;
        background: $color-danger-soft;
      }
    }
  }

  &__stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 42px;
    height: 42px;
    flex-shrink: 0;
    border-radius: 12px;
  }

  &__stat-text {
    display: flex;
    flex-direction: column;
    gap: 1px;
  }

  &__stat-value {
    font-size: 22px;
    font-weight: 750;
    color: $color-text;
  }

  &__stat-label {
    font-size: 12px;
    color: $color-text-secondary;
  }

  &__charts {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 18px;
  }

  &__chart {
    padding: 20px 22px;
    box-shadow: none;
  }

  &__chart-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 650;
      color: $color-text;
    }
  }

  @media (max-width: 1100px) {
    &__stats {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 720px) {
    &__charts {
      grid-template-columns: 1fr;
    }
  }
}
</style>
