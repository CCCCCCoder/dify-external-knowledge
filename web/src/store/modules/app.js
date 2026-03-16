import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 侧边栏状态
    sidebar: {
      opened: true,
      withoutAnimation: false
    },
    // 设备类型
    device: 'desktop',
    // 主题设置
    theme: {
      primaryColor: '#409EFF',
      darkMode: false
    },
    // 加载状态
    loading: false,
    // 面包屑导航
    breadcrumb: []
  }),

  getters: {
    // 获取侧边栏状态
    sidebarStatus: (state) => state.sidebar.opened,
    // 获取设备类型
    isMobile: (state) => state.device === 'mobile',
    // 获取主题颜色
    primaryColor: (state) => state.theme.primaryColor,
    // 获取加载状态
    isLoading: (state) => state.loading
  },

  actions: {
    // 切换侧边栏
    toggleSidebar(withoutAnimation = false) {
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = withoutAnimation
    },
    
    // 关闭侧边栏
    closeSidebar(withoutAnimation = false) {
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    },
    
    // 设置设备类型
    setDevice(device) {
      this.device = device
    },
    
    // 设置主题颜色
    setPrimaryColor(color) {
      this.theme.primaryColor = color
    },
    
    // 切换暗黑模式
    toggleDarkMode() {
      this.theme.darkMode = !this.theme.darkMode
    },
    
    // 设置加载状态
    setLoading(loading) {
      this.loading = loading
    },
    
    // 设置面包屑
    setBreadcrumb(breadcrumb) {
      this.breadcrumb = breadcrumb
    }
  }
})