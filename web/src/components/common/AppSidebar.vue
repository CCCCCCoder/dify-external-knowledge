<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../../store'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 菜单数据
const menuItems = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    title: '仪表板',
    icon: 'DataBoard'
  },
  {
    path: '/mappings',
    name: 'KnowledgeMappings',
    title: '知识库映射',
    icon: 'Connection'
  },
  {
    path: '/rag-config',
    name: 'RagConfig',
    title: 'RAG框架配置',
    icon: 'Setting'
  },
  {
    path: '/logs',
    name: 'Logs',
    title: '日志查看',
    icon: 'Document'
  },
  {
    path: '/config-history',
    name: 'ConfigHistory',
    title: '配置历史',
    icon: 'Clock'
  }
]

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 菜单宽度
const sidebarWidth = computed(() => {
  return appStore.sidebarStatus ? '250px' : '64px'
})

// 菜单折叠状态
const isCollapse = computed(() => {
  return !appStore.sidebarStatus
})

// 处理菜单点击
const handleMenuSelect = (index) => {
  router.push(index)
}

// 处理子菜单点击
const handleSubmenuClick = (index) => {
  // 可以在这里添加子菜单点击逻辑
}
</script>

<template>
  <div 
    class="app-sidebar"
    :style="{ width: sidebarWidth }"
  >
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      class="sidebar-menu"
      @select="handleMenuSelect"
    >
      <template v-for="item in menuItems" :key="item.path">
        <el-menu-item :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<style scoped>
.app-sidebar {
  position: fixed;
  top: 60px;
  left: 0;
  bottom: 0;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s ease;
  z-index: 999;
  overflow: hidden;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
  background: #fff;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 250px;
}

/* 菜单项样式 */
:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  color: #606266;
  border-bottom: none;
}

:deep(.el-menu-item:hover) {
  background-color: #ecf5ff;
  color: #409eff;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

/* 折叠状态下的样式 */
:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 20px;
  text-align: center;
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  margin-right: 0;
  font-size: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 0;
    width: 250px !important;
    z-index: 1001;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  }
  
  .sidebar-menu {
    width: 250px !important;
  }
  
  .sidebar-menu:not(.el-menu--collapse) {
    width: 250px;
  }
}
</style>