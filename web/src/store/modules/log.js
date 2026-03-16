import { defineStore } from 'pinia'
import { logApi } from '../../api/log'

export const useLogStore = defineStore('log', {
  state: () => ({
    // 日志列表
    logs: [],
    // 日志详情
    logDetail: null,
    // 日志统计
    statistics: null,
    // 分页信息
    pagination: {
      total: 0,
      page: 1,
      size: 20
    },
    // 查询参数
    queryParams: {
      knowledgeId: '',
      providerType: '',
      startDate: '',
      endDate: '',
      status: '',
      keyword: ''
    },
    // 加载状态
    loading: {
      list: false,
      detail: false,
      statistics: false
    }
  }),

  getters: {
    // 获取日志列表
    getAllLogs: (state) => state.logs,
    
    // 获取日志详情
    getLogDetail: (state) => state.logDetail,
    
    // 获取日志统计
    getLogStatistics: (state) => state.statistics,
    
    // 获取分页信息
    getPagination: (state) => state.pagination,
    
    // 获取查询参数
    getQueryParams: (state) => state.queryParams,
    
    // 获取加载状态
    getLoadingStatus: (state) => (type) => {
      return state.loading[type] || false
    }
  },

  actions: {
    // 获取日志列表
    async fetchLogs(params = {}) {
      this.loading.list = true
      try {
        const queryParams = { ...this.queryParams, ...params }
        const response = await logApi.getLogs(queryParams)
        
        this.logs = response.data.records || []
        this.pagination = {
          total: response.data.total || 0,
          page: response.data.page || 1,
          size: response.data.size || 20
        }
        
        return response.data
      } catch (error) {
        console.error('获取日志列表失败:', error)
        throw error
      } finally {
        this.loading.list = false
      }
    },

    // 获取日志详情
    async fetchLogDetail(id) {
      this.loading.detail = true
      try {
        const response = await logApi.getLogDetail(id)
        this.logDetail = response.data
        return response.data
      } catch (error) {
        console.error('获取日志详情失败:', error)
        throw error
      } finally {
        this.loading.detail = false
      }
    },

    // 获取日志统计
    async fetchLogStatistics(params = {}) {
      this.loading.statistics = true
      try {
        const response = await logApi.getLogStatistics(params)
        this.statistics = response.data
        return response.data
      } catch (error) {
        console.error('获取日志统计失败:', error)
        throw error
      } finally {
        this.loading.statistics = false
      }
    },

    // 设置查询参数
    setQueryParams(params) {
      this.queryParams = { ...this.queryParams, ...params }
    },

    // 重置查询参数
    resetQueryParams() {
      this.queryParams = {
        knowledgeId: '',
        providerType: '',
        startDate: '',
        endDate: '',
        status: '',
        keyword: ''
      }
    },

    // 设置分页信息
    setPagination(pagination) {
      this.pagination = { ...this.pagination, ...pagination }
    },

    // 清空日志详情
    clearLogDetail() {
      this.logDetail = null
    },

    // 清空日志统计
    clearLogStatistics() {
      this.statistics = null
    },

    // 导出日志
    async exportLogs(params = {}) {
      try {
        const queryParams = { ...this.queryParams, ...params }
        const response = await logApi.exportLogs(queryParams)
        return response.data
      } catch (error) {
        console.error('导出日志失败:', error)
        throw error
      }
    },

    // 实时日志流（WebSocket连接）
    startRealTimeLogs() {
      // 这里可以实现WebSocket连接来获取实时日志
      console.log('开始实时日志流')
    },

    // 停止实时日志流
    stopRealTimeLogs() {
      // 这里可以关闭WebSocket连接
      console.log('停止实时日志流')
    }
  }
})