import { createApp } from "vue";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import "./style.css";
import App from "./App.vue";

// 导入路由
import router from './router'

// 导入状态管理
import pinia from './store'

// 创建应用实例
const app = createApp(App);

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 使用插件
app.use(ElementPlus);
app.use(router);
app.use(pinia);

// 挂载应用
app.mount("#app");
