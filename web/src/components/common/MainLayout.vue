<script setup>
import { computed } from 'vue'
import { useAppStore } from '../../store'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppBreadcrumb from './AppBreadcrumb.vue'

const appStore = useAppStore()
const route = useRoute()
const router = useRouter()

// 计算侧边栏是否显示
const sidebarVisible = computed(() => {
  return !appStore.isMobile || appStore.sidebarStatus
})

// 计算主内容区域的样式
const mainStyle = computed(() => {
  const sidebarWidth = appStore.sidebarStatus ? '250px' : '64px'
  return {
    marginLeft: appStore.isMobile ? '0' : sidebarWidth,
    transition: 'margin-left 0.3s ease'
  }
})

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}
</script>

<template>
  <div class="app-layout">
    <!-- 头部 -->
    <AppHeader @toggle-sidebar="toggleSidebar" />
    
    <!-- 主体内容 -->
    <div class="app-main">
      <!-- 侧边栏 -->
      <AppSidebar v-if="sidebarVisible" />
      
      <!-- 主内容区 -->
      <div class="app-content" :style="mainStyle">
        <!-- 面包屑导航 -->
        <AppBreadcrumb />
        
        <!-- 路由视图 -->
        <div class="app-view">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

.app-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f5f7fa;
}

.app-view {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-view {
    padding: 10px;
  }
}
</style>