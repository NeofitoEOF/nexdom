<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits(['close'])

const route = useRoute()

const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(`${path}/`)
}

const navItems = [
  { name: 'Dashboard', path: '/', icon: 'home' },
  { name: 'Products', path: '/products', icon: 'box' },
  { name: 'Estoque', path: '/inventory', icon: 'archive' },
  { name: 'Relatórios', path: '/reports', icon: 'chart' }
]

const overlayClasses = computed(() => [
  'fixed inset-0 bg-gray-600 bg-opacity-75 transition-opacity md:hidden z-20',
  props.isOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'
])

const sidebarClasses = computed(() => [
  'fixed inset-y-0 left-0 flex flex-col bg-white border-r shadow-xl transform transition-transform duration-300 ease-in-out z-30',
  'w-64',
  props.isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
])
</script>

<template>
  <div>
    <div 
      :class="overlayClasses"
      @click="emit('close')"
    ></div>
    
    <aside :class="sidebarClasses">
      <div class="flex items-center justify-between h-16 px-4 border-b">
        <div class="flex items-center">
          <span class="text-lg font-bold text-primary-600">Nexdom Estoque</span>
        </div>
        <button 
          class="md:hidden p-2 rounded-md text-gray-500 hover:text-gray-900 hover:bg-gray-100 focus:outline-none"
          @click="emit('close')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <nav class="flex-1 px-2 py-4 space-y-1 overflow-y-auto">
        <template v-for="item in navItems" :key="item.path">
          <router-link 
            :to="item.path"
            :class="[
              'flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors',
              isActive(item.path)
                ? 'bg-primary-50 text-primary-700'
                : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
            ]"
          >
            <span class="mr-3">
              <svg v-if="item.icon === 'home'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              
              <svg v-if="item.icon === 'box'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              
              <svg v-if="item.icon === 'archive'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
              </svg>
              
              <svg v-if="item.icon === 'chart'" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </span>
            {{ item.name }}
          </router-link>
        </template>
      </nav>
      
      <div class="p-4 border-t">
        <div class="flex items-center">
          <div class="flex-shrink-0">
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>