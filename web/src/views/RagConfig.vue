<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useConfigStore } from '../store'
import { RAG_PROVIDER_TYPES } from '../api'

const configStore = useConfigStore()

// 响应式数据
const loading = ref(false)
const activeTab = ref(RAG_PROVIDER_TYPES.BAILIAN)

// 获取RAG配置
const fetchRagConfig = async (providerType) => {
  loading.value = true
  try {
    await configStore.fetchRagConfig(providerType)
  } catch (error) {
    ElMessage.error(`获取${providerType === RAG_PROVIDER_TYPES.BAILIAN ? '阿里云百炼' : 'RAGFlow'}配置失败`)
  } finally {
    loading.value = false
  }
}

// 保存配置
const saveConfig = async (providerType) => {
  try {
    const config = configStore.getRagConfigByType(providerType)
    await configStore.updateRagConfig(providerType, config)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('配置保存失败')
  }
}

// 测试配置
const testConfig = async (providerType) => {
  try {
    const result = await configStore.testRagConfig(providerType, {
      test_query: '测试查询',
      test_knowledge_id: 'test-kb-id'
    })
    
    if (result.success) {
      ElMessage.success(`测试成功，响应时间: ${result.response_time}ms`)
    } else {
      ElMessage.error(`测试失败: ${result.error_message}`)
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  fetchRagConfig(tabName)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchRagConfig(activeTab.value)
})
</script>

<template>
  <div class="rag-config">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>RAG框架配置</h2>
        <p>配置阿里云百炼和RAGFlow的API参数</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Refresh" @click="fetchRagConfig(activeTab)" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 配置选项卡 -->
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 阿里云百炼配置 -->
        <el-tab-pane label="阿里云百炼" :name="RAG_PROVIDER_TYPES.BAILIAN">
          <div v-loading="loading" class="config-content">
            <el-form :model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN) || {}" label-width="150px">
              <el-form-item label="API端点">
                <el-input v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.endpoint" placeholder="https://bailian.aliyuncs.com" />
              </el-form-item>
              <el-form-item label="API密钥">
                <el-input v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.apiKey" type="password" placeholder="请输入API密钥" />
              </el-form-item>
              <el-form-item label="稀疏检索TopK">
                <el-input-number v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.configParams?.sparseSimilarityTopK" :min="0" :max="100" />
              </el-form-item>
              <el-form-item label="密集检索TopK">
                <el-input-number v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.configParams?.denseSimilarityTopK" :min="0" :max="100" />
              </el-form-item>
              <el-form-item label="启用重排序">
                <el-switch v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.configParams?.enableReranking" />
              </el-form-item>
              <el-form-item label="重排序最小分数">
                <el-input-number v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.configParams?.rerankMinScore" :min="0.01" :max="1" :step="0.01" />
              </el-form-item>
              <el-form-item label="重排序TopN">
                <el-input-number v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.BAILIAN)?.configParams?.rerankTopN" :min="1" :max="20" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveConfig(RAG_PROVIDER_TYPES.BAILIAN)">保存配置</el-button>
                <el-button @click="testConfig(RAG_PROVIDER_TYPES.BAILIAN)">测试连接</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- RAGFlow配置 -->
        <el-tab-pane label="RAGFlow" :name="RAG_PROVIDER_TYPES.RAGFLOW">
          <div v-loading="loading" class="config-content">
            <el-form :model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.RAGFLOW) || {}" label-width="150px">
              <el-form-item label="API端点">
                <el-input v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.RAGFLOW)?.endpoint" placeholder="http://localhost:9380" />
              </el-form-item>
              <el-form-item label="API密钥">
                <el-input v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.RAGFLOW)?.apiKey" type="password" placeholder="请输入API密钥" />
              </el-form-item>
              <el-form-item label="TopK">
                <el-input-number v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.RAGFLOW)?.configParams?.topK" :min="1" :max="100" />
              </el-form-item>
              <el-form-item label="相似度阈值">
                <el-input-number v-model="configStore.getRagConfigByType(RAG_PROVIDER_TYPES.RAGFLOW)?.configParams?.scoreThreshold" :min="0" :max="1" :step="0.01" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveConfig(RAG_PROVIDER_TYPES.RAGFLOW)">保存配置</el-button>
                <el-button @click="testConfig(RAG_PROVIDER_TYPES.RAGFLOW)">测试连接</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.rag-config {
  padding: 0;
}

.page-header {
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

.config-content {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>