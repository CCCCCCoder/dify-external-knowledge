<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 路由映射表
const routeMap = {
  '/dashboard': '仪表板',
  '/mappings': '知识库映射',
  '/rag-config': 'RAG框架配置',
  '/logs': '日志查看',
  '/config-history': '配置历史'
}

// 计算面包屑数据
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const breadcrumbs = []
  
  // 添加首页
  breadcrumbs.push({
    name: 'dashboard',
    path: '/dashboard',
    title: '首页'
  })
  
  // 添加匹配的路由
  matched.forEach(item => {
    if (item.path !== '/') {
      breadcrumbs.push({
        name: item.name,
        path: item.path,
        title: item.meta.title || routeMap[item.path] || item.name
      })
    }
  })
  
  return breadcrumbs
})

// 处理面包屑点击
const handleBreadcrumbClick = (breadcrumb) => {
  if (breadcrumb.path !== route.path) {
    router.push(breadcrumb.path)
  }
}
</script>

<template>
  <div class="app-breadcrumb">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item
        v-for="(item, index) in breadcrumbList"
        :key="index"
        :class="{ 'is-link': item.path !== $route.path }"
        @click="handleBreadcrumbClick(item)"
      >
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<style scoped>
.app-breadcrumb {
  padding: 16px 0;
  background: #fff;
  margin-bottom: 20px;
  border-radius: 8px;
  padding-left: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

:deep(.el-breadcrumb__item) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item .is-link) {
  color: #606266;
  cursor: pointer;
  transition: color 0.3s;
}

:deep(.el-breadcrumb__item .is-link:hover) {
  color: #409eff;
}

:deep(.el-breadcrumb__item:last-child .is-link) {
  color: #303133;
  cursor: default;
  font-weight: 500;
}

:deep(.el-breadcrumb__item:last-child .is-link:hover) {
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-breadcrumb {
    padding: 12px 15px;
    margin-bottom: 15px;
  }
  
  :deep(.el-breadcrumb__item) {
    font-size: 13px;
  }
}
</style>