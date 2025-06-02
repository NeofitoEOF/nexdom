<script setup lang="ts">

defineEmits(['toggle-sidebar'])
</script>

<template>
  <header class="bg-white shadow-sm z-10">
    <div class="flex justify-between items-center px-4 py-3">
      <div class="flex items-center">
        <button 
          class="md:hidden mr-2 p-2 rounded-md text-gray-500 hover:text-gray-900 hover:bg-gray-100 focus:outline-none"
          @click="$emit('toggle-sidebar')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <h1 class="text-xl font-semibold text-gray-800">Nexdom Estoque</h1>
      </div>
      
      <div class="flex items-center space-x-3">
        <div class="flex items-center">
        </div>
      </div>
    </div>
  </header>
</template>