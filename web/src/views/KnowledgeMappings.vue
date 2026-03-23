<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useConfigStore } from '../store'
import { MAPPING_STATUS, RAG_PROVIDER_TYPES } from '../api'
import ImportDialog from '../components/ImportDialog.vue'
import dayjs from 'dayjs'

const configStore = useConfigStore()

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const importDialogVisible = ref(false)
const exporting = ref(false)
const currentMapping = ref({
  id: null,
  knowledgeId: '',
  providerType: '',
  targetId: '',
  workspaceId: '',
  configParams: {},
  status: MAPPING_STATUS.ENABLED
})

// 表单数据
const form = ref({ ...currentMapping.value })

// 获取映射列表
const fetchMappings = async () => {
  loading.value = true
  try {
    await configStore.fetchMappings()
  } catch (error) {
    ElMessage.error('获取知识库映射列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    knowledgeId: '',
    providerType: '',
    targetId: '',
    workspaceId: '',
    configParams: {},
    status: MAPPING_STATUS.ENABLED
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存映射
const saveMapping = async () => {
  try {
    if (isEdit.value) {
      await configStore.updateMapping(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await configStore.createMapping(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchMappings()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

// 删除映射
const deleteMapping = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除知识库映射 "${row.knowledgeId}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await configStore.deleteMapping(row.id)
    ElMessage.success('删除成功')
    fetchMappings()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === MAPPING_STATUS.ENABLED ? MAPPING_STATUS.DISABLED : MAPPING_STATUS.ENABLED
    await configStore.updateMapping(row.id, { ...row, status: newStatus })
    ElMessage.success('状态更新成功')
    fetchMappings()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 格式化状态
const formatStatus = (status) => {
  return status === MAPPING_STATUS.ENABLED ? '启用' : '禁用'
}

// 格式化提供者类型
const formatProviderType = (type) => {
  return type === RAG_PROVIDER_TYPES.BAILIAN ? '阿里云百炼' : 'RAGFlow'
}

// 导出配置
const exportConfig = async () => {
  exporting.value = true
  try {
    const response = await configStore.exportConfig()
    const blob = new Blob([JSON.stringify(response, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `config_export_${dayjs().format('YYYYMMDD_HHmmss')}.json`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 导入成功回调
const handleImportSuccess = () => {
  fetchMappings()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchMappings()
})
</script>

<template>
  <div class="knowledge-mappings">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>知识库映射</h2>
        <p>管理Dify知识库与RAG框架的映射关系</p>
      </div>
      <div class="header-right">
        <el-button @click="exportConfig" :loading="exporting">
          导出配置
        </el-button>
        <el-button @click="importDialogVisible = true">
          导入配置
        </el-button>
        <el-button type="primary" :icon="Plus" @click="showAddDialog">
          新增映射
        </el-button>
        <el-button :icon="Refresh" @click="fetchMappings" :loading="loading">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 映射列表 -->
    <el-card>
      <el-table 
        :data="configStore.getAllMappings" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="knowledgeId" label="Dify知识库ID" min-width="150" />
        <el-table-column prop="providerType" label="提供者类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.providerType === RAG_PROVIDER_TYPES.BAILIAN ? 'primary' : 'success'">
              {{ formatProviderType(row.providerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标知识库ID" min-width="150" />
        <el-table-column prop="workspaceId" label="工作空间ID" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === MAPPING_STATUS.ENABLED"
              @click="toggleStatus(row)"
            />
            <span class="status-text">{{ formatStatus(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteMapping(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑映射' : '新增映射'"
      width="600px"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item label="Dify知识库ID" required>
          <el-input v-model="form.knowledgeId" placeholder="请输入Dify知识库ID" />
        </el-form-item>
        <el-form-item label="提供者类型" required>
          <el-select v-model="form.providerType" placeholder="请选择提供者类型" style="width: 100%">
            <el-option label="阿里云百炼" :value="RAG_PROVIDER_TYPES.BAILIAN" />
            <el-option label="RAGFlow" :value="RAG_PROVIDER_TYPES.RAGFLOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标知识库ID" required>
          <el-input v-model="form.targetId" placeholder="请输入目标知识库ID" />
        </el-form-item>
        <el-form-item label="工作空间ID" v-if="form.providerType === RAG_PROVIDER_TYPES.BAILIAN">
          <el-input v-model="form.workspaceId" placeholder="请输入工作空间ID" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="MAPPING_STATUS.ENABLED">启用</el-radio>
            <el-radio :label="MAPPING_STATUS.DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMapping">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <ImportDialog 
      v-model:visible="importDialogVisible"
      @success="handleImportSuccess"
    />
  </div>
</template>

<style scoped>
.knowledge-mappings {
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

.status-text {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
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