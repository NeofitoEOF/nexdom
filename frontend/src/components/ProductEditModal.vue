<template>
  <div v-if="show" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true" @click="closeModal"></div>

      <!-- Modal panel -->
      <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="sm:flex sm:items-start">
            <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </div>
            <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
              <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                Editar Produto
              </h3>
              
              <div class="mt-4">
                <form @submit.prevent="handleSubmit" class="space-y-4">
                  <!-- Código do produto -->
                  <div>
                    <label for="code" class="block text-sm font-medium text-gray-700">Código</label>
                    <input 
                      type="text" 
                      id="code" 
                      v-model="form.code" 
                      class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                      :class="{'border-red-300 focus:border-red-500 focus:ring-red-500': errors.code}"
                    />
                    <p v-if="errors.code" class="mt-1 text-sm text-red-600">{{ errors.code }}</p>
                  </div>
                  
                  <!-- Tipo de produto -->
                  <div>
                    <label for="type" class="block text-sm font-medium text-gray-700">Tipo</label>
                    <select 
                      id="type" 
                      v-model="form.type" 
                      class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                      :class="{'border-red-300 focus:border-red-500 focus:ring-red-500': errors.type}"
                    >
                      <option value="">Selecione um tipo</option>
                      <option v-for="type in productTypes" :key="type" :value="type">{{ getTypeDisplayName(type) }}</option>
                    </select>
                    <p v-if="errors.type" class="mt-1 text-sm text-red-600">{{ errors.type }}</p>
                  </div>
                  
                  <!-- Descrição -->
                  <div>
                    <label for="description" class="block text-sm font-medium text-gray-700">Descrição</label>
                    <textarea 
                      id="description" 
                      v-model="form.description" 
                      rows="3" 
                      class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                      :class="{'border-red-300 focus:border-red-500 focus:ring-red-500': errors.description}"
                    ></textarea>
                    <p v-if="errors.description" class="mt-1 text-sm text-red-600">{{ errors.description }}</p>
                  </div>
                  
                  <!-- Preço de fornecedor -->
                  <div>
                    <label for="supplierPrice" class="block text-sm font-medium text-gray-700">Preço de Fornecedor (R$)</label>
                    <div class="mt-1 relative rounded-md shadow-sm">
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <span class="text-gray-500 sm:text-sm">R$</span>
                      </div>
                      <input 
                        type="number" 
                        id="supplierValue" 
                        v-model="form.supplierValue" 
                        step="0.01" 
                        min="0" 
                        class="block w-full pl-10 pr-12 border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    </div>
                    <p v-if="errors.supplierValue" class="mt-1 text-sm text-red-600">{{ errors.supplierValue }}</p>
                  </div>
                  
                  <!-- Estoque -->
                  <div>
                    <label for="stockQuantity" class="block text-sm font-medium text-gray-700">Estoque</label>
                    <div class="mt-1 relative rounded-md shadow-sm">
                      <input 
                        type="number" 
                        id="stockQuantity" 
                        v-model="form.stockQuantity" 
                        class="block w-full pr-12 border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                        :class="{'border-red-300 focus:border-red-500 focus:ring-red-500': errors.stockQuantity}"
                        min="0" step="1"
                      />
                      <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                        <span class="text-gray-500 sm:text-sm">un</span>
                      </div>
                    </div>
                    <p v-if="errors.stockQuantity" class="mt-1 text-sm text-red-600">{{ errors.stockQuantity }}</p>
                  </div>

                  <!-- Mensagem de erro -->
                  <div v-if="errors.submit" class="p-3 bg-red-50 border border-red-200 text-red-700 rounded-md">
                    <div class="flex">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span>{{ errors.submit }}</span>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
        
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button 
            type="button" 
            @click="handleSubmit"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm"
            :disabled="loading"
          >
            <span v-if="!loading">Salvar Alterações</span>
            <span v-else class="flex items-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Processando...
            </span>
          </button>
          <button 
            type="button" 
            @click="closeModal"
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
          >
            Cancelar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useProductStore } from '../stores/productStore'
import { PRODUCT_TYPES } from '../utils/productTypes'
import { getTypeDisplayName } from '../utils/productTypes'
import type { Product } from '../interfaces'

const props = defineProps<{
  show: boolean
  product: Product | null
}>()

const emit = defineEmits<{
  close: []
  updated: []
}>()

const productStore = useProductStore()
const productTypes = PRODUCT_TYPES

// Estado do formulário
const form = reactive({
  code: '',
  description: '',
  type: '',
  supplierValue: 0,
  stockQuantity: 0
})

// Estado de erros e loading
const errors = ref<Record<string, string>>({})
const loading = ref(false)

// Carregar dados do produto quando o modal abrir
watch(() => props.show, (newVal) => {
  if (newVal && props.product) {
    form.code = props.product.code || ''
    form.description = props.product.description || ''
    form.type = props.product.type || ''
    form.supplierValue = props.product.supplierPrice || 0
    form.stockQuantity = props.product.stock || 0
    errors.value = {}
  }
})

// Validação do formulário
const validateForm = () => {
  const newErrors: Record<string, string> = {}
  
  if (!form.code.trim()) {
    newErrors.code = 'Código é obrigatório'
  }
  
  if (!form.type) {
    newErrors.type = 'Tipo é obrigatório'
  }
  
  if (!form.description.trim()) {
    newErrors.description = 'Descrição é obrigatória'
  }
  
  if (form.supplierValue <= 0) {
    newErrors.supplierValue = 'Preço de fornecedor deve ser maior que zero'
  }
  
  if (form.stockQuantity < 0) {
    newErrors.stockQuantity = 'Estoque não pode ser negativo'
  }
  
  errors.value = newErrors
  return Object.keys(newErrors).length === 0
}

// Fechar o modal
const closeModal = () => {
  emit('close')
}

// Enviar o formulário
const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }
  
  if (!props.product) {
    return
  }
  
  loading.value = true
  
  try {
    // Manter os campos originais que não estão no formulário
    const formData = {
      code: form.code,
      description: form.description,
      type: form.type,
      supplierValue: Number(form.supplierValue),
      stockQuantity: Number(form.stockQuantity)
    }
    
    await productStore.updateProduct(props.product.id, formData)
    emit('updated')
    closeModal()
  } catch (error) {
    errors.value.submit = error instanceof Error ? error.message : 'Erro ao salvar produto'
  } finally {
    loading.value = false
  }
}
</script>
