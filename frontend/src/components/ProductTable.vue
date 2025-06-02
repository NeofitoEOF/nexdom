<template>
  <div class="overflow-x-auto rounded-lg shadow border border-gray-200">
    <div class="p-4 bg-white rounded-t-lg">
      <h2 class="text-lg font-bold mb-2">Produtos</h2>
    </div>
    <table class="min-w-full border-separate border-spacing-0 text-center text-sm sm:text-base">
      <thead class="bg-gray-100 sticky top-0">
        <tr>
          <th class="px-2 sm:px-4 py-3 align-middle font-semibold border-b border-gray-200 min-w-[90px]">Código</th>
          <th class="px-2 sm:px-4 py-3 align-middle font-semibold border-b border-gray-200 min-w-[100px]">Tipo</th>
          <th class="px-2 sm:px-4 py-3 align-middle font-semibold border-b border-gray-200 min-w-[120px]">Descrição</th>
          <th class="px-2 sm:px-4 py-3 align-middle font-semibold border-b border-gray-200 min-w-[90px]">Fornecedor</th>
          <th class="px-2 sm:px-4 py-3 align-middle font-semibold border-b border-gray-200 min-w-[70px]">Estoque</th>
          <th class="px-2 sm:px-4 py-3 align-middle font-semibold border-b border-gray-200 min-w-[100px]">Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(product, index) in products" :key="product.id" class="even:bg-gray-50 hover:bg-blue-50 transition border-b border-gray-100" :class="{'bg-gray-100': index % 2 === 0}">
          <td class="px-2 sm:px-4 py-3 align-middle border-b border-gray-100">{{ product.code }}</td>
          <td class="px-2 sm:px-4 py-3 align-middle border-b border-gray-100">{{ getTypeDisplayName(product.type) }}</td>
          <td class="px-2 sm:px-4 py-3 align-middle border-b border-gray-100">{{ product.description }}</td>
          <td class="px-2 sm:px-4 py-3 align-middle border-b border-gray-100">{{ formatCurrency(product.supplierPrice) }}</td>
          <td class="px-2 sm:px-4 py-3 align-middle border-b border-gray-100">{{ formatNumber(product.stock, 0) }}</td>
          <td class="px-2 sm:px-4 py-3 align-middle border-b border-gray-100">
            <div class="flex gap-1 sm:gap-2 justify-center items-center">
              <BaseButton btnClass="btn-outline"
                @click="$emit('edit', product)"
                title="Editar produto"
                aria-label="Editar produto"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600 hover:text-blue-800 transition inline-block" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M16.862 3.487a2.25 2.25 0 113.182 3.182l-10.5 10.5-4 1 1-4 10.5-10.5zM19.5 6.75L17.25 4.5" /></svg>
              </BaseButton>
              <BaseButton btnClass="btn-danger"
                @click="$emit('delete', product)"
                title="Excluir produto"
                aria-label="Excluir produto"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-red-600 hover:text-red-800 transition inline-block" fill="currentColor" viewBox="0 0 24 24"><path d="M6 7V5a3 3 0 013-3h6a3 3 0 013 3v2h5v2h-2v13a2 2 0 01-2 2H4a2 2 0 01-2-2V9H0V7h6zm2 0h8V5a1 1 0 00-1-1H9a1 1 0 00-1 1v2zm10 2H6v11h12V9zm-7 3v5h2v-5h-2z"/></svg>
              </BaseButton>
            </div>
          </td>
        </tr>
        <tr v-if="products.length === 0">
          <td colspan="6" class="text-center p-4 text-gray-500">Nenhum produto encontrado.</td>
        </tr>
      </tbody>
    </table>
    <div class="text-xs text-gray-500 px-2 py-1 sm:hidden">Arraste para o lado para ver mais &rarr;</div>
  </div>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'
import { formatCurrency, formatNumber } from '../utils/formatters'
import { getTypeDisplayName } from '../utils/productTypes'
import type { Product } from '../interfaces'

interface Props {
  products: Product[];
}

defineProps<Props>();

defineEmits<{
  edit: [product: Product];
  
  delete: [product: Product];
  
  view: [product: Product];
}>();
</script>
