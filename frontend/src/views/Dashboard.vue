<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '../stores/productStore'
import { useInventoryStore } from '../stores/inventoryStore'
import { formatCurrency, formatNumber } from '../utils/formatters'
import type { InventoryStatsByType, ProductWithProfit } from '../interfaces'

const router = useRouter()
const productStore = useProductStore()
const inventoryStore = useInventoryStore()

onMounted(async (): Promise<void> => {
  await Promise.all([
    productStore.fetchProducts(),
    inventoryStore.fetchTransactions()
  ])
})

const totalProducts = computed((): number => productStore.products.length)

const lowStockProducts = computed((): number => productStore.products.filter(p => p.stock < 5).length)

const totalInventoryValue = computed((): number => {
  return inventoryStore.getProductsWithProfitData.reduce((total, product) => {
    return total + (product.currentStockValue || 0)
  }, 0)
})

const profitData = computed((): ProductWithProfit[] => {
  return inventoryStore.getProductsWithProfitData
    .filter(p => p.totalSold > 0)
    .sort((a, b) => b.totalProfit - a.totalProfit)
    .slice(0, 5)
})

const inventoryByType = computed((): InventoryStatsByType => {
  return inventoryStore.getInventoryStatsByType
})
</script>

<template>
  <div class="container mx-auto max-w-7xl px-2 sm:px-4 lg:px-8 py-8">
  <div class="mb-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-2">Painel</h1>
  </div>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-10">
      <div class="bg-white rounded-xl shadow-md p-6 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Total Produtos</p>
            <p class="text-2xl font-bold text-gray-900">{{ formatNumber(totalProducts, 0) }}</p>
          </div>
          <div class="p-3 rounded-full bg-primary-100 text-primary-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          </div>
        </div>
        <div class="mt-4">
          <router-link to="/products" class="text-sm text-primary-600 hover:text-primary-800">
            Visualizar todos produtos →
          </router-link>
        </div>
      </div>
      
      <div class="bg-white rounded-xl shadow-md p-6 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Itens com estoque baixo</p>
            <p class="text-2xl font-bold text-gray-900">{{ formatNumber(lowStockProducts, 0) }}</p>
          </div>
          <div class="p-3 rounded-full bg-warning-100 text-warning-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
        </div>
        <div class="mt-4">
          <router-link to="/inventory" class="text-sm text-primary-600 hover:text-primary-800">
            Gerenciar inventário →
          </router-link>
        </div>
      </div>
      
      <div class="bg-white rounded-xl shadow-md p-6 flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Valor total do estoque</p>
            <p class="text-2xl font-bold text-gray-900">{{ formatCurrency(totalInventoryValue) }}</p>
          </div>
          <div class="p-3 rounded-full bg-success-100 text-success-600">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
        <div class="mt-4">
          <router-link to="/reports" class="text-sm text-primary-600 hover:text-primary-800">
            Ver relatórios →
          </router-link>
        </div>
      </div>
    </div>
    
 
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <div class="bg-white rounded-xl shadow-md p-6 flex flex-col justify-between">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Inventário por tipo</h2>
        
        <div class="space-y-4">
          <template v-for="(stats, type) in inventoryByType" :key="type">
            <div>
              <div class="flex justify-between mb-1">
                <span class="text-sm font-medium text-gray-700">{{ type }}</span>
                <span class="text-sm font-medium text-gray-700">
                  {{ formatNumber(stats.totalAvailable, 0) }} disponíveis / {{ formatNumber(stats.totalSold, 0) }} vendidos
                </span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2.5">
                <div 
                  class="bg-primary-600 h-2.5 rounded-full" 
                  :style="`width: ${Math.min(100, (stats.totalSold / (stats.totalSold + stats.totalAvailable)) * 100)}%`"
                ></div>
              </div>
            </div>
          </template>
        </div>
      </div>
      
      <div class="bg-white rounded-xl shadow-md p-6 flex flex-col justify-between">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Produtos com maior lucro</h2>
        
        <div class="space-y-4">
          <template v-if="profitData.length > 0">
            <div v-for="product in profitData" :key="product.id" class="border-b pb-3 last:border-b-0 last:pb-0">
              <div class="flex justify-between">
                <span class="font-medium text-gray-800">{{ product.name }}</span>
                <span class="font-semibold text-success-600">{{ formatCurrency(product.totalProfit) }}</span>
              </div>
              <div class="text-sm text-gray-500">
                {{ formatNumber(product.totalSold, 0) }} unidades vendidas
              </div>
            </div>
          </template>
          <div v-else class="text-gray-500 text-center py-4">
            No sales data available
          </div>
        </div>
      </div>
    </div>
    

    <div class="bg-white rounded-xl shadow-md p-6 mb-8">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Ações rápidas</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-4">
        <button 
          @click="router.push('/products/create')"
          class="btn-primary flex items-center justify-center"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Adicionar novo produto
        </button>
        
        <button 
          @click="router.push('/inventory')"
          class="btn-success flex items-center justify-center"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
          </svg>
          Gerenciar inventário
        </button>
        
        <button 
          @click="router.push('/reports')"
          class="btn-secondary flex items-center justify-center"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          Gerar relatórios
        </button>
      </div>
    </div>
  </div>
</template>