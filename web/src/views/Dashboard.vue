<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAppStore } from '../store'
import { dashboardApi, TIME_RANGES } from '../api'
import { ElMessage } from 'element-plus'
import RequestTrendChart from '../components/charts/RequestTrendChart.vue'
import ProviderDistChart from '../components/charts/ProviderDistChart.vue'
import PerformanceChart from '../components/charts/PerformanceChart.vue'

const appStore = useAppStore()

// 响应式数据
const loading = ref(false)
const timeRange = ref(TIME_RANGES.LAST_7_DAYS)
const overviewData = ref({
  totalRequests: 0,
  successRate: 0,
  avgResponseTime: 0,
  activeMappings: 0
})

const requestStats = ref({
  dates: [],
  counts: []
})

const providerStats = ref([])

const performanceMetrics = ref({
  cpu: 0,
  memory: 0,
  disk: 0,
  network: 0,
  bailianTime: 0,
  ragflowTime: 0
})

const errorStats = ref({
  errors: [],
  total: 0
})

// 计算属性
const successRateColor = computed(() => {
  const rate = overviewData.value.successRate
  if (rate >= 95) return '#67c23a'
  if (rate >= 90) return '#e6a23c'
  return '#f56c6c'
})

const responseTimeColor = computed(() => {
  const time = overviewData.value.avgResponseTime
  if (time <= 1000) return '#67c23a'
  if (time <= 2000) return '#e6a23c'
  return '#f56c6c'
})

// 获取仪表板数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 获取概览数据
    const overviewRes = await dashboardApi.getOverview()
    overviewData.value = overviewRes.data || {
      totalRequests: 0,
      successRate: 0,
      avgResponseTime: 0,
      activeMappings: 0
    }

    // 获取请求统计
    const requestRes = await dashboardApi.getRequestStats({
      timeRange: timeRange.value
    })
    requestStats.value = requestRes.data || {
      dates: [],
      counts: []
    }

    // 获取提供者统计
    const providerRes = await dashboardApi.getProviderStats({
      timeRange: timeRange.value
    })
    providerStats.value = providerRes.data || []

    // 获取性能指标
    const performanceRes = await dashboardApi.getPerformanceMetrics()
    performanceMetrics.value = performanceRes.data || {
      cpu: 0,
      memory: 0,
      disk: 0,
      network: 0
    }

    // 获取错误统计
    const errorRes = await dashboardApi.getErrorStats({
      timeRange: timeRange.value
    })
    errorStats.value = errorRes.data || {
      errors: [],
      total: 0
    }
  } catch (error) {
    console.error('获取仪表板数据失败:', error)
    ElMessage.error('获取仪表板数据失败')
  } finally {
    loading.value = false
  }
}

// 时间范围变化
const handleTimeRangeChange = () => {
  fetchDashboardData()
}

// 刷新数据
const refreshData = () => {
  fetchDashboardData()
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 格式化百分比
const formatPercentage = (num) => {
  return (num * 100).toFixed(1) + '%'
}

// 格式化响应时间
const formatResponseTime = (ms) => {
  if (ms >= 1000) {
    return (ms / 1000).toFixed(2) + 's'
  }
  return ms.toFixed(0) + 'ms'
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDashboardData()
})
</script>

