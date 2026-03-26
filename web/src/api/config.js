import { get, post, put, del, upload, download } from '../utils/request'

// 知识库映射相关API
export const configApi = {
  // 获取知识库映射列表
  getMappings(params = {}) {
    return get('/config/mappings', params)
  },

  // 创建知识库映射
  createMapping(data) {
    return post('/config/mappings', data)
  },

  // 更新知识库映射
  updateMapping(id, data) {
    return put(`/config/mappings/${id}`, data)
  },

  // 删除知识库映射
  deleteMapping(id) {
    return del(`/config/mappings/${id}`)
  },

  // 获取RAG框架配置
  getRagConfig(providerType) {
    return get(`/config/rag/${providerType}`)
  },

  // 更新RAG框架配置
  updateRagConfig(providerType, data) {
    return put(`/config/rag/${providerType}`, data)
  },

  // 测试RAG框架配置
  testRagConfig(providerType, data) {
    return post(`/config/rag/${providerType}/test`, data)
  },

    // 获取配置变更历史
    getConfigHistory(configType, configId, page = 1, size = 20) {
      return get('/config/history', {
        configType: configType,
        configId: configId,
        page,
        size
      })
    },

    // 配置回滚
    rollbackConfig(configType, configId, historyId) {
      return post('/config/rollback', {
        configType: configType,
        configId: configId,
        historyId: historyId
      })
    },

  // 批量导入配置
  importConfig(file) {
    return upload('/config/import', file)
  },

  // 批量导出配置
  exportConfig(format = 'json') {
    return download('/config/export', { format })
  }
}

// RAG框架配置类型枚举
export const RAG_PROVIDER_TYPES = {
  BAILIAN: 'bailian',
  RAGFLOW: 'ragflow'
}

// 知识库映射状态枚举
export const MAPPING_STATUS = {
  DISABLED: 0,
  ENABLED: 1
}

// 操作类型枚举
export const OPERATION_TYPES = {
  CREATE: 'CREATE',
  UPDATE: 'UPDATE',
  DELETE: 'DELETE'
}

// 配置类型枚举
export const CONFIG_TYPES = {
  MAPPING: 'mapping',
  RAG_CONFIG: 'rag_config'
}