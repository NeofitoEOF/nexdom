<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '../../stores/productStore'
import { useInventoryStore } from '../../stores/inventoryStore'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import BaseInput from '../../components/BaseInput.vue'
import BaseSelect from '../../components/BaseSelect.vue'
import BaseButton from '../../components/BaseButton.vue'
import BaseForm from '../../components/BaseForm.vue'
import { formatCurrency } from '../../utils/formatters'
import type { Product, InventoryTransaction } from '../../interfaces'


const handleValueInput = (event: Event): void => {
  const input = (event.target as HTMLInputElement).value
  const numbers = input.replace(/\D/g, '')
  const numberValue = Number(numbers) / 100
  value.value = numberValue
  formattedValue.value = numberValue ? `R$ ${numberValue.toFixed(2).replace('.', ',')}` : 'R$ 0'
}

const traduzirTipoMovimentacao = (tipo: string): string => {
  const traducoes: Record<string, string> = {
    'input': 'Entrada',
    'output': 'Saída',
    'IN': 'Entrada',
    'OUT': 'Saída',
    'ENTRADA': 'Entrada',
    'SAIDA': 'Saída'
  }
  
  return traducoes[tipo] || tipo
}

const router = useRouter()
const productStore = useProductStore()

const inventoryStore = useInventoryStore()

const selectedProductId = ref('')
const quantity = ref(1)
const notes = ref('')
const type = ref<'input' | 'output'>('input')
const value = ref(0)
const formattedValue = ref('R$ 0')
const error = ref('')
const success = ref('')
const loading = ref(false)

const products = computed<Product[]>(() => productStore.products)
const selectedProduct = computed<Product | undefined>(() => {
  if (!selectedProductId.value) return undefined
  return productStore.getProductById(selectedProductId.value)
})

const transactions = computed<InventoryTransaction[]>(() => {
  return [...inventoryStore.transactions]
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
})


interface TransactionWithProductInfo extends Omit<InventoryTransaction, 'productId'> {
  productId: string;
  productCode?: string; 
  productName: string;
  productType: string;
  productNotes: string;
  value: number;
  displayType?: string; 
}

const transactionsWithProductInfo = computed(() => {
  return transactions.value.map(transaction => {
    const product = products.value.find(p => p.id === transaction.productId)
    return {
      ...transaction,
      value: transaction.value || 0,
      productName: product ? product.name : '',
      productType: product ? product.type : '',
      productNotes: product ? product.description : 'Produto Desconhecido',
      displayType: traduzirTipoMovimentacao(transaction.type)
    } as TransactionWithProductInfo
  })
})


const handleTransaction = async (): Promise<void> => {
  if (!selectedProductId.value) {
    error.value = 'Por favor, selecione um produto'
    return
  }
  
  if (quantity.value <= 0) {
    error.value = 'A quantidade deve ser maior que zero'
    return
  }
  
  if (value.value < 0) {
    error.value = 'O valor de venda não pode ser negativo'
    return
  }
  
  if (type.value === 'output' && selectedProduct.value) {
    if (selectedProduct.value.stock < quantity.value) {
      error.value = `Estoque insuficiente. Disponível: ${selectedProduct.value.stock}`
      return
    }
    
    const productTransactions = inventoryStore.getTransactionsByProduct(selectedProductId.value)
    const hasEntryMovements = productTransactions.some((t: any) => t.type === 'input')
    
    if (!hasEntryMovements) {
      error.value = 'Não é possível realizar uma saída sem antes registrar uma entrada para este produto'
      return
    }
  }
  
  error.value = ''
  success.value = ''
  loading.value = true
  
  try {
    const newTransaction = {
      productId: selectedProductId.value,
      type: type.value,
      quantity: parseInt(quantity.value.toString()),
      value: type.value === 'output' ? parseFloat(value.value.toString()) : undefined,
      notes: notes.value
    }
    
    const result = await inventoryStore.addTransaction(newTransaction)
    
    if (result.success) {
      success.value = `${type.value === 'input' ? 'Adicionado' : 'Removido'} ${quantity.value} itens com sucesso`
      selectedProductId.value = ''
      quantity.value = 1
      notes.value = ''
      value.value = 0
      formattedValue.value = 'R$ 0'
      await productStore.fetchProducts()
    } else {
      error.value = result.message || 'Ocorreu um erro'
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Erro ao processar movimentação'
  } finally {
    loading.value = false
  }
}


const formatDate = (date: Date | string): string => {
  try {
    return format(new Date(date), 'dd/MM/yyyy HH:mm', { locale: ptBR })
  } catch (e) {
    return 'Data inválida'
  }
}

const viewProduct = (productId: string | number): void => {
  router.push(`/products/${productId}`)
}

onMounted(async () => {
  loading.value = true
  
  try {
    await Promise.all([
      productStore.fetchProducts(),
      inventoryStore.initialize() 
    ])
    
  } catch (err) {
    error.value = 'Erro ao carregar dados. Por favor, recarregue a página.'
  } finally {
    loading.value = false
  }
})

</script>
<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Movimentação de Estoque</h1>
      <p class="text-gray-600">Gerenciar níveis de estoque dos produtos</p>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols">
      <div class="bg-white p-6 rounded-lg shadow border">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nova Movimentação</h2>
        
        <BaseForm :onSubmit="handleTransaction">

          <BaseSelect
            v-model="selectedProductId"
            :options="products.map(p => ({ value: p.id, label: `${p.name}` }))"
            label="Produto*"
            placeholder="Selecione um produto"
            :error="error && !selectedProductId ? error : ''"
          />

          <div class="mb-4">
            <BaseSelect
              v-model="type"
              label="Tipo de Movimentação"
              :options="[
                { value: 'input', label: 'Entrada' },
                { value: 'output', label: 'Saída' }
              ]"
            />
          </div>

          <BaseInput
            v-model="quantity"
            type="number"
            label="Quantidade*"
            :min="1"
            placeholder="Digite a quantidade"
            :error="error && quantity <= 0 ? error : ''"
          />

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Valor de Venda*</label>
            <div class="relative">
              <BaseInput
                v-model="formattedValue"
                type="text"
                placeholder="R$ 0"
                @input="handleValueInput($event)"
              />
            </div>
          </div>

          <BaseInput
            v-model="notes"
            textarea
            label="Observações"
            placeholder="Digite qualquer observação sobre esta movimentação"
          />

          <div v-if="error" class="mb-4 p-2 bg-red-50 text-red-700 rounded-md">
            {{ error }}
          </div>

          <div v-if="success" class="mb-4 p-2 bg-green-50 text-green-700 rounded-md">
            {{ success }}
          </div>

          <div class="flex justify-center">
            <BaseButton
              :type="'submit'"
              :btnClass="type === 'output' ? 'bg-red-600 hover:bg-red-700 text-white' : 'bg-blue-600 hover:bg-blue-700 text-white'"
              :disabled="loading"
            >
              {{ loading ? 'Processando...' : (type === 'input' ? 'Adicionar ao Estoque' : 'Remover do Estoque') }}
            </BaseButton>
          </div>
        </BaseForm>
        
        <div v-if="selectedProduct" class="mt-6 p-4 bg-gray-50 rounded-lg">
          <h3 class="font-medium text-gray-900 mb-2">{{ selectedProduct.name }}</h3>
          <div class="flex justify-between text-sm">
            <span class="text-gray-600">Nome:</span>
            <span class="font-medium">{{ selectedProduct.name }}</span>
          </div>
          <div class="flex justify-between text-sm mt-1">
            <span class="text-gray-600">Estoque Atual:</span>
            <span 
              :class="[
                'font-medium',
                selectedProduct.stock > 10 
                  ? 'text-green-700' 
                  : selectedProduct.stock > 0 
                    ? 'text-yellow-700' 
                    : 'text-red-700'
              ]"
            >
              {{ selectedProduct.stock }} unidades
            </span>
          </div>
          <div class="flex justify-between text-sm mt-1">
            <span class="text-gray-600">Tipo:</span>
            <span>{{ selectedProduct.type }}</span>
          </div>
          <div class="flex justify-between text-sm mt-1">
            <span class="text-gray-600">Valor do Fornecedor:</span>
            <span>{{ formatCurrency(selectedProduct.supplierPrice) }}</span>
          </div>
          <button 
            @click="viewProduct(Number(selectedProduct.id))"
            class="mt-3 text-sm text-blue-600 hover:text-blue-800"
          >
            Ver Detalhes do Produto →
          </button>
        </div>
      </div>
    </div>

    <div class="py-6 h-screen overflow-y-auto">
      <div class="w-full mx-auto px-2 sm:px-4 lg:px-6">
          <div class="mb-6 flex flex-wrap justify-between items-center gap-4">
            <h1 class="text-2xl font-semibold text-gray-900">Historico de Movimentações</h1>
            <button 
              @click="inventoryStore.fetchTransactions()"
              class="text-sm text-blue-600 hover:text-blue-800"
              :disabled="inventoryStore.isLoading"
            >
              {{ inventoryStore.isLoading ? 'Carregando...' : 'Atualizar' }}
            </button>
          </div>
          
          <div v-if="inventoryStore.isLoading" class="text-center py-4 text-gray-500">
            Carregando movimentações...
          </div>
          
          <div v-else-if="inventoryStore.error" class="text-center py-4 text-red-500">
            Erro: {{ inventoryStore.error }}
          </div>
          
          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Data
                  </th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Produto
                  </th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tipo
                  </th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Quantidade
                  </th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Valor de Venda
                  </th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Descrição
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="transaction in transactionsWithProductInfo" :key="transaction.id" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ formatDate(transaction.date) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <a 
                      href="#"
                      class="text-sm font-medium text-blue-600 hover:text-blue-900"
                    >
                      {{ transaction.productName }}
                    </a>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span 
                      class="px-2 py-1 text-xs font-medium rounded-full"
                      :class="{
                        'bg-green-100 text-green-800': transaction.type === 'input',
                        'bg-red-100 text-red-800': transaction.type === 'output'
                      }"
                    >
                      {{ transaction.type === 'input' ? 'Entrada' : 'Saída' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ transaction.quantity }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ formatCurrency(transaction.value) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ transaction.notes || '-' }}
                  </td>
                </tr>
                
                <tr v-if="transactionsWithProductInfo.length === 0">
                  <td colspan="6" class="px-6 py-4 text-center text-gray-500">
                    Nenhuma movimentação encontrada.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
      </div>
    </div>
  </div>
</template>