<template>
  <div class="dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h2>仪表板</h2>
        <p>系统运行状态和关键指标概览</p>
      </div>
      <div class="header-right">
        <el-select v-model="timeRange" @change="handleTimeRangeChange" style="width: 150px">
          <el-option label="今天" :value="TIME_RANGES.TODAY" />
          <el-option label="昨天" :value="TIME_RANGES.YESTERDAY" />
          <el-option label="最近7天" :value="TIME_RANGES.LAST_7_DAYS" />
          <el-option label="最近30天" :value="TIME_RANGES.LAST_30_DAYS" />
          <el-option label="最近90天" :value="TIME_RANGES.LAST_90_DAYS" />
        </el-select>
        <el-button type="primary" :icon="Refresh" @click="refreshData" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon requests">
              <el-icon size="32"><TrendCharts /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">总请求数</div>
              <div class="card-value">{{ formatNumber(overviewData.totalRequests) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon success-rate" :style="{ color: successRateColor }">
              <el-icon size="32"><SuccessFilled /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">成功率</div>
              <div class="card-value" :style="{ color: successRateColor }">
                {{ formatPercentage(overviewData.successRate) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon response-time" :style="{ color: responseTimeColor }">
              <el-icon size="32"><Timer /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">平均响应时间</div>
              <div class="card-value" :style="{ color: responseTimeColor }">
                {{ formatResponseTime(overviewData.avgResponseTime) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon mappings">
              <el-icon size="32"><Connection /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">活跃映射</div>
              <div class="card-value">{{ overviewData.activeMappings }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>请求趋势</span>
              <el-tag type="info">{{ timeRange === TIME_RANGES.TODAY ? '今天' : timeRange === TIME_RANGES.YESTERDAY ? '昨天' : timeRange === TIME_RANGES.LAST_7_DAYS ? '最近7天' : timeRange === TIME_RANGES.LAST_30_DAYS ? '最近30天' : '最近90天' }}</el-tag>
            </div>
          </template>
          <div class="chart-container">
            <RequestTrendChart 
              :dates="requestStats.dates" 
              :counts="requestStats.counts"
              :loading="loading" 
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>提供者分布</span>
            </div>
          </template>
          <div class="chart-container">
            <ProviderDistChart 
              :data="providerStats"
              :loading="loading"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 性能指标和错误统计 -->
    <el-row :gutter="20" class="metrics-section">
      <el-col :xs="24" :lg="12">
        <el-card class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>性能指标</span>
            </div>
          </template>
          <div class="metrics-grid">
            <div class="metrics-left">
              <div class="metric-item">
                <div class="metric-label">CPU使用率</div>
                <el-progress 
                  :percentage="performanceMetrics.cpu" 
                  :color="performanceMetrics.cpu > 80 ? '#f56c6c' : performanceMetrics.cpu > 60 ? '#e6a23c' : '#67c23a'"
                />
              </div>
              <div class="metric-item">
                <div class="metric-label">内存使用率</div>
                <el-progress 
                  :percentage="performanceMetrics.memory" 
                  :color="performanceMetrics.memory > 80 ? '#f56c6c' : performanceMetrics.memory > 60 ? '#e6a23c' : '#67c23a'"
                />
              </div>
              <div class="metric-item">
                <div class="metric-label">磁盘使用率</div>
                <el-progress 
                  :percentage="performanceMetrics.disk" 
                  :color="performanceMetrics.disk > 80 ? '#f56c6c' : performanceMetrics.disk > 60 ? '#e6a23c' : '#67c23a'"
                />
              </div>
              <div class="metric-item">
                <div class="metric-label">网络使用率</div>
                <el-progress 
                  :percentage="performanceMetrics.network" 
                  :color="performanceMetrics.network > 80 ? '#f56c6c' : performanceMetrics.network > 60 ? '#e6a23c' : '#67c23a'"
                />
              </div>
            </div>
            <div class="metrics-right">
              <PerformanceChart 
                :bailian-time="performanceMetrics.bailianTime"
                :ragflow-time="performanceMetrics.ragflowTime"
                :loading="loading"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>错误统计</span>
              <el-tag type="danger" v-if="errorStats.total > 0">{{ errorStats.total }}</el-tag>
            </div>
          </template>
          <div class="error-content">
            <div v-if="errorStats.errors.length === 0" class="empty-errors">
              <el-empty description="暂无错误记录" />
            </div>
            <div v-else class="error-list">
              <div v-for="error in errorStats.errors.slice(0, 5)" :key="error.id" class="error-item">
                <div class="error-info">
                  <span class="error-type">{{ error.type }}</span>
                  <span class="error-time">{{ error.time }}</span>
                </div>
                <div class="error-message">{{ error.message }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-left p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 10px;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: #409eff;
}

.card-icon.requests {
  background-color: #ecf5ff;
}

.card-icon.success-rate {
  background-color: #f0f9ff;
}

.card-icon.response-time {
  background-color: #fdf6ec;
}

.card-icon.mappings {
  background-color: #f0f9ff;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: calc(100% - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-chart {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.metrics-section {
  margin-bottom: 20px;
}

.metrics-card {
  min-height: 350px;
}

.metrics-grid {
  display: flex;
  gap: 20px;
  height: calc(100% - 60px);
}

.metrics-left {
  flex: 1;
  padding: 10px 0;
}

.metrics-right {
  flex: 1;
  min-height: 250px;
}

.metrics-content {
  padding: 10px 0;
}

.metric-item {
  margin-bottom: 20px;
}

.metric-label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.error-content {
  height: calc(100% - 60px);
  overflow: auto;
}

.empty-errors {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-list {
  padding: 10px 0;
}

.error-item {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.error-item:last-child {
  border-bottom: none;
}

.error-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.error-type {
  font-weight: 500;
  color: #f56c6c;
}

.error-time {
  color: #909399;
  font-size: 12px;
}

.error-message {
  color: #606266;
  font-size: 13px;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-end;
  }
  
  .overview-card {
    height: 100px;
    margin-bottom: 10px;
  }
  
  .card-icon {
    width: 50px;
    height: 50px;
    margin-right: 10px;
  }
  
  .card-value {
    font-size: 20px;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .metrics-card {
    height: 250px;
  }
}
</style>