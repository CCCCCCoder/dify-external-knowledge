import { defineStore } from 'pinia'
import { logApi } from '../../api/log'

export const useLogStore = defineStore('log', {
  state: () => ({
    logs: [],
    logDetail: null,
    statistics: null,
    pagination: {
      total: 0,
      page: 1,
      size: 20
    },
    queryParams: {
      knowledgeId: '',
      providerType: '',
      startDate: '',
      endDate: '',
      status: '',
      keyword: ''
    },
    loading: {
      list: false,
      detail: false,
      statistics: false
    },
    eventSource: null,
    connected: false
  }),

  getters: {
    getAllLogs: (state) => state.logs,
    getLogDetail: (state) => state.logDetail,
    getLogStatistics: (state) => state.statistics,
    getPagination: (state) => state.pagination,
    getQueryParams: (state) => state.queryParams,
    getLoadingStatus: (state) => (type) => {
      return state.loading[type] || false
    }
  },

  actions: {
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

    setQueryParams(params) {
      this.queryParams = { ...this.queryParams, ...params }
    },

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

    setPagination(pagination) {
      this.pagination = { ...this.pagination, ...pagination }
    },

    clearLogDetail() {
      this.logDetail = null
    },

    clearLogStatistics() {
      this.statistics = null
    },

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

    startRealTimeLogs() {
      if (this.eventSource) {
        return
      }

      const apiBase = import.meta.env.VITE_API_BASE_URL || ''
      const eventSource = new EventSource(`${apiBase}/api/logs/stream?bufferSize=100`)
      
      eventSource.onmessage = (event) => {
        try {
          const log = JSON.parse(event.data)
          this.logs.unshift(log)
          if (this.logs.length > 500) {
            this.logs.pop()
          }
        } catch (e) {
          console.error('Failed to parse log event:', e)
        }
      }

      eventSource.onerror = () => {
        this.stopRealTimeLogs()
        setTimeout(() => {
          if (!this.eventSource) {
            this.startRealTimeLogs()
          }
        }, 3000)
      }

      this.eventSource = eventSource
      this.connected = true
    },

    stopRealTimeLogs() {
      if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
        this.connected = false
      }
    }
  }
})
