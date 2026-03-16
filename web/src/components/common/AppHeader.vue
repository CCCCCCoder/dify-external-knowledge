<script setup>
import { computed } from 'vue'
import { useAppStore } from '../../store'
import { useRouter } from 'vue-router'

const appStore = useAppStore()
const router = useRouter()

// 定义事件
const emit = defineEmits(['toggle-sidebar'])

// 计算当前时间
const currentTime = computed(() => {
  return new Date().toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

// 切换侧边栏
const handleToggleSidebar = () => {
  emit('toggle-sidebar')
}

// 返回首页
const goHome = () => {
  router.push('/dashboard')
}
</script>

<template>
  <div class="app-header">
    <div class="header-left">
      <!-- 菜单切换按钮 -->
      <el-button 
        type="text" 
        class="menu-toggle"
        @click="handleToggleSidebar"
      >
        <el-icon size="20">
          <Expand v-if="!appStore.sidebarStatus" />
          <Fold v-else />
        </el-icon>
      </el-button>
      
      <!-- Logo和标题 -->
      <div class="logo-container" @click="goHome">
        <img src="/src/assets/vue.svg" alt="Logo" class="logo" />
        <h1 class="app-title">Dify外部知识库API管理</h1>
      </div>
    </div>
    
    <div class="header-right">
      <!-- 用户信息 -->
      <el-dropdown class="user-dropdown">
        <div class="user-info">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="username">管理员</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item>
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided>
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.app-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.menu-toggle {
  margin-right: 20px;
  color: #606266;
}

.menu-toggle:hover {
  color: #409eff;
}

.logo-container {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  margin-left: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 15px;
  }
  
  .menu-toggle {
    margin-right: 15px;
  }
  
  .logo {
    height: 28px;
    margin-right: 10px;
  }
  
  .app-title {
    font-size: 16px;
  }
  
  .username {
    display: none;
  }
}
</style>