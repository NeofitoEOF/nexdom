<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore } from '../../stores/productStore'
import { useInventoryStore } from '../../stores/inventoryStore'
import { format } from 'date-fns'
import BaseButton from '../../components/BaseButton.vue'
import { formatCurrency, formatNumber } from '../../utils/formatters'
import { getTypeDisplayName } from '../../utils/productTypes'
import type { Product, InventoryTransaction } from '../../interfaces'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()
const inventoryStore = useInventoryStore()

const productId = route.params.id as string

const product = computed<Product | undefined>(() => productStore.getProductById(productId))

const quantity = ref(1)
const notes = ref('')
const transactionType = ref<'input' | 'output'>('input')
const error = ref('')
const success = ref('')

const transactions = computed<InventoryTransaction[]>(() => {
  return inventoryStore.getTransactionsByProduct(productId)
})

const totalSold = computed((): number => {
  return transactions.value
    .filter(t => t.type === 'output')
    .reduce((sum, t) => sum + t.quantity, 0)
})

const totalProfit = computed((): number => {
  if (!product.value) return 0
  return totalSold.value * (product.value.sellingPrice - product.value.supplierPrice)
})

const handleTransaction = async (): Promise<void> => {
  if (quantity.value <= 0) {
    error.value = 'A quantidade deve ser maior que zero'
    return
  }
  
  if (transactionType.value === 'output' && product.value && quantity.value > product.value.stock) {
    error.value = 'Quantidade insuficiente em estoque'
    return
  }
  
  error.value = ''
  success.value = ''
  
  try {
    const transaction = {
      productId,
      quantity: quantity.value,
      type: transactionType.value,
      notes: notes.value,
      date: new Date(),
      value: product.value ? (transactionType.value === 'input' ? product.value.supplierPrice : product.value.sellingPrice) : 0
    }
    
    const result = await inventoryStore.addTransaction(transaction)
    
    if (result.success) {
      success.value = `${transactionType.value === 'input' ? 'Adicionado' : 'Removido'} ${quantity.value} ${quantity.value === 1 ? 'item' : 'itens'} com sucesso`
      quantity.value = 1
      notes.value = ''
    } else {
      error.value = result.message || 'Ocorreu um erro ao processar a transação'
    }
  } catch (err) {

    error.value = err instanceof Error ? err.message : 'Ocorreu um erro ao processar a transação'
  }
}

const formatDate = (date: Date | string): string => {
  try {
    return format(new Date(date), 'dd/MM/yyyy HH:mm')
  } catch (e) {
    return 'Data inválida'
  }
}

const goBack = (): void => {
  router.push('/products')
}

const editProduct = (): void => {
  router.push(`/products/${productId}/edit`)
}

onMounted((): void => {
  if (!product.value) {
    router.push('/products')
  }
})
</script>

<template>
  <div v-if="product">
    <div class="mb-6 flex justify-between items-center">
      <div>
        <BaseButton btnClass="btn-outline" @click="goBack">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Voltar para Produtos
        </BaseButton>
        <h1 class="text-2xl font-bold text-gray-900">{{ product.name }}</h1>
      </div>
      
      <BaseButton btnClass="btn-primary" @click="editProduct">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
        Editar Produto
      </BaseButton>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
      <div class="lg:col-span-2">
        <div class="card">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Detalhes do Produto</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Nome</p>
              <p class="text-lg font-medium">{{ product.name }}</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Tipo</p>
              <p class="text-lg font-medium">{{ getTypeDisplayName(product.type) }}</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Preço do Fornecedor</p>
              <p class="text-lg font-medium">{{ formatCurrency(product.supplierPrice) }}</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Preço de Venda</p>
              <p class="text-lg font-medium">{{ formatCurrency(product.sellingPrice) }}</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Estoque</p>
              <p 
                class="text-lg font-medium"
                :class="[
                  product.stock > 10 ? 'text-success-700' : product.stock > 0 ? 'text-warning-700' : 'text-danger-700'
                ]"
              >
                {{ formatNumber(product.stock, 0) }} unidades
              </p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Margem de Lucro</p>
              <p class="text-lg font-medium text-success-700">
                {{ formatNumber((((product.sellingPrice - product.supplierPrice) / product.sellingPrice) * 100), 2) }}%
              </p>
            </div>
          </div>
          
          <div class="mt-6">
            <p class="text-sm font-medium text-gray-500 mb-1">Descrição</p>
            <p class="text-gray-800">{{ product.description }}</p>
          </div>
        </div>
        
        <div class="card mt-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Métricas de Desempenho</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Total Vendido</p>
              <p class="text-2xl font-bold text-gray-700">{{ formatNumber(totalSold, 0) }}</p>
              <p class="text-xs text-gray-500">itens vendidos</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Revenue</p>
              <p class="text-2xl font-bold text-gray-900">${{ (totalSold * product.sellingPrice).toFixed(2) }}</p>
              <p class="text-xs text-gray-500">total sales</p>
            </div>
            
            <div>
              <p class="text-sm font-medium text-gray-500 mb-1">Lucro</p>
              <p class="text-2xl font-bold text-success-700">{{ formatCurrency(totalProfit) }}</p>
              <p class="text-xs text-gray-500">lucro total</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="lg:col-span-1">
        <div class="card">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Gerenciar Estoque</h2>
          
          <form @submit.prevent="handleTransaction">
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">Tipo de Transação</label>
              <div class="flex space-x-4">
                <label class="inline-flex items-center">
                  <input type="radio" v-model="transactionType" value="input" class="form-radio text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-gray-700">Entrada</span>
                </label>
                <label class="inline-flex items-center">
                  <input type="radio" v-model="transactionType" value="output" class="form-radio text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-gray-700">Saída</span>
                </label>
              </div>
            </div>
            
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">Quantidade</label>
              <input
                type="number"
                v-model.number="quantity"
                min="1"
                class="input"
              />
            </div>
            
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">Observações (Opcional)</label>
              <textarea
                v-model="notes"
                rows="2"
                class="input"
                placeholder="Adicione observações sobre esta transação"
              ></textarea>
            </div>
            
            <div v-if="error" class="mb-4 p-2 bg-danger-50 text-danger-700 rounded-md">
              {{ error }}
            </div>
            
            <div v-if="success" class="mb-4 p-2 bg-success-50 text-success-700 rounded-md">
              {{ success }}
            </div>
            
            <button 
              type="submit"
              :class="[
                'w-full',
                transactionType === 'input' ? 'btn-success' : 'btn-primary'
              ]"
            >
              {{ transactionType === 'input' ? 'Adicionar ao Estoque' : 'Remover do Estoque' }}
            </button>
          </form>
        </div>
        
        <div class="card mt-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Transações Recentes</h2>
          
          <div class="space-y-3 max-h-80 overflow-y-auto">
            <template v-if="transactions.length > 0">
              <div v-for="transaction in transactions" :key="transaction.id" class="flex items-start border-b pb-3 last:border-b-0 last:pb-0">
                <div 
                  :class="[
                    'flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center',
                    transaction.type === 'input' ? 'bg-success-100 text-success-600' : 'bg-primary-100 text-primary-600'
                  ]"
                >
                  <svg v-if="transaction.type === 'input'" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                  </svg>
                </div>
                <div class="ml-3 flex-1">
                  <div class="flex justify-between">
                    <span class="text-sm font-medium">
                      {{ transaction.type === 'input' ? 'Added' : 'Removed' }} {{ transaction.quantity }} units
                    </span>
                    <span class="text-xs text-gray-500">{{ formatDate(transaction.date) }}</span>
                  </div>
                  <p v-if="transaction.notes" class="text-xs text-gray-500 mt-1">{{ transaction.notes }}</p>
                </div>
              </div>
            </template>
            <div v-else class="text-center text-gray-500 py-4">
              No transactions for this product
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>