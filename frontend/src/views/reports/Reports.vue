<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '../../stores/productStore'
import { useInventoryStore } from '../../stores/inventoryStore'
import BaseSelect from '../../components/BaseSelect.vue'
import { getTypeDisplayName } from '../../utils/productTypes'
import { formatCurrency, formatNumber } from '../../utils/formatters'

const router = useRouter()
const productStore = useProductStore()
const inventoryStore = useInventoryStore()

onMounted(async () => {
  await productStore.fetchProducts()
  await inventoryStore.fetchTransactions()
  await inventoryStore.fetchMovementsGroupedByProduct()
})

const selectedType = ref('')
const sortField = ref('profit')
const sortDirection = ref('desc')

const productTypes = computed(() => ['', ...productStore.getProductTypes])

interface MovimentoEstoque {
  id: string;
  productId: string;
  type: string;
  quantity: number;
  date: Date;
  value?: number;
  saleValue?: number;
  notes?: string;
}

interface ProdutoLucro {
  id: string;
  nome: string;
  tipo: string;
  qtdDisponivel: number;
  qtdVendida: number;
  receitaTotal: number;
  lucroUnitario: number;
  lucroTotal: number;
  margem: string;
}

interface TotalMetrics {
  products: number;
  sold: number;
  revenue: number;
  profit: number;
}

const produtosLucroBackend = computed<ProdutoLucro[]>(() => {
  const produtos = productStore.products
  const transacoes: MovimentoEstoque[] = inventoryStore.transactions as MovimentoEstoque[]

  return produtos.map(produto => {
    const entradas = transacoes.filter(t => String(t.productId) === String(produto.id) && (t.type === 'IN' || t.type === 'ENTRADA'))
    const saidas = transacoes.filter(t => String(t.productId) === String(produto.id) && (t.type === 'OUT' || t.type === 'SAIDA'))

    const qtdEntradas = entradas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const qtdSaidas = saidas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const qtdDisponivel = qtdEntradas - qtdSaidas
    const qtdVendida = qtdSaidas

    const receitaTotal = saidas.reduce((sum, t) => {
      const precoVenda = typeof t.saleValue === 'number' ? t.saleValue : (typeof t.value === 'number' ? t.value : 0)
      return sum + (precoVenda * (t.quantity || 0))
    }, 0)
    const custoTotal = saidas.reduce((sum, t) => sum + (produto.supplierPrice * (t.quantity || 0)), 0)

    const lucroUnitario = qtdVendida > 0 ? (receitaTotal / qtdVendida) - produto.supplierPrice : 0
    const lucroTotal = receitaTotal - custoTotal
    const margem = receitaTotal > 0 ? ((lucroTotal / receitaTotal) * 100).toFixed(2) + '%' : '0%'

    return {
      id: produto.id,
      nome: produto.name,
      tipo: produto.type,
      qtdDisponivel,
      qtdVendida,
      receitaTotal,
      lucroUnitario,
      lucroTotal,
      margem
    }
  })
})

const totalMetrics = computed<TotalMetrics>(() => {
  const produtos = produtosLucroBackend.value

  return {
    products: produtos.length,
    sold: produtos.reduce((sum: number, p) => sum + (p.qtdVendida || 0), 0),
    revenue: produtos.reduce((sum: number, p) => sum + (p.receitaTotal || 0), 0),
    profit: produtos.reduce((sum: number, p) => sum + (p.lucroTotal || 0), 0)
  }
})

const inventoryByType = computed(() => {
  return inventoryStore.getInventoryStatsByType
})

const setSorting = (field: string) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'desc'
  }
}

const viewProduct = (id: string) => {
  router.push(`/products/${id}`)
}
function safeNumber(val: any): number {
  return typeof val === 'number' && isFinite(val) ? val : 0
}

function safeCurrency(val: any): string {
  return formatCurrency(safeNumber(val))
}

function safeFormatNumber(val: any, decimals: number = 2): string {
  return formatNumber(safeNumber(val), decimals)
}

</script>

<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-6xl mx-auto px-4">
    <div class="mb-10">
      <h1 class="text-3xl font-extrabold text-gray-900 mb-1">Relatórios</h1>
      <p class="text-lg text-gray-600">Análises e insights sobre seu estoque</p>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-10">
      <div class="bg-white rounded-xl shadow border p-6 flex flex-col items-center">
        <div class="text-sm font-semibold text-gray-500 mb-1">Produtos</div>
        <div class="text-4xl font-extrabold text-gray-900">{{ safeFormatNumber(totalMetrics.products, 0) }}</div>
      </div>
      <div class="bg-white rounded-xl shadow border p-6 flex flex-col items-center">
        <div class="text-sm font-semibold text-gray-500 mb-1">Total Vendido</div>
        <div class="text-4xl font-extrabold text-gray-900">{{ safeFormatNumber(totalMetrics.sold, 0) }}</div>
      </div>
      <div class="bg-white rounded-xl shadow border p-6 flex flex-col items-center">
        <div class="text-sm font-semibold text-gray-500 mb-1">Receita</div>
        <div class="text-4xl font-extrabold text-gray-900">{{ safeCurrency(totalMetrics.revenue) }}</div>
      </div>
      <div class="bg-white rounded-xl shadow border p-6 flex flex-col items-center">
        <div class="text-sm font-semibold text-gray-500 mb-1">Lucro</div>
        <div class="text-4xl font-extrabold text-green-600">{{ safeCurrency(totalMetrics.profit) }}</div>
      </div>
    </div>
    
    <div class="bg-white rounded-xl shadow border p-6 mb-10">
      <h2 class="text-xl font-bold text-gray-900 mb-6">Estoque por Tipo</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div class="space-y-6">
          <template v-for="(stats, type) in inventoryByType" :key="type">
            <div>
              <div class="flex justify-between mb-1">
                <span class="text-sm font-medium text-gray-700">{{ type }}</span>
                <span class="text-sm font-medium text-gray-700">
                  {{ stats.totalAvailable }} disponível / {{ stats.totalSold }} vendido
                </span>
              </div>
              <div class="relative pt-1">
                <div class="flex mb-2 items-center justify-between">
                  <div>
                    <span class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-primary-600 bg-primary-100">
                      Available
                    </span>
                  </div>
                  <div>
                    <span class="text-xs font-semibold inline-block py-1 px-2 uppercase rounded-full text-primary-600 bg-primary-100">
                      Sold
                    </span>
                  </div>
                </div>
                <div class="flex h-2 mb-0 overflow-hidden text-xs bg-gray-200 rounded-full">
                  <div 
                    class="flex flex-col justify-center text-center text-white bg-success-500 h-2"
                    :style="`width: ${stats.totalSold > 0 ? (stats.totalAvailable / (stats.totalAvailable + stats.totalSold)) * 100 : 100}%`"
                  ></div>
                  <div 
                    class="flex flex-col justify-center text-center text-white bg-primary-500 h-2"
                    :style="`width: ${stats.totalSold > 0 ? (stats.totalSold / (stats.totalAvailable + stats.totalSold)) * 100 : 0}%`"
                  ></div>
                </div>
              </div>
            </div>
          </template>
        </div>
        
        <div class="flex items-center justify-center">
          <div class="text-center">
            <p class="text-lg font-medium text-gray-800 mb-2">Principais tipos de produtos</p>
            <div class="space-y-2">
              <template v-for="(stats, type) in inventoryByType" :key="type">
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-600">{{ type }}</span>
                  <span class="text-sm font-medium">{{ safeFormatNumber(stats.totalSold, 0) }} units sold</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="bg-white rounded-xl shadow border p-6">
      <div class="flex flex-col md:flex-row md:justify-between md:items-center mb-6 gap-4">
        <h2 class="text-xl font-bold text-gray-900">Análise de Lucro por Produto</h2>
        <div>
          <label class="text-sm font-medium text-gray-700">Filtrar por Tipo:</label>
          <BaseSelect
            v-model="selectedType"
            :options="productTypes.map(type => ({ value: type, label: type ? getTypeDisplayName(type) : 'Todos os Tipos' }))"
            label="Tipo de Produto"
            placeholder="Todos os Tipos"
          />
        </div>
      </div>
      <div class="overflow-x-auto rounded-lg border">
        <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg">
          <thead class="bg-gray-50">
            <tr>
              <th 
  scope="col" 
  class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
  @click="setSorting('name')"
>
  Produto
  <span v-if="sortField === 'name'" class="ml-1">
    {{ sortDirection === 'asc' ? '↑' : '↓' }}
  </span>
</th>
<th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
  Tipo
</th>
<th 
  scope="col" 
  class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
  @click="setSorting('stock')"
>
  Quantidade Disponível
  <span v-if="sortField === 'stock'" class="ml-1">
    {{ sortDirection === 'asc' ? '↑' : '↓' }}
  </span>
</th>
<th 
  scope="col" 
  class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
  @click="setSorting('sold')"
>
  Quantidade Vendida
  <span v-if="sortField === 'sold'" class="ml-1">
    {{ sortDirection === 'asc' ? '↑' : '↓' }}
  </span>
</th>
<th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
  Receita
</th>
<th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
  Lucro Unitário<br/>(Venda - Fornecedor)
</th>
<th 
  scope="col" 
  class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
  @click="setSorting('profit')"
>
  Lucro Total<br/>(Lucro Unitário × Vendido)
  <span v-if="sortField === 'profit'" class="ml-1">
    {{ sortDirection === 'asc' ? '↑' : '↓' }}
  </span>
  <span class="ml-1 cursor-pointer" title="Lucro total = (Valor de Venda - Valor do Fornecedor) x Quantidade Vendida">
    <svg xmlns="http://www.w3.org/2000/svg" class="inline h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><title>Lucro total = (Valor de Venda - Valor do Fornecedor) x Quantidade Vendida</title><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z" /></svg>
  </span>
</th>
<th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
  Margem
</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="produto in produtosLucroBackend" :key="produto.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <a 
                  @click.prevent="viewProduct(produto.id)"
                  href="#"
                  class="text-sm font-medium text-primary-600 hover:text-primary-900"
                >
                  {{ produto.nome }}
                </a>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-primary-100 text-primary-800">
                  {{ produto.tipo }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ safeFormatNumber(produto.qtdDisponivel, 0) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ safeFormatNumber(produto.qtdVendida, 0) }}
                <span class="ml-1 cursor-pointer" title="Quantidade total de vendas (saídas) deste produto">
                  <svg xmlns="http://www.w3.org/2000/svg" class="inline h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><title>Quantidade total de vendas (saídas) deste produto</title><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z" /></svg>
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ safeCurrency(produto.receitaTotal) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                {{ safeCurrency(produto.lucroUnitario) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <span :class="{
                  'text-green-600 font-bold': produto.lucroTotal > 0,
                  'text-red-600 font-bold': produto.lucroTotal < 0,
                  'text-gray-500': produto.lucroTotal === 0
                }">
                  {{ safeCurrency(produto.lucroTotal) }}
                  <span v-if="produto.lucroTotal > 0" title="Lucro positivo">▲</span>
                  <span v-else-if="produto.lucroTotal < 0" title="Prejuízo">▼</span>
                  <span v-else title="Nenhum lucro">–</span>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <div class="bg-white rounded-xl shadow border p-6 mt-10">
      <h2 class="text-xl font-bold text-gray-900 mb-6">Movimentações Detalhadas por Produto</h2>
      <div class="overflow-x-auto rounded-lg border">
        <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg">
          <thead>
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Produto</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entradas</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                UNITS SOLD
                <span class="ml-1 cursor-pointer" title="Quantidade total de vendas (saídas) deste produto">
                  <svg xmlns="http://www.w3.org/2000/svg" class="inline h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><title>Quantidade total de vendas (saídas) deste produto</title><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z" /></svg>
                </span>
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                PROFIT TOTAL
                <span class="ml-1 cursor-pointer" title="Lucro total = (Preço de venda - Preço fornecedor) x Unidades vendidas">
                  <svg xmlns="http://www.w3.org/2000/svg" class="inline h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><title>Lucro total = (Preço de venda - Preço fornecedor) x Unidades vendidas</title><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z" /></svg>
                </span>
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valor Total Saídas</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lucro Real</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="(movements, productId) in inventoryStore.groupedMovements" :key="productId">
              <td class="px-6 py-4 whitespace-nowrap">{{ movements[0]?.productName }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ safeFormatNumber(movements.filter(m => m.type === 'ENTRADA').reduce((sum, m) => sum + m.quantity, 0), 0) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">{{ safeFormatNumber(movements.filter(m => m.type === 'SAIDA').reduce((sum, m) => sum + m.quantity, 0), 0) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ safeCurrency(movements.filter(m => m.type === 'ENTRADA').reduce((sum, m) => sum + (m.value * m.quantity), 0)) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{ safeCurrency(movements.filter(m => m.type === 'SAIDA').reduce((sum, m) => sum + (m.value * m.quantity), 0)) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                {{
                  safeCurrency(
                    movements.filter(m => m.type === 'SAIDA').reduce((sum, m) => sum + (m.value * m.quantity), 0)
                    -
                    movements.filter(m => m.type === 'ENTRADA').reduce((sum, m) => sum + (m.value * m.quantity), 0)
                  )
                }}
              </td>
            </tr>
            <tr v-if="Object.keys(inventoryStore.groupedMovements ?? {}).length === 0">
              <td colspan="6" class="px-6 py-4 text-center text-gray-500">Nenhuma movimentação encontrada.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

      </div>
    </div>
</template>