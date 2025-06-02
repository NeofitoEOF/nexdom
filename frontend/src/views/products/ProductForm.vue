<template>
  <div class="container mx-auto max-w-7xl px-2 sm:px-4 lg:px-8 py-8">
    <div class="flex flex-col gap-8">
      <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between mb-2 gap-1">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">{{ isEdit ? 'Editar Produto' : 'Criar Produto' }}</h1>
          <p class="text-gray-500 text-base mt-1">{{ isEdit ? 'Atualize as informações do produto.' : 'Adicione um novo produto ao catálogo.' }}</p>
        </div>
        <div class="flex gap-2 mt-4 sm:mt-0">
          <BaseButton btnClass="btn-outline" @click="$router.push('/products')">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 -ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
            Voltar para lista
          </BaseButton>
        </div>
      </div>
      
      <div class="bg-white rounded-xl shadow p-6">
        <form @submit.prevent="handleSubmit">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
            <div>
              <label for="type" class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
              <BaseSelect
                id="type"
                v-model="form.type"
                :options="productTypes"
                class="select w-full"
                :class="{ 'border-danger-500': formSubmitted && errors.type }"
              />
              <p v-if="formSubmitted && errors.type" class="mt-1 text-sm text-danger-600">{{ errors.type }}</p>
            </div>
            
            <div>
              <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Nome</label>
              <BaseInput
                id="name"
                v-model="form.name"
                type="text"
                class="input w-full"
                :class="{ 'border-danger-500': formSubmitted && errors.name }"
                placeholder="Nome do produto"
              />
              <p v-if="formSubmitted && errors.name" class="mt-1 text-sm text-danger-600">{{ errors.name }}</p>
            </div>

            <div>
              <label for="supplierPrice" class="block text-sm font-medium text-gray-700 mb-1">Preço de custo</label>
              <BaseInput
                id="supplierPrice"
                v-model="form.supplierPrice"
                type="number"
                class="input w-full"
                :class="{ 'border-danger-500': formSubmitted && errors.supplierPrice }"
                placeholder="Preço de custo do produto"
              />
              <p v-if="formSubmitted && errors.supplierPrice" class="mt-1 text-sm text-danger-600">{{ errors.supplierPrice }}</p>
            </div>

            <div>
              <label for="sellingPrice" class="block text-sm font-medium text-gray-700 mb-1">Preço de venda</label>
              <BaseInput
                id="sellingPrice"
                v-model="form.sellingPrice"
                type="number"
                class="input w-full"
                :class="{ 'border-danger-500': formSubmitted && errors.sellingPrice }"
                placeholder="Preço de venda do produto"
              />
              <p v-if="formSubmitted && errors.sellingPrice" class="mt-1 text-sm text-danger-600">{{ errors.sellingPrice }}</p>
            </div>

            <div>
              <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Descrição</label>
              <BaseInput
                id="description"
                v-model="form.description"
                type="text"
                class="input w-full"
                :class="{ 'border-danger-500': formSubmitted && errors.description }"
                placeholder="Descrição do produto"
              />
              <p v-if="formSubmitted && errors.description" class="mt-1 text-sm text-danger-600">{{ errors.description }}</p>
            </div>

            <div>
              <label for="stock" class="block text-sm font-medium text-gray-700 mb-1">Estoque</label>
              <BaseInput
                id="stock"
                v-model="form.stock"
                type="number"
                class="input w-full"
                :class="{ 'border-danger-500': formSubmitted && errors.stock }"
                placeholder="Estoque do produto"
              />
              <p v-if="formSubmitted && errors.stock" class="mt-1 text-sm text-danger-600">{{ errors.stock }}</p>
            </div>

            <div class="md:col-span-2 flex justify-end gap-3 mt-6">
              <BaseButton
                type="button"
                btnClass="btn-outline"
                @click="$router.push('/products')"
              >
                Cancelar
              </BaseButton>
              <BaseButton
                type="submit"
                btnClass="btn-primary"
                :loading="loading"
              >
                <svg v-if="!loading" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 -ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path v-if="isEdit" stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                  <path v-else stroke-linecap="round" stroke-linejoin="round" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                {{ isEdit ? 'Atualizar Produto' : 'Criar Produto' }}
              </BaseButton>
          </div>

            <div v-if="errors.submit" class="md:col-span-2 mt-4 p-3 bg-danger-50 border border-danger-200 text-danger-700 rounded-md">
              <div class="flex">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{{ errors.submit }}</span>
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
        sellingPrice: existingProduct.sellingPrice || 0,
        stock: existingProduct.stock || 0
      }
    } else {
      router.push('/products')
    }
  }
})
</script>