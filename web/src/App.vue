<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useAppStore } from './store'
import { useRoute } from 'vue-router'
import MainLayout from './components/common/MainLayout.vue'

const appStore = useAppStore()
const route = useRoute()

// 监听窗口大小变化，设置设备类型
const handleResize = () => {
  const width = window.innerWidth
  if (width < 768) {
    appStore.setDevice('mobile')
    appStore.closeSidebar(true)
  } else {
    appStore.setDevice('desktop')
    appStore.sidebar.opened = true
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <MainLayout />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  color: #333;
  background-color: #f5f7fa;
}

#app {
  height: 100%;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element Plus 样式覆盖 */
.el-main {
  padding: 20px;
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-button {
  border-radius: 4px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}

/* 工具类 */
.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.text-left {
  text-align: left;
}

.mb-10 {
  margin-bottom: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-10 {
  margin-top: 10px;
}

.mt-20 {
  margin-top: 20px;
}

.ml-10 {
  margin-left: 10px;
}

.mr-10 {
  margin-right: 10px;
}

.p-10 {
  padding: 10px;
}

.p-20 {
  padding: 20px;
}

.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

.full-width {
  width: 100%;
}

.full-height {
  height: 100%;
}

/* 响应式断点 */
@media (max-width: 768px) {
  .el-main {
    padding: 10px;
  }
  
  .el-card {
    margin-bottom: 10px;
  }
  
  .hidden-xs-only {
    display: none !important;
  }
}

@media (min-width: 769px) {
  .hidden-sm-and-up {
    display: none !important;
  }
}
</style>
