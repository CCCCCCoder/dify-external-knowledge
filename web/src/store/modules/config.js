import { defineStore } from 'pinia'
import { configApi } from '../../api/config'

export const useConfigStore = defineStore('config', {
  state: () => ({
    // 知识库映射列表
    mappings: [],
    // RAG框架配置
    ragConfigs: {},
    // 配置历史
    configHistory: [],
    // 当前编辑的映射
    currentMapping: null,
    // 当前编辑的RAG配置
    currentRagConfig: null,
    // 加载状态
    loading: {
      mappings: false,
      ragConfigs: false,
      history: false
    }
  }),

  getters: {
    // 获取所有映射
    getAllMappings: (state) => state.mappings,
    
    // 根据ID获取映射
    getMappingById: (state) => (id) => {
      return state.mappings.find(mapping => mapping.id === id)
    },
    
    // 根据提供者类型获取RAG配置
    getRagConfigByType: (state) => (providerType) => {
      return state.ragConfigs[providerType]
    },
    
    // 获取所有RAG配置
    getAllRagConfigs: (state) => state.ragConfigs,
    
    // 获取配置历史
    getConfigHistory: (state) => state.configHistory,
    
    // 获取加载状态
    getLoadingStatus: (state) => (type) => {
      return state.loading[type] || false
    }
  },

  actions: {
    // 获取知识库映射列表
    async fetchMappings() {
      this.loading.mappings = true
      try {
        const response = await configApi.getMappings()
        this.mappings = response.data || []
        return this.mappings
      } catch (error) {
        console.error('获取知识库映射失败:', error)
        throw error
      } finally {
        this.loading.mappings = false
      }
    },

    // 创建知识库映射
    async createMapping(mappingData) {
      try {
        const response = await configApi.createMapping(mappingData)
        const newMapping = response.data
        this.mappings.push(newMapping)
        return newMapping
      } catch (error) {
        console.error('创建知识库映射失败:', error)
        throw error
      }
    },

    // 更新知识库映射
    async updateMapping(id, mappingData) {
      try {
        const response = await configApi.updateMapping(id, mappingData)
        const updatedMapping = response.data
        const index = this.mappings.findIndex(mapping => mapping.id === id)
        if (index !== -1) {
          this.mappings[index] = updatedMapping
        }
        return updatedMapping
      } catch (error) {
        console.error('更新知识库映射失败:', error)
        throw error
      }
    },

    // 删除知识库映射
    async deleteMapping(id) {
      try {
        await configApi.deleteMapping(id)
        this.mappings = this.mappings.filter(mapping => mapping.id !== id)
      } catch (error) {
        console.error('删除知识库映射失败:', error)
        throw error
      }
    },

    // 获取RAG框架配置
    async fetchRagConfig(providerType) {
      this.loading.ragConfigs = true
      try {
        const response = await configApi.getRagConfig(providerType)
        this.ragConfigs[providerType] = response.data
        return response.data
      } catch (error) {
        console.error('获取RAG框架配置失败:', error)
        throw error
      } finally {
        this.loading.ragConfigs = false
      }
    },

    // 更新RAG框架配置
    async updateRagConfig(providerType, configData) {
      try {
        const response = await configApi.updateRagConfig(providerType, configData)
        this.ragConfigs[providerType] = response.data
        return response.data
      } catch (error) {
        console.error('更新RAG框架配置失败:', error)
        throw error
      }
    },

    // 测试RAG框架配置
    async testRagConfig(providerType, testData) {
      try {
        const response = await configApi.testRagConfig(providerType, testData)
        return response.data
      } catch (error) {
        console.error('测试RAG框架配置失败:', error)
        throw error
      }
    },

    // 获取配置历史
    async fetchConfigHistory(configType, configId, page = 1, size = 20) {
      this.loading.history = true
      try {
        const response = await configApi.getConfigHistory(configType, configId, page, size)
        this.configHistory = response.data.records || []
        return response.data
      } catch (error) {
        console.error('获取配置历史失败:', error)
        throw error
      } finally {
        this.loading.history = false
      }
    },

    // 配置回滚
    async rollbackConfig(configType, configId, historyId) {
      try {
        const response = await configApi.rollbackConfig(configType, configId, historyId)
        return response.data
      } catch (error) {
        console.error('配置回滚失败:', error)
        throw error
      }
    },

    // 批量导入配置
    async importConfig(file) {
      try {
        const response = await configApi.importConfig(file)
        return response.data
      } catch (error) {
        console.error('导入配置失败:', error)
        throw error
      }
    },

    // 批量导出配置
    async exportConfig(format = 'json') {
      try {
        const response = await configApi.exportConfig(format)
        return response.data
      } catch (error) {
        console.error('导出配置失败:', error)
        throw error
      }
    },

    // 设置当前编辑的映射
    setCurrentMapping(mapping) {
      this.currentMapping = mapping
    },

    // 设置当前编辑的RAG配置
    setCurrentRagConfig(config) {
      this.currentRagConfig = config
    }
  }
})