<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useProductStore } from '../../stores/productStore'
import { useInventoryStore } from '../../stores/inventoryStore'
import { getTypeDisplayName } from '../../utils/productTypes'
import { formatCurrency, formatNumber } from '../../utils/formatters'

const productStore = useProductStore()
const inventoryStore = useInventoryStore()

onMounted(async () => {
  await productStore.fetchProducts()
  await inventoryStore.fetchTransactions()
  await inventoryStore.fetchMovementsGroupedByProduct()
})

const simulacaoProdutoId = ref('')
const simulacaoQtd = ref(1)
const simulacaoPreco = ref(0)

watch(simulacaoProdutoId, (id) => {
  const produto = produtosLucroBackend.value.find(p => p.id === id)
  simulacaoPreco.value = produto ? produto.receitaTotal && produto.qtdVendida > 0 ? produto.receitaTotal / produto.qtdVendida : 0 : 0
})

const simulacaoResultado = computed(() => {
  const produto = produtosLucroBackend.value.find(p => p.id === simulacaoProdutoId.value)
  if (!produto || simulacaoQtd.value <= 0 || simulacaoPreco.value < 0) return null
  const receita = simulacaoPreco.value * simulacaoQtd.value
  const custoMedio = produto.qtdVendida > 0 ? (produto.receitaTotal - produto.lucroTotal) / produto.qtdVendida : 0
  const custoTotal = custoMedio * simulacaoQtd.value
  const lucro = receita - custoTotal
  const margem = custoTotal > 0 ? ((lucro / custoTotal) * 100).toFixed(1) : '0.0'
  return { receita, lucro, margem }
})

interface ProfitSubtotal {
  totalAvailable: number;
  totalSold: number;
  totalSalesValue: number;
  totalCost: number;
  totalProfit: number;
}

const subtotalByType = computed<Record<string, ProfitSubtotal & { margin: string }>>(() => {
  const result: Record<string, ProfitSubtotal & { margin: string }> = {}
  const produtos = produtosLucroBackend.value
  const grouped = produtos.reduce<Record<string, typeof produtos>>((acc, p) => {
    if (!acc[p.tipo]) acc[p.tipo] = []
    acc[p.tipo].push(p)
    return acc
  }, {})
  Object.entries(grouped).forEach(([type, products]) => {
    let totalAvailable = products.reduce((sum, p) => sum + (p.qtdDisponivel ?? 0), 0)
    let totalSold = products.reduce((sum, p) => sum + (p.qtdVendida ?? 0), 0)
    let totalSalesValue = products.reduce((sum, p) => sum + (p.receitaTotal ?? 0), 0)
    let totalCost = products.reduce((sum, p) => sum + (p.custoTotal ?? 0), 0)
    let totalProfit = products.reduce((sum, p) => sum + (p.lucroTotal ?? 0), 0)

    totalAvailable = Math.max(totalAvailable, 0)
    totalSold = Math.max(totalSold, 0)
    totalSalesValue = Math.max(totalSalesValue, 0)
    totalCost = Math.max(totalCost, 0)
    totalProfit = Math.max(totalProfit, 0)

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
  precoMedioVenda: number;
  custoTotal: number;
  lucroUnitario: number;
  lucroTotal: number;
  margem: string;
}


const produtosLucroBackend = computed<ProdutoLucro[]>(() => {
  const produtos = productStore.products
  const transacoes: MovimentoEstoque[] = inventoryStore.transactions as any[]

  if (!produtos.length || !transacoes.length) return []

  return produtos.map(produto => {
    const id = produto.id
    const nome = produto.code || produto.name || produto.description || '—'
    const tipo = produto.type || '—'
    const supplierValue = produto.supplierPrice || 0

    const entradas = transacoes.filter(t => String(t.productId) === String(id) && (t.type === 'input'))
    const saidas = transacoes.filter(t => String(t.productId) === String(id) && (t.type === 'output'))

    const qtdEntradas = entradas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const qtdSaidas = saidas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const qtdDisponivel = qtdEntradas - qtdSaidas
    const qtdVendida = qtdSaidas

    const receitaTotal = saidas.reduce((sum, t) => sum + ((t.value || 0) * (t.quantity || 0)), 0)
    const totalPagoEntradas = entradas.reduce((sum, t) => sum + ((t.value || supplierValue) * (t.quantity || 0)), 0)
    const totalUnidadesEntradas = entradas.reduce((sum, t) => sum + (t.quantity || 0), 0)
    const custoMedio = totalUnidadesEntradas > 0 ? totalPagoEntradas / totalUnidadesEntradas : supplierValue
    const custoTotalVendido = custoMedio * qtdVendida
    const lucroTotal = receitaTotal - custoTotalVendido
    const margem = custoTotalVendido > 0 ? ((lucroTotal / custoTotalVendido) * 100).toFixed(1) : '0.0'

    const precoMedioVenda = qtdVendida > 0 ? receitaTotal / qtdVendida : 0
    return {
      id: String(id),
      nome,
      tipo,
      qtdDisponivel,
      qtdVendida,
      receitaTotal,
      precoMedioVenda,
      custoTotal: custoTotalVendido,
      lucroUnitario: qtdVendida > 0 ? lucroTotal / qtdVendida : 0,
      lucroTotal,
      margem
    }
  })
})
const inventoryByType = computed(() => {
  return inventoryStore.getInventoryStatsByType
})
defineExpose({ inventoryByType })

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
    
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-10">

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
      
        <div class="bg-white rounded-xl shadow border p-6 mb-10">
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
              </tr>
            </thead>
            <tbody>
              <tr v-for="(subtotal, type) in subtotalByType" :key="type" class="hover:bg-gray-50">
                <td class="px-4 py-2 font-semibold text-gray-700">{{ getTypeDisplayName(type) }}</td>
                <td class="px-4 py-2 text-right">{{ safeFormatNumber(subtotal.totalAvailable, 0) }}</td>
                <td class="px-4 py-2 text-right">{{ safeFormatNumber(totalSold, 0) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(totalSalesValue) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(totalCost) }}</td>
                <td class="px-4 py-2 text-right">{{ safeCurrency(totalProfit) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
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
    </div>
  </div>

</template>