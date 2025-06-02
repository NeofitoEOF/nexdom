<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '../../stores/productStore'
import ProductTable from '../../components/ProductTable.vue'
import BaseInput from '../../components/BaseInput.vue'
import BaseSelect from '../../components/BaseSelect.vue'
import BaseButton from '../../components/BaseButton.vue'
import type { Product, ProductSearchFilters } from '../../interfaces'
const router = useRouter()
const productStore = useProductStore()
const loading = ref(false)
const error = ref('')
const searchQuery = ref('')
const selectedType = ref('')
const refreshTrigger = ref(0) 
const loadProducts = async (): Promise<void> => {
  loading.value = true
  error.value = ''
  try {
    await productStore.fetchProducts()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Erro desconhecido ao carregar produtos'
  } finally {
    loading.value = false
  }
}
const triggerRefresh = (): void => {
  refreshTrigger.value++
  loadProducts()
}
onMounted((): void => {
  loadProducts()
})
watch(refreshTrigger, (): void => {
  loadProducts()
})
const productTypes = computed((): string[] => {
  const types = productStore.getProductTypes || []
  return types.filter(type => type && type.trim() !== '')
})

const filteredProducts = computed<Product[]>(() => {
  const filters: ProductSearchFilters = {}
  
  if (searchQuery.value) {
    filters.name = searchQuery.value
  }
  
  if (selectedType.value) {
    filters.type = selectedType.value
  }
  
  return productStore.getFilteredProducts(filters)
})
const viewProduct = (product: Product): void => {
  router.push(`/products/${product.id}`)
}
const editProduct = (product: Product): void => {
  router.push(`/products/${product.id}/edit`)
}

const deleteProduct = async (product: Product): Promise<void> => {
  if (confirm('Tem certeza que deseja excluir este produto?')) {
    try {
      const result = await productStore.removeProduct(product.id)
      if (!result) {
        throw new Error('Erro ao excluir produto')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Erro ao excluir produto. Tente novamente.'
    }
  }
}
const createProduct = (): void => {
  router.push('/products/create')
}
const refreshProducts = (): void => {
  triggerRefresh()
}
</script>
<template>
  <div class="container mx-auto max-w-7xl px-2 sm:px-4 lg:px-8 py-8">
    <div class="flex flex-col gap-8">
      <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between mb-2 gap-1">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Produtos</h1>
          <p class="text-gray-500 text-base mt-1">Gerencie seu catálogo de produtos.</p>
        </div>
        <div class="flex gap-2 mt-4 sm:mt-0">
          <BaseButton btnClass="btn-outline" @click="refreshProducts">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 -ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0A8.003 8.003 0 0012 20a8.003 8.003 0 007.418-11M4.582 9H9" /></svg>
            Atualizar
          </BaseButton>
          <BaseButton btnClass="btn-primary" @click="createProduct">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 -ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" /></svg>
            Novo Produto
          </BaseButton>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow p-4 flex flex-col sm:flex-row gap-4 items-stretch sm:items-end mb-2">
        <div class="flex-1">
          <BaseInput v-model="searchQuery" placeholder="Buscar produtos..." class="w-full" />
        </div>
        <div class="flex-1">
          <BaseSelect v-model="selectedType" :options="productTypes" class="w-full" :disabled="loading" />
        </div>
      </div>
      <div>
        <div v-if="loading" class="flex justify-center items-center min-h-48">
          <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-primary-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
          <span class="text-gray-600">Carregando produtos...</span>
        </div>
        <div v-else>
          <ProductTable :products="filteredProducts" @edit="editProduct" @delete="deleteProduct" @view="viewProduct" />
          <div v-if="filteredProducts.length === 0" class="text-center text-gray-500">
            Nenhum produto encontrado.
            <button @click="createProduct" class="text-primary-600 hover:text-primary-900 font-medium">Adicionar novo produto</button>.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>