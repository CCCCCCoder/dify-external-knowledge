import { get, download } from '../utils/request'

export const logApi = {
  getLogs(params = {}) {
    return get('/logs/recent', params)
  },

  getLogDetail(id) {
    return get(`/logs/${id}`)
  },

  getLogStatistics(params = {}) {
    return get('/logs/stats', params)
  },

  exportLogs(params = {}) {
    return download('/logs/export', params)
  },

  getRecentLogs(limit = 100) {
    return get('/logs/recent', { limit })
  }
}

// 日志状态枚举
export const LOG_STATUS = {
  SUCCESS: 'success',
  ERROR: 'error',
  PENDING: 'pending'
}

// 提供者类型枚举
export const PROVIDER_TYPES = {
  BAILIAN: 'bailian',
  RAGFLOW: 'ragflow'
}

// 请求方法枚举
export const REQUEST_METHODS = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE'
}