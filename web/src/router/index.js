import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: {
      title: '仪表板',
      icon: 'DataBoard'
    }
  },
  {
    path: '/mappings',
    name: 'KnowledgeMappings',
    component: () => import('../views/KnowledgeMappings.vue'),
    meta: {
      title: '知识库映射',
      icon: 'Connection'
    }
  },
  {
    path: '/rag-config',
    name: 'RagConfig',
    component: () => import('../views/RagConfig.vue'),
    meta: {
      title: 'RAG框架配置',
      icon: 'Setting'
    }
  },
  {
    path: '/logs',
    name: 'Logs',
    component: () => import('../views/Logs.vue'),
    meta: {
      title: '日志查看',
      icon: 'Document'
    }
  },
  {
    path: '/config-history',
    name: 'ConfigHistory',
    component: () => import('../views/ConfigHistory.vue'),
    meta: {
      title: '配置历史',
      icon: 'Clock'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta && to.meta.title) {
    document.title = `${to.meta.title} - Dify外部知识库API管理`
  }
  next()
})

export default router