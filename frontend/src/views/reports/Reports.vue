<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
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

// --- Simulação de Projeção de Lucro ---
const simulacaoProdutoId = ref('')
const simulacaoQtd = ref(1)
const simulacaoPreco = ref(0)

// Atualiza o preço simulado para o preço de venda do produto ao trocar produto
watch(simulacaoProdutoId, (id) => {
  const produto = produtosLucroBackend.value.find(p => p.id === id)
  simulacaoPreco.value = produto ? produto.receitaTotal && produto.qtdVendida > 0 ? produto.receitaTotal / produto.qtdVendida : 0 : 0
})

const simulacaoResultado = computed(() => {
  const produto = produtosLucroBackend.value.find(p => p.id === simulacaoProdutoId.value)
  if (!produto || simulacaoQtd.value <= 0 || simulacaoPreco.value < 0) return null
  const receita = simulacaoPreco.value * simulacaoQtd.value
  // custo médio estimado por unidade
  const custoMedio = produto.qtdVendida > 0 ? (produto.receitaTotal - produto.lucroTotal) / produto.qtdVendida : 0
  const custoTotal = custoMedio * simulacaoQtd.value
  const lucro = receita - custoTotal
  const margem = custoTotal > 0 ? ((lucro / custoTotal) * 100).toFixed(1) : '0.0'
  return { receita, lucro, margem }
})
// --- Fim Simulação ---

const selectedType = ref('')
const sortField = ref('profit')
const sortDirection = ref('desc')

const productTypes = computed(() => ['', ...productStore.getProductTypes])

/**
 * Subtotais por tipo de produto (estoque, vendido, receita, custo, lucro, margem)
 * @returns Record<string, ProfitSubtotal>
 */
interface ProfitSubtotal {
  totalAvailable: number;
  totalSold: number;
  totalSalesValue: number;
  totalCost: number;
  totalProfit: number;
}

// Subtotais por tipo de produto com ajustes de sinais e limites
const subtotalByType = computed<Record<string, ProfitSubtotal & { margin: string }>>(() => {
  const result: Record<string, ProfitSubtotal & { margin: string }> = {}
  const products = productStore.products
  const grouped = products.reduce<Record<string, typeof products>>((acc, p) => {
    if (!acc[p.type]) acc[p.type] = []
    acc[p.type].push(p)
    return acc
  }, {})
  Object.entries(grouped).forEach(([type, products]) => {
    // Soma estoques, vendidos, receita, custo e lucro
    let totalAvailable = products.reduce((sum, p) => sum + (p.stock ?? 0), 0)
    let totalSold = products.reduce((sum, p) => sum + (p.totalSold ?? 0), 0)
    let totalSalesValue = products.reduce((sum, p) => sum + (p.totalSalesValue ?? 0), 0)
    let totalCost = products.reduce((sum, p) => sum + (p.totalCost ?? 0), 0)
    let totalProfit = products.reduce((sum, p) => sum + (p.totalProfit ?? 0), 0)

    // Ajustes de sinais: nunca negativos
    totalAvailable = Math.max(totalAvailable, 0)
    totalSold = Math.max(totalSold, 0)
    totalSalesValue = Math.max(totalSalesValue, 0)
    totalCost = Math.max(totalCost, 0)
    totalProfit = Math.max(totalProfit, 0)

    // Margem (%) limitada a 9999, e mostra '—' se custo zero
    let margin: string
    if (totalCost > 0) {
      const rawMargin = (totalProfit / totalCost) * 100
      margin = `${Math.min(Math.max(rawMargin, 0), 9999).toFixed(1)}`
    } else {
      margin = '—'
    }

    result[type] = { totalAvailable, totalSold, totalSalesValue, totalCost, totalProfit, margin }
  })
  return result
})

// Atualize também o template para usar subtotal.margin

// --- Helpers para Análise de Lucro por Produto ---
import { ref as vueRef } from 'vue'

const searchQuery = vueRef('')

const totalMetrics = computed(() => ({
  products: filteredProdutosLucro.value.length,
  sold: totalSold.value,
  revenue: totalSalesValue.value,
  profit: totalProfit.value
}))

const filteredProdutosLucro = computed(() => {
  if (!searchQuery.value) return produtosLucroBackend.value
  return produtosLucroBackend.value.filter(p =>
    p.nome.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const totalAvailable = computed(() =>
  filteredProdutosLucro.value.reduce((sum, p) => sum + p.qtdDisponivel, 0)
)
const totalSold = computed(() =>
  filteredProdutosLucro.value.reduce((sum, p) => sum + p.qtdVendida, 0)
)
const totalSalesValue = computed(() =>
  filteredProdutosLucro.value.reduce((sum, p) => sum + p.receitaTotal, 0)
)
const totalCost = computed(() =>
  filteredProdutosLucro.value.reduce((sum, p) => sum + p.custoTotal, 0)
)
const totalProfit = computed(() =>
  filteredProdutosLucro.value.reduce((sum, p) => sum + p.lucroTotal, 0)
)
const averageProfitMargin = computed(() =>
  totalCost.value > 0 ? (totalProfit.value / totalCost.value) * 100 : 0
)

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
  // Usar os campos do store (já adaptados pelo fetchProducts)
  const produtos = productStore.products
  const transacoes: MovimentoEstoque[] = inventoryStore.transactions as any[]

  if (!produtos.length || !transacoes.length) return []

  return produtos.map(produto => {
    const id = produto.id
    const nome = produto.code || produto.name || produto.description || '—'
    const tipo = produto.type || '—'
    const supplierValue = produto.supplierPrice || 0
    const qtdEstoque = produto.stock || 0

    // Movimentações desse produto
    const entradas = transacoes.filter(t => String(t.productId) === String(id) && (t.type === 'input'))
    const saidas = transacoes.filter(t => String(t.productId) === String(id) && (t.type === 'output'))

    const qtdEntradas = entradas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const qtdSaidas = saidas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const qtdDisponivel = qtdEntradas - qtdSaidas
    const qtdVendida = qtdSaidas

    // Receita total: soma de value * quantity nas saídas
    const receitaTotal = saidas.reduce((sum, t) => sum + ((t.value || 0) * (t.quantity || 0)), 0)
    // Custo total: soma de supplierValue * quantity nas entradas
    const custoTotal = entradas.reduce((sum, t) => sum + (supplierValue * (t.quantity || 0)), 0)
    const lucroUnitario = qtdVendida > 0 ? (receitaTotal - custoTotal) / qtdVendida : 0
    const lucroTotal = receitaTotal - custoTotal
    const margem = custoTotal > 0 ? ((lucroTotal / custoTotal) * 100).toFixed(1) : '0.0'

    return {
      id: String(id),
      nome,
      tipo,
      qtdDisponivel,
      qtdVendida,
      receitaTotal,
      custoTotal,
      lucroUnitario,
      lucroTotal,
      margem
    }
  })
})
const inventoryByType = computed(() => {
  return inventoryStore.getInventoryStatsByType
})
// Expose inventoryByType to the template
defineExpose({ inventoryByType })

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

      <!-- Simulação de Projeção de Lucro -->
      <div class="bg-white rounded-xl shadow border p-6 mb-10">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Simulação de Projeção de Lucro</h2>
        <div class="flex flex-col md:flex-row gap-4 items-end">
          <div class="flex-1">
            <label class="block text-sm font-medium text-gray-700 mb-1">Produto</label>
            <select v-model="simulacaoProdutoId" class="w-full border rounded px-2 py-1">
              <option value="">Selecione...</option>
              <option v-for="p in produtosLucroBackend" :key="p.id" :value="p.id">{{ p.nome }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Qtd. simulada</label>
            <input type="number" v-model.number="simulacaoQtd" min="1" class="w-24 border rounded px-2 py-1" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Preço simulado (R$)</label>
            <input type="number" v-model.number="simulacaoPreco" min="0" step="0.01" class="w-28 border rounded px-2 py-1" />
          </div>
        </div>
        <div v-if="simulacaoResultado" class="mt-6 bg-gray-50 rounded p-4 flex flex-col md:flex-row gap-8 justify-between items-center">
          <div>
            <div class="text-xs text-gray-500">Receita projetada</div>
            <div class="text-lg font-bold text-yellow-700">{{ safeCurrency(simulacaoResultado.receita) }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500">Lucro projetado</div>
            <div class="text-lg font-bold text-green-700">{{ safeCurrency(simulacaoResultado.lucro) }}</div>
          </div>
          <div>
            <div class="text-xs text-gray-500">Margem (%)</div>
            <div class="text-lg font-bold text-blue-700">{{ simulacaoResultado.margem }}%</div>
          </div>
        </div>
      </div>
      <!-- Fim Simulação de Projeção -->

      <!-- Relatórios de Destaque -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-10">

      <!-- Produtos com maior lucro -->
      <div class="bg-white rounded-xl shadow border p-6 mb-10">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Produtos com maior lucro</h2>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead>
              <tr>
                <th class="px-4 py-2 text-left text-xs font-semibold text-gray-500 uppercase">Produto</th>
                <th class="px-4 py-2 text-left text-xs font-semibold text-gray-500 uppercase">Tipo</th>
                <th class="px-4 py-2 text-right text-xs font-semibold text-gray-500 uppercase">Qtd. Vendida</th>
                <th class="px-4 py-2 text-right text-xs font-semibold text-gray-500 uppercase">Receita</th>
                <th class="px-4 py-2 text-right text-xs font-semibold text-gray-500 uppercase">Lucro</th>
                <th class="px-4 py-2 text-right text-xs font-semibold text-gray-500 uppercase">Margem (%)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in produtosLucroBackend.slice().sort((a, b) => b.lucroTotal - a.lucroTotal).slice(0, 5)" :key="p.id" class="hover:bg-gray-50">
                <td class="px-4 py-2 font-medium text-gray-900">{{ p.nome }}</td>
                <td class="px-4 py-2 text-gray-700">{{ p.tipo }}</td>
                <td class="px-4 py-2 text-right">{{ safeFormatNumber(p.qtdVendida, 0) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(p.receitaTotal) }}</td>
                <td class="px-4 py-2 text-right text-green-700 font-semibold">{{ safeCurrency(p.lucroTotal) }}</td>
                <td class="px-4 py-2 text-right">{{ p.margem }}%</td>
              </tr>
              <tr v-if="!produtosLucroBackend.length">
                <td colspan="6" class="px-4 py-2 text-center text-gray-400">Sem dados</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- Fim Produtos com maior lucro -->

        <!-- Produto mais lucrativo -->
        <div class="bg-white rounded-xl shadow border p-6 flex flex-col items-center">
          <div class="text-sm font-semibold text-gray-500 mb-1">Produto mais lucrativo</div>
          <div v-if="produtosLucroBackend.length" class="text-center">
            <div class="text-lg font-bold text-gray-800 mb-1">
              <span>{{ produtosLucroBackend.slice().sort((a, b) => b.lucroTotal - a.lucroTotal)[0]?.nome || '—' }}</span>
            </div>
            <div class="text-green-600 text-2xl font-extrabold">
              {{ safeCurrency(produtosLucroBackend.slice().sort((a, b) => b.lucroTotal - a.lucroTotal)[0]?.lucroTotal) }}
            </div>
            <div class="text-xs text-gray-400">Lucro total</div>
          </div>
          <div v-else class="text-gray-400">Sem dados</div>
        </div>
      </div>
      <!-- Fim dos relatórios de destaque -->

      <!-- Bloco de cartões de totais/subtotais e análise de lucro (migrado do InventoryTransactions.vue) -->
      
      <!-- Fim do bloco migrado -->

      <!-- Subtotais por Tipo de Produto -->
      <div class="bg-white rounded-xl shadow border p-6 mb-10">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Subtotais por Tipo de Produto</h2>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Tipo</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Estoque</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Vendido</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Receita</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Custo</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Lucro</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Margem (%)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(subtotal, type) in subtotalByType" :key="type" class="hover:bg-gray-50">
                <td class="px-4 py-2 font-semibold text-gray-700">{{ getTypeDisplayName(type) }}</td>
                <td class="px-4 py-2 text-right">{{ safeFormatNumber(subtotal.totalAvailable, 0) }}</td>
                <td class="px-4 py-2 text-right">{{ safeFormatNumber(subtotal.totalSold, 0) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(subtotal.totalSalesValue) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(subtotal.totalCost) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(subtotal.totalProfit) }}</td>
                <td class="px-4 py-2 text-right">{{ subtotal.margin }}<span v-if="subtotal.margin !== '—'">%</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      *
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

      <!-- Análise de Lucro por Produto (fiel ao relatório original) -->
      <div class="bg-white rounded-xl shadow border p-6">
        <div class="mb-4 flex flex-wrap justify-between items-center gap-3">
          <div class="text-sm text-gray-500">
            <span class="font-medium">{{ produtosLucroBackend.length }}</span> produtos analisados
          </div>
          <div class="relative text-gray-600 focus-within:text-gray-400">
            <span class="absolute inset-y-0 left-0 flex items-center pl-2">
              <span class="material-icons text-gray-400 text-sm">search</span>
            </span>
            <input v-model="searchQuery" type="search"
              class="py-2 pl-10 pr-3 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="Filtrar produtos..." aria-label="Filtrar produtos">
          </div>
        </div>
        <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Produto</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tipo</th>
              <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Estoque</th>
              <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Vendido</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Preço Médio</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Custo</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Margem</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Lucro</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="produto in filteredProdutosLucro" :key="produto.id" class="hover:bg-gray-50">
              <td class="px-4 py-3">{{ produto.nome }}</td>
              <td class="px-4 py-3">{{ produto.tipo }}</td>
              <td class="px-4 py-3 text-center">{{ safeFormatNumber(produto.qtdDisponivel, 0) }}</td>
              <td class="px-4 py-3 text-center">{{ safeFormatNumber(produto.qtdVendida, 0) }}</td>
              <td class="px-4 py-3 text-right">{{ safeCurrency(produto.precoMedioVenda) }}</td>
              <td class="px-4 py-3 text-right">{{ safeCurrency(produto.custoTotal) }}</td>
              <td class="px-4 py-3 text-right">{{ produto.margem }}%</td>
              <td class="px-4 py-3 text-right">
                {{ safeCurrency(produto.lucroTotal) }}
                <div class="w-24 h-2 bg-white bg-opacity-30 rounded-full mt-1 overflow-hidden float-right">
                  <div class="h-full rounded-full bg-white"
                    :style="{ width: Math.min(Math.abs(Number(produto.margem)), 100) + '%' }"></div>
                </div>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="bg-gray-100 font-semibold">
              <td class="px-4 py-3 text-right" colspan="2">Total Geral:</td>
              <td class="px-4 py-3 text-center">{{ safeFormatNumber(totalAvailable, 0) }} un</td>
              <td class="px-4 py-3 text-center">{{ safeFormatNumber(totalSold, 0) }} un</td>
              <td class="px-4 py-3 text-right">{{ totalSold > 0 ? safeCurrency(totalSalesValue / totalSold) : '-' }}
              </td>
              <td class="px-4 py-3 text-right">{{ safeCurrency(totalCost) }}</td>
              <td class="px-4 py-3 text-right">{{ safeFormatNumber(averageProfitMargin, 1) }}%</td>
              <td class="px-4 py-3 text-right">
                {{ safeCurrency(totalProfit) }}
                <div class="w-24 h-2 bg-white bg-opacity-30 rounded-full mt-1 overflow-hidden float-right">
                  <div class="h-full rounded-full bg-white"
                    :style="{ width: Math.min(Math.abs(averageProfitMargin), 100) + '%' }"></div>
                </div>
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      <table class="min-w-full divide-y divide-gray-200 bg-white rounded-lg">
        <thead>
          <tr>
            <th scope="col"
              class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
              @click="setSorting('name')">Produto</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entradas</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              UNITS SOLD
              <span class="ml-1 cursor-pointer" title="Quantidade total de vendas (saídas) deste produto">
                <svg xmlns="http://www.w3.org/2000/svg" class="inline h-4 w-4 text-gray-400" fill="none"
                  viewBox="0 0 24 24" stroke="currentColor">
                  <title>Quantidade total de vendas (saídas) deste produto</title>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z" />
                </svg>
              </span>
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              PROFIT TOTAL
              <span class="ml-1 cursor-pointer"
                title="Lucro total = (Preço de venda - Preço fornecedor) x Unidades vendidas">
                <svg xmlns="http://www.w3.org/2000/svg" class="inline h-4 w-4 text-gray-400" fill="none"
                  viewBox="0 0 24 24" stroke="currentColor">
                  <title>Lucro total = (Preço de venda - Preço fornecedor) x Unidades vendidas</title>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z" />
                </svg>
              </span>
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valor Total
              Saídas
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lucro Real</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="(movements, productId) in inventoryStore.groupedMovements" :key="productId">
            <td class="px-6 py-4 whitespace-nowrap">{{ movements[0]?.productName }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{safeFormatNumber(movements.filter(m => m.type ===
              'ENTRADA').reduce((sum, m) => sum + m.quantity, 0), 0) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{safeFormatNumber(movements.filter(m => m.type ===
              'SAIDA').reduce((sum, m) => sum + m.quantity, 0), 0) }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              {{safeCurrency(movements.filter(m => m.type === 'ENTRADA').reduce((sum, m) => sum + (m.value *
                m.quantity),
              0)) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              {{safeCurrency(movements.filter(m => m.type === 'SAIDA').reduce((sum, m) => sum + (m.value * m.quantity),
              0)) }}
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

</template>