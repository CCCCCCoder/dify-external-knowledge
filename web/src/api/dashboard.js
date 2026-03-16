import { get } from '../utils/request'

// 仪表板相关API
export const dashboardApi = {
  // 获取仪表板概览数据
  getOverview() {
    return get('/dashboard/overview')
  },

  // 获取请求统计
  getRequestStats(params = {}) {
    return get('/dashboard/request-stats', params)
  },

  // 获取提供者统计
  getProviderStats(params = {}) {
    return get('/dashboard/provider-stats', params)
  },

  // 获取性能指标
  getPerformanceMetrics(params = {}) {
    return get('/dashboard/performance-metrics', params)
  },

  // 获取错误统计
  getErrorStats(params = {}) {
    return get('/dashboard/error-stats', params)
  },

  // 获取实时指标
  getRealTimeMetrics() {
    return get('/dashboard/real-time-metrics')
  }
}

// 时间范围枚举
export const TIME_RANGES = {
  TODAY: 'today',
  YESTERDAY: 'yesterday',
  LAST_7_DAYS: 'last_7_days',
  LAST_30_DAYS: 'last_30_days',
  LAST_90_DAYS: 'last_90_days',
  CUSTOM: 'custom'
}

// 指标类型枚举
export const METRIC_TYPES = {
  REQUEST_COUNT: 'request_count',
  SUCCESS_RATE: 'success_rate',
  RESPONSE_TIME: 'response_time',
  ERROR_RATE: 'error_rate',
  THROUGHPUT: 'throughput'
}