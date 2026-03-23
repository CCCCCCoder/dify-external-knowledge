<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useConfigStore } from '../store'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const configStore = useConfigStore()

const loading = ref(false)
const fileList = ref([])

const handleFileChange = (uploadFile) => {
  fileList.value = [uploadFile]
}

const handleFileRemove = () => {
  fileList.value = []
}

const submitImport = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要导入的配置文件')
    return
  }

  const file = fileList.value[0].raw
  if (!file) {
    ElMessage.warning('文件无效')
    return
  }

  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await fetch('/api/config/import', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) {
      throw new Error('Import failed')
    }

    const result = await response.json()
    ElMessage.success('导入成功')
    emit('success')
    emit('update:visible', false)
    fileList.value = []
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  fileList.value = []
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    title="导入配置"
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    @close="handleClose"
    width="500px"
  >
    <el-upload
      ref="uploadRef"
      drag
      :auto-upload="false"
      :limit="1"
      :on-change="handleFileChange"
      :on-remove="handleFileRemove"
      accept=".json"
      action="#"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 .json 格式的配置文件
        </div>
      </template>
    </el-upload>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitImport" :loading="loading">
        确认导入
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 7px;
}
</style>
