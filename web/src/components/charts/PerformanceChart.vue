<script setup>
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent } from 'echarts/components'

use([CanvasRenderer, BarChart, TitleComponent, TooltipComponent, GridComponent])

const props = defineProps({
  bailianTime: {
    type: Number,
    default: 0
  },
  ragflowTime: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartOption = computed(() => {
  return {
    title: {
      text: '平均响应时间 (ms)',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal',
        color: '#303133'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const item = params[0]
        return `${item.name}<br/>响应时间: ${item.value}ms`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['百炼', 'RAGflow'],
      axisLabel: {
        color: '#606266'
      },
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#606266',
        formatter: (value) => value + 'ms'
      },
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: '响应时间',
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          color: (params) => {
            const colors = ['#409eff', '#67c23a']
            return colors[params.dataIndex]
          },
          borderRadius: [4, 4, 0, 0]
        },
        data: [
          { value: props.bailianTime, itemStyle: { color: '#409eff' } },
          { value: props.ragflowTime, itemStyle: { color: '#67c23a' } }
        ]
      }
    ]
  }
})

const hasData = computed(() => {
  return props.bailianTime > 0 || props.ragflowTime > 0
})
</script>

<template>
  <div class="performance-chart">
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
.performance-chart {
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
