<script setup>
import { ref } from 'vue'
import DemoCard from './components/DemoCard.vue'

const activeIndex = ref('1')
const handleSelect = (key) => {
  console.log(key)
}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo-container">
            <img src="./assets/vue.svg" class="logo" alt="Vue logo" />
            <h1 class="app-title">Vue 3 + Element Plus Demo</h1>
          </div>
          <el-menu
            :default-active="activeIndex"
            class="el-menu-demo"
            mode="horizontal"
            @select="handleSelect"
          >
            <el-menu-item index="1">Dashboard</el-menu-item>
            <el-menu-item index="2">Products</el-menu-item>
            <el-menu-item index="3">Settings</el-menu-item>
            <el-sub-menu index="4">
              <template #title>User Center</template>
              <el-menu-item index="4-1">Profile</el-menu-item>
              <el-menu-item index="4-2">Preferences</el-menu-item>
              <el-menu-item index="4-3">Logout</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
      </el-header>
      <el-container>
        <el-aside width="250px">
          <el-menu
            default-active="1"
            class="el-menu-vertical"
            :collapse="false"
          >
            <el-menu-item index="1">
              <el-icon><HomeFilled /></el-icon>
              <span>Dashboard</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon><Document /></el-icon>
              <span>Documents</span>
            </el-menu-item>
            <el-menu-item index="3">
              <el-icon><Setting /></el-icon>
              <span>Settings</span>
            </el-menu-item>
            <el-menu-item index="4">
              <el-icon><User /></el-icon>
              <span>Users</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <h2>Welcome to Your Vue 3 Application</h2>
          <p class="description">This project is built with Vue 3, Vite, and Element Plus</p>
          
          <el-divider content-position="left">Component Demos</el-divider>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <DemoCard 
                title="Form Controls" 
                icon="Edit"
                description="Input, select, checkbox, radio and form examples"
              />
            </el-col>
            <el-col :span="8">
              <DemoCard 
                title="Data Display" 
                icon="DataLine"
                description="Table, pagination and data visualization components"
              />
            </el-col>
            <el-col :span="8">
              <DemoCard 
                title="Notifications" 
                icon="Bell"
                description="Alerts, messages and notification examples"
              />
            </el-col>
          </el-row>
        </el-main>
      </el-container>
      <el-footer>
        <p>&copy; {{ new Date().getFullYear() }} - Vue 3 + Element Plus Demo Application</p>
      </el-footer>
    </el-container>
  </div>
</template>

<style scoped>
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  height: 32px;
  will-change: filter;
  transition: filter 300ms;
}

.logo:hover {
  filter: drop-shadow(0 0 1em #42b883aa);
}

.app-title {
  font-size: 1.2rem;
  margin: 0;
  color: #409EFF;
}

.el-header {
  background-color: white;
  color: #333;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
  height: 60px !important;
}

.el-aside {
  background-color: #f5f7fa;
  color: #333;
  border-right: solid 1px #e6e6e6;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.el-footer {
  background-color: #fff;
  color: #888;
  text-align: center;
  height: 60px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #eee;
}

.description {
  color: #666;
  margin-bottom: 20px;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

h2 {
  color: #333;
  margin-top: 0;
}
</style>
