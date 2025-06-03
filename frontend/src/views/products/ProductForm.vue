<template>
  <div class="container mx-auto max-w-5xl px-4 py-10">
    <div class="flex flex-col gap-8">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div class="flex items-center">
          <div class="bg-blue-600 p-3 rounded-lg shadow-lg mr-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10" />
            </svg>
          </div>
          <div>
            <h1 class="text-3xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">{{ isEdit ? 'Editar Produto' : 'Criar Produto' }}</h1>
            <p class="text-gray-500 text-base mt-1">{{ isEdit ? 'Atualize as informações do produto.' : 'Adicione um novo produto ao catálogo.' }}</p>
          </div>
        </div>
        <div class="flex gap-2 mt-4 sm:mt-0">
          <BaseButton btnClass="btn-outline hover:bg-blue-50 transition-all duration-200" @click="$router.push('/products')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 -ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Voltar para lista
          </BaseButton>
        </div>
      </div>
      
      <div class="bg-white rounded-xl shadow-lg p-8 border border-gray-100">
        <form @submit.prevent="handleSubmit">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-8">
            <div class="relative">
              <label for="type" class="block text-sm font-semibold text-gray-700 mb-2 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                </svg>
                Tipo
              </label>
              <div class="relative">
                <BaseSelect
                  id="type"
                  v-model="form.type"
                  :options="productTypes"
                  class="select w-full rounded-lg border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 shadow-sm"
                  :class="{ 'border-danger-500 ring-1 ring-danger-500': formSubmitted && errors.type }"
                />
              </div>
              <p v-if="formSubmitted && errors.type" class="mt-1 text-sm text-danger-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {{ errors.type }}
              </p>
            </div>
            
            <div class="relative">
              <label for="name" class="block text-sm font-semibold text-gray-700 mb-2 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Nome
              </label>
              <div class="relative">
                <BaseInput
                  id="name"
                  v-model="form.name"
                  type="text"
                  class="input w-full pl-3 pr-10 py-2.5 rounded-lg border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 shadow-sm"
                  :class="{ 'border-danger-500 ring-1 ring-danger-500': formSubmitted && errors.name }"
                  placeholder="Nome do produto"
                />
              </div>
              <p v-if="formSubmitted && errors.name" class="mt-1 text-sm text-danger-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {{ errors.name }}
              </p>
            </div>

            <div class="relative">
              <label for="supplierPrice" class="block text-sm font-semibold text-gray-700 mb-2 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Preço de custo
              </label>
              <div class="relative">
                <BaseInput
                  id="supplierPrice"
                  v-model="form.supplierPrice"
                  type="number"
                  class="input w-full pl-3 pr-10 py-2.5 rounded-lg border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 shadow-sm"
                  :class="{ 'border-danger-500 ring-1 ring-danger-500': formSubmitted && errors.supplierPrice }"
                  placeholder="Preço de custo do produto"
                />
                <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
                  <span>R$</span>
                </div>
              </div>
              <p v-if="formSubmitted && errors.supplierPrice" class="mt-1 text-sm text-danger-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {{ errors.supplierPrice }}
              </p>
            </div>



            <div class="relative">
              <label for="description" class="block text-sm font-semibold text-gray-700 mb-2 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
                </svg>
                Descrição
              </label>
              <div class="relative">
                <BaseInput
                  id="description"
                  v-model="form.description"
                  type="text"
                  class="input w-full pl-3 pr-10 py-2.5 rounded-lg border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 shadow-sm"
                  :class="{ 'border-danger-500 ring-1 ring-danger-500': formSubmitted && errors.description }"
                  placeholder="Descrição do produto"
                />
              </div>
              <p v-if="formSubmitted && errors.description" class="mt-1 text-sm text-danger-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {{ errors.description }}
              </p>
            </div>

            <div class="relative">
              <label for="stock" class="block text-sm font-semibold text-gray-700 mb-2 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l4 2" />
                </svg>
                Estoque
              </label>
              <div class="relative">
                <BaseInput
                  id="stock"
                  v-model="form.stock"
                  type="number"
                  class="input w-full pl-3 pr-10 py-2.5 rounded-lg border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 shadow-sm"
                  :class="{ 'border-danger-500 ring-1 ring-danger-500': formSubmitted && errors.stock }"
                  placeholder="Estoque do produto"
                />
                <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
                  <span>un</span>
                </div>
              </div>
              <p v-if="formSubmitted && errors.stock" class="mt-1 text-sm text-danger-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {{ errors.stock }}
              </p>
            </div>

            <div class="md:col-span-2 flex justify-end gap-4 mt-8">
              <BaseButton
                type="button"
                btnClass="btn-outline hover:bg-gray-50 transition-all duration-200 px-6 py-2.5 rounded-lg"
                @click="$router.push('/products')"
              >
                <span class="flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  Cancelar
                </span>
              </BaseButton>
              <BaseButton
                type="submit"
                btnClass="btn-primary bg-blue-600 hover:bg-blue-700 text-white transition-all duration-200 px-6 py-2.5 rounded-lg shadow-md hover:shadow-lg"
                :loading="loading"
              >
                <span v-if="!loading" class="flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path v-if="isEdit" stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                    <path v-else stroke-linecap="round" stroke-linejoin="round" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  {{ isEdit ? 'Atualizar Produto' : 'Criar Produto' }}
                </span>
                <span v-else class="flex items-center">
                  <svg class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Processando...
                </span>
              </BaseButton>
            </div>

            <div v-if="errors.submit" class="md:col-span-2 mt-4 p-4 bg-danger-50 border border-danger-200 text-danger-700 rounded-lg shadow-sm">
              <div class="flex items-center">
                <div class="bg-danger-100 rounded-full p-1 mr-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-danger-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <span class="font-medium">{{ errors.submit }}</span>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '../../stores/productStore'
import { useProductForm } from '../../composables/useProductForm'
import BaseInput from '../../components/BaseInput.vue'
import BaseSelect from '../../components/BaseSelect.vue'
import BaseButton from '../../components/BaseButton.vue'

const {
  form,
  errors,
  loading,
  formSubmitted,
  isEdit,
  handleSubmit,
  productTypes: readonlyProductTypes
} = useProductForm()

const productTypes = [...readonlyProductTypes]

const productStore = useProductStore()
const router = useRouter()
const productId = ref<string | undefined>(undefined)

onMounted(async (): Promise<void> => {
  await productStore.fetchProducts();
  if (isEdit.value) {
    const existingProduct = productStore.getProductById(productId.value || '')
    if (existingProduct) {
      form.value = {
        name: existingProduct.name || '',
        description: existingProduct.description || '',
        type: existingProduct.type || '',
        supplierPrice: existingProduct.supplierPrice || 0,
        stock: existingProduct.stock || 0
      }
    } else {
      router.push('/products')
    }
  }
})
</script>