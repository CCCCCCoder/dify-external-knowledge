<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useConfigStore } from '../store'
import { CONFIG_TYPES, OPERATION_TYPES } from '../api'

const configStore = useConfigStore()

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentHistory = ref(null)

// 查询参数
const queryParams = ref({
  configType: '',
  configId: '',
  page: 1,
  size: 20
})

// 获取配置历史
const fetchConfigHistory = async () => {
  loading.value = true
  try {
    await configStore.fetchConfigHistory(
      queryParams.value.configType,
      queryParams.value.configId,
      queryParams.value.page,
      queryParams.value.size
    )
  } catch (error) {
    ElMessage.error('获取配置历史失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  fetchConfigHistory()
}

// 重置搜索
const handleReset = () => {
  queryParams.value = {
    configType: '',
    configId: '',
    page: 1,
    size: 20
  }
  fetchConfigHistory()
}

// 分页变化
const handlePageChange = (page) => {
  queryParams.value.page = page
  fetchConfigHistory()
}

// 查看详情
const showDetail = (row) => {
  currentHistory.value = row
  detailDialogVisible.value = true
}

// 配置回滚
const rollbackConfig = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到 ${new Date(row.createdAt).toLocaleString()} 的配置吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await configStore.rollbackConfig(row.configType, row.configId, row.id)
    ElMessage.success('配置回滚成功')
    fetchConfigHistory()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('配置回滚失败')
    }
  }
}

// 格式化操作类型
const formatOperationType = (type) => {
  const typeMap = {
    [OPERATION_TYPES.CREATE]: '创建',
    [OPERATION_TYPES.UPDATE]: '更新',
    [OPERATION_TYPES.DELETE]: '删除'
  }
  return typeMap[type] || type
}

// 格式化配置类型
const formatConfigType = (type) => {
  return type === CONFIG_TYPES.MAPPING ? '知识库映射' : 'RAG框架配置'
}

// 格式化JSON
const formatJson = (jsonString) => {
  try {
    return JSON.stringify(JSON.parse(jsonString || '{}'), null, 2)
  } catch (error) {
    return jsonString || '{}'
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchConfigHistory()
})
</script>

<template>
  <div class="config-history">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>配置历史</h2>
        <p>查看配置变更历史和回滚配置</p>
      </div>
      <div class="header-right">
        <el-button :icon="Refresh" @click="fetchConfigHistory" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="配置类型">
          <el-select v-model="queryParams.configType" placeholder="请选择配置类型" clearable>
            <el-option label="知识库映射" :value="CONFIG_TYPES.MAPPING" />
            <el-option label="RAG框架配置" :value="CONFIG_TYPES.RAG_CONFIG" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置ID">
          <el-input v-model="queryParams.configId" placeholder="请输入配置ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史列表 -->
    <el-card>
      <el-table 
        :data="configStore.getConfigHistory" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configType" label="配置类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.configType === CONFIG_TYPES.MAPPING ? 'primary' : 'success'">
              {{ formatConfigType(row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configId" label="配置ID" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.operationType === OPERATION_TYPES.CREATE ? 'success' : row.operationType === OPERATION_TYPES.DELETE ? 'danger' : 'warning'">
              {{ formatOperationType(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.operationType === OPERATION_TYPES.UPDATE"
              type="warning" 
              size="small" 
              @click="rollbackConfig(row)"
            >
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="configStore.getConfigHistory.total || 0"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="配置历史详情"
      width="800px"
    >
      <div v-if="currentHistory" class="history-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentHistory.id }}</el-descriptions-item>
          <el-descriptions-item label="配置类型">{{ formatConfigType(currentHistory.configType) }}</el-descriptions-item>
          <el-descriptions-item label="配置ID">{{ currentHistory.configId }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ formatOperationType(currentHistory.operationType) }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentHistory.operator }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ new Date(currentHistory.createdAt).toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentHistory.oldValue" class="detail-section">
          <h4>旧值</h4>
          <el-input
            type="textarea"
            :rows="8"
            readonly
            :value="formatJson(currentHistory.oldValue)"
          />
        </div>
        
        <div v-if="currentHistory.newValue" class="detail-section">
          <h4>新值</h4>
          <el-input
            type="textarea"
            :rows="8"
            readonly
            :value="formatJson(currentHistory.newValue)"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.config-history {
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

.search-card {
  margin-bottom: 20px;
}

.history-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin-bottom: 10px;
  color: #303133;
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