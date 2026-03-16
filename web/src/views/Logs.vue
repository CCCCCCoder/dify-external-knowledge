<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useLogStore } from '../store'
import { PROVIDER_TYPES, LOG_STATUS } from '../api'

const logStore = useLogStore()

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentLog = ref(null)

// 查询参数
const queryParams = ref({
  knowledgeId: '',
  providerType: '',
  startDate: '',
  endDate: '',
  status: '',
  keyword: '',
  page: 1,
  size: 20
})

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true
  try {
    await logStore.fetchLogs(queryParams.value)
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  fetchLogs()
}

// 重置搜索
const handleReset = () => {
  logStore.resetQueryParams()
  queryParams.value = { ...logStore.getQueryParams, page: 1, size: 20 }
  fetchLogs()
}

// 分页变化
const handlePageChange = (page) => {
  queryParams.value.page = page
  fetchLogs()
}

// 查看详情
const showDetail = async (row) => {
  try {
    await logStore.fetchLogDetail(row.id)
    currentLog.value = logStore.getLogDetail
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取日志详情失败')
  }
}

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    [LOG_STATUS.SUCCESS]: '成功',
    [LOG_STATUS.ERROR]: '失败',
    [LOG_STATUS.PENDING]: '进行中'
  }
  return statusMap[status] || status
}

// 格式化提供者类型
const formatProviderType = (type) => {
  return type === PROVIDER_TYPES.BAILIAN ? '阿里云百炼' : 'RAGFlow'
}

// 格式化响应时间
const formatResponseTime = (time) => {
  if (!time) return '-'
  if (time >= 1000) {
    return (time / 1000).toFixed(2) + 's'
  }
  return time + 'ms'
}

// 组件挂载时获取数据
onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="logs">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>日志查看</h2>
        <p>查看API请求和响应日志</p>
      </div>
      <div class="header-right">
        <el-button :icon="Download" @click="logStore.exportLogs(queryParams)">
          导出日志
        </el-button>
        <el-button :icon="Refresh" @click="fetchLogs" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="知识库ID">
          <el-input v-model="queryParams.knowledgeId" placeholder="请输入知识库ID" clearable />
        </el-form-item>
        <el-form-item label="提供者类型">
          <el-select v-model="queryParams.providerType" placeholder="请选择提供者类型" clearable>
            <el-option label="阿里云百炼" :value="PROVIDER_TYPES.BAILIAN" />
            <el-option label="RAGFlow" :value="PROVIDER_TYPES.RAGFLOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="LOG_STATUS.SUCCESS" />
            <el-option label="失败" :value="LOG_STATUS.ERROR" />
            <el-option label="进行中" :value="LOG_STATUS.PENDING" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="(dates) => {
              queryParams.startDate = dates ? dates[0] : ''
              queryParams.endDate = dates ? dates[1] : ''
            }"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card>
      <el-table 
        :data="logStore.getAllLogs" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="requestId" label="请求ID" min-width="180" />
        <el-table-column prop="knowledgeId" label="知识库ID" min-width="120" />
        <el-table-column prop="providerType" label="提供者类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.providerType === PROVIDER_TYPES.BAILIAN ? 'primary' : 'success'">
              {{ formatProviderType(row.providerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseStatus" label="响应状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.responseStatus === 200 ? 'success' : 'danger'">
              {{ row.responseStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间" width="100">
          <template #default="{ row }">
            {{ formatResponseTime(row.responseTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="logStore.getPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="800px"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="请求ID">{{ currentLog.requestId }}</el-descriptions-item>
          <el-descriptions-item label="知识库ID">{{ currentLog.knowledgeId }}</el-descriptions-item>
          <el-descriptions-item label="提供者类型">{{ formatProviderType(currentLog.providerType) }}</el-descriptions-item>
          <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
          <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
          <el-descriptions-item label="响应状态">{{ currentLog.responseStatus }}</el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ formatResponseTime(currentLog.responseTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ new Date(currentLog.createdAt).toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h4>请求头</h4>
          <el-input
            type="textarea"
            :rows="4"
            readonly
            :value="JSON.stringify(JSON.parse(currentLog.requestHeaders || '{}'), null, 2)"
          />
        </div>
        
        <div class="detail-section">
          <h4>请求体</h4>
          <el-input
            type="textarea"
            :rows="6"
            readonly
            :value="JSON.stringify(JSON.parse(currentLog.requestBody || '{}'), null, 2)"
          />
        </div>
        
        <div class="detail-section">
          <h4>响应体</h4>
          <el-input
            type="textarea"
            :rows="6"
            readonly
            :value="JSON.stringify(JSON.parse(currentLog.responseBody || '{}'), null, 2)"
          />
        </div>
        
        <div v-if="currentLog.errorMessage" class="detail-section">
          <h4>错误信息</h4>
          <el-alert
            :title="currentLog.errorMessage"
            type="error"
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.logs {
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

.log-detail {
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