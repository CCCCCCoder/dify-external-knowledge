import { get, download } from '../utils/request'

// 日志管理相关API
export const logApi = {
  // 获取日志列表
  getLogs(params = {}) {
    return get('/logs', params)
  },

  // 获取日志详情
  getLogDetail(id) {
    return get(`/logs/${id}`)
  },

  // 获取日志统计
  getLogStatistics(params = {}) {
    return get('/logs/stats', params)
  },

  // 导出日志
  exportLogs(params = {}) {
    return download('/logs/export', params)
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