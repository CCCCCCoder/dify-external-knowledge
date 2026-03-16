import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

// 导出所有store
export { useAppStore } from './modules/app'
export { useConfigStore } from './modules/config'
export { useLogStore } from './modules/log'