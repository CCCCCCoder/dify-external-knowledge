// 统一导出所有API服务
export { configApi, RAG_PROVIDER_TYPES, MAPPING_STATUS, OPERATION_TYPES, CONFIG_TYPES } from './config'
export { logApi, LOG_STATUS, PROVIDER_TYPES, REQUEST_METHODS } from './log'
export { dashboardApi, TIME_RANGES, METRIC_TYPES } from './dashboard'

// 导出所有API方法的对象，方便使用
export const api = {
  config: {
    getMappings: (...args) => import('./config').then(m => m.configApi.getMappings(...args)),
    createMapping: (...args) => import('./config').then(m => m.configApi.createMapping(...args)),
    updateMapping: (...args) => import('./config').then(m => m.configApi.updateMapping(...args)),
    deleteMapping: (...args) => import('./config').then(m => m.configApi.deleteMapping(...args)),
    getRagConfig: (...args) => import('./config').then(m => m.configApi.getRagConfig(...args)),
    updateRagConfig: (...args) => import('./config').then(m => m.configApi.updateRagConfig(...args)),
    testRagConfig: (...args) => import('./config').then(m => m.configApi.testRagConfig(...args)),
    getConfigHistory: (...args) => import('./config').then(m => m.configApi.getConfigHistory(...args)),
    rollbackConfig: (...args) => import('./config').then(m => m.configApi.rollbackConfig(...args)),
    importConfig: (...args) => import('./config').then(m => m.configApi.importConfig(...args)),
    exportConfig: (...args) => import('./config').then(m => m.configApi.exportConfig(...args))
  },
  log: {
    getLogs: (...args) => import('./log').then(m => m.logApi.getLogs(...args)),
    getLogDetail: (...args) => import('./log').then(m => m.logApi.getLogDetail(...args)),
    getLogStatistics: (...args) => import('./log').then(m => m.logApi.getLogStatistics(...args)),
    exportLogs: (...args) => import('./log').then(m => m.logApi.exportLogs(...args))
  },
  dashboard: {
    getOverview: (...args) => import('./dashboard').then(m => m.dashboardApi.getOverview(...args)),
    getRequestStats: (...args) => import('./dashboard').then(m => m.dashboardApi.getRequestStats(...args)),
    getProviderStats: (...args) => import('./dashboard').then(m => m.dashboardApi.getProviderStats(...args)),
    getPerformanceMetrics: (...args) => import('./dashboard').then(m => m.dashboardApi.getPerformanceMetrics(...args)),
    getErrorStats: (...args) => import('./dashboard').then(m => m.dashboardApi.getErrorStats(...args)),
    getRealTimeMetrics: (...args) => import('./dashboard').then(m => m.dashboardApi.getRealTimeMetrics(...args))
  }
}