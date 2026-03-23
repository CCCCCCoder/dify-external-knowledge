<script setup>
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, PieChart, TitleComponent, TooltipComponent, LegendComponent])

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartOption = computed(() => {
  const chartData = props.data.map(item => ({
    name: item.type === 'bailian' ? '百炼' : item.type === 'ragflow' ? 'RAGflow' : item.type,
    value: item.count
  }))

  const total = chartData.reduce((sum, item) => sum + item.value, 0)

  return {
    title: {
      text: '提供者分布',
      subtext: `总计: ${total}`,
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal',
        color: '#303133'
      },
      subtextStyle: {
        fontSize: 12,
        color: '#909399'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}<br/>请求数: ${params.value}<br/>占比: ${params.percent}%`
      }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#606266'
      }
    },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
    series: [
      {
        name: '提供者',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '55%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: chartData
      }
    ]
  }
})

const hasData = computed(() => {
  return props.data.length > 0 && props.data.some(item => item.count > 0)
})
</script>

<template>
  <div class="provider-dist-chart">
    <div v-if="loading" class="chart-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-else-if="!hasData" class="chart-empty">
      <el-empty description="暂无数据" />
    </div>
    <v-chart v-else :option="chartOption" autoresize style="height: 100%; width: 100%;" />
  </div>
</template>

<style scoped>
.provider-dist-chart {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-loading,
.chart-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #909399;
}
</style>
