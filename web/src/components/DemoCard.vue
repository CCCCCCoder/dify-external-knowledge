<script setup>
import { ref } from 'vue';

defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  }
});

const isHovering = ref(false);

const handleMouseEnter = () => {
  isHovering.value = true;
};

const handleMouseLeave = () => {
  isHovering.value = false;
};
</script>

<template>
  <el-card 
    class="demo-card"
    shadow="hover"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="demo-card-content">
      <el-icon v-if="icon" class="card-icon"><component :is="icon" /></el-icon>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <el-button 
        type="primary" 
        :class="{ 'visible': isHovering }"
        class="explore-button"
      >
        Explore <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </el-card>
</template>

<style scoped>
.demo-card {
  height: 100%;
  transition: transform 0.3s;
}

.demo-card:hover {
  transform: translateY(-5px);
}

.demo-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 10px;
}

.card-icon {
  font-size: 2.5rem;
  color: #409EFF;
  margin-bottom: 16px;
}

h3 {
  margin: 10px 0;
  color: #333;
}

p {
  color: #666;
  margin-bottom: 20px;
}

.explore-button {
  opacity: 0;
  transition: opacity 0.3s;
}

.explore-button.visible {
  opacity: 1;
}
</style>
