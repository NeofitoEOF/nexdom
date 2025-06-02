<script setup lang="ts">
import { ref } from 'vue'
import AppHeader from './components/layout/AppHeader.vue'
import AppSidebar from './components/layout/AppSidebar.vue'

const isSidebarOpen = ref(false)

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}
</script>

<template>
  <div class="relative min-h-screen bg-gray-100">
  <div class="flex min-h-screen">
    <AppSidebar :is-open="isSidebarOpen" @close="isSidebarOpen = false" />
    <div class="flex flex-col flex-1 min-w-0">
      <AppHeader @toggle-sidebar="toggleSidebar" />
      <main class="flex-1 overflow-auto p-6 bg-gray-100">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</div>
</template>

<style scoped>
.relative {
  position: relative;
}
.flex-1 {
  flex: 1 1 0%;
}
.min-w-0 {
  min-width: 0;
}
.min-h-screen {
  min-height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (min-width: 768px) {
  .sidebar {
    position: static !important;
    transform: none !important;
    z-index: 10 !important;
  }
}

main {
  z-index: 20;
  position: relative;
}
</style>