import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { useProductStore } from './productStore'
import { InventoryTransaction, ProductWithProfit } from '../interfaces'
import { ApiGroupedMovement, UIGroupedMovement } from '../interfaces'
import { stockMovementsAPI } from '../services/api'

export const useInventoryStore = defineStore('inventory', () => {
  const transactions = ref<InventoryTransaction[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const productStore = useProductStore()

  async function fetchTransactions() {
    isLoading.value = true
    error.value = null
    try {
      const response = await stockMovementsAPI.getAll()
      const data = response.data as any
      
      if (data && Array.isArray(data)) {
        transactions.value = data.map((item: any) => ({
          id: String(item.id),
          productId: String(item.productId),
          type: item.movementType === 'ENTRADA' ? 'input' : 'output',
          quantity: item.quantity,
          value: item.saleValue || item.value,
          date: new Date(item.createdAt || new Date()),
          notes: item.description || item.notes
        }))
      }
    } catch (err: any) {
      let errorMessage = 'Erro ao carregar transações'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha ao carregar dados'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage

    } finally {
      isLoading.value = false
    }
  }

  async function addTransaction(transaction: any) {
    isLoading.value = true
    error.value = null

    try {
      if (transaction.type === 'output') {
        const stockResult = productStore.updateStock(transaction.productId, -transaction.quantity)
        if (!stockResult.success) {
          error.value = stockResult.message || 'Erro ao validar estoque'
          return { success: false, message: error.value }
        }
      } else {
        productStore.updateStock(transaction.productId, transaction.quantity)
      }

      const newTransaction: InventoryTransaction = {
        id: uuidv4(),
        productId: transaction.productId,
        type: transaction.type,
        quantity: transaction.quantity,
        value: transaction.value,
        notes: transaction.notes,
        date: new Date()
      }

      transactions.value.push(newTransaction)

      try {
        const response = await stockMovementsAPI.create(newTransaction)
        if (response.data && response.data.id) {
          const index = transactions.value.findIndex(t => t.id === newTransaction.id)
          if (index !== -1) {
            transactions.value[index].id = response.data.id
          }
        }
      } catch (apiError: any) {

      }

      return { success: true }
    } catch (err: any) {
      let errorMessage = 'Erro ao adicionar transação'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha ao adicionar'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      return { success: false, message: errorMessage }
    } finally {
      isLoading.value = false
    }
  }

  async function updateTransaction(
    id: string,
    data: Partial<InventoryTransaction>
  ): Promise<{ success: boolean; message?: string; transaction?: InventoryTransaction }> {
    isLoading.value = true
    error.value = null
    try {
      const response = await stockMovementsAPI.update(id, data)
      
      const index = transactions.value.findIndex(t => t.id === id)
      if (index !== -1) {
        transactions.value[index] = { ...transactions.value[index], ...data }
      }
      
      return { success: true, transaction: response.data as InventoryTransaction }
    } catch (err: any) {
      let errorMessage = 'Erro ao atualizar transação'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha na atualização'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      return { success: false, message: errorMessage }
    } finally {
      isLoading.value = false
    }
  }

  async function deleteTransaction(id: string): Promise<{ success: boolean; message?: string }> {
    isLoading.value = true
    error.value = null
    try {
      await stockMovementsAPI.delete(id)
      const index = transactions.value.findIndex(t => t.id === id)
      if (index !== -1) {
        transactions.value.splice(index, 1)
      }
      return { success: true }
    } catch (err: any) {
      let errorMessage = 'Erro ao remover transação'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha na remoção'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      return { success: false, message: errorMessage }
    } finally {
      isLoading.value = false
    }
  }

  const transactionsWithProductInfo = computed(() => {
    const productStore = useProductStore()
    return transactions.value.map(transaction => {
      const product = productStore.products.find(p => String(p.id) === String(transaction.productId))
      return {
        ...transaction,
        productName: product?.name || 'Produto Desconhecido',
        productType: product?.type || 'Tipo Desconhecido'
      }
    }).sort((a, b) => b.date.getTime() - a.date.getTime())
  })

  const getProductsWithProfitData = computed(() => {
    const allProducts = productStore.products
    const result: ProductWithProfit[] = []

    allProducts.forEach(product => {
      const productTransactions = transactions.value.filter(
        t => String(t.productId) === String(product.id)
      )

      const totalSold = productTransactions
        .filter(t => t.type === 'output')
        .reduce((sum, t) => sum + t.quantity, 0)

      const totalSalesValue = productTransactions
        .filter(t => t.type === 'output')
        .reduce((sum, t) => sum + (t.value || 0), 0)

      const totalProfit = totalSalesValue > 0
        ? totalSalesValue - (totalSold * product.supplierPrice)
        : totalSold * (product.sellingPrice - product.supplierPrice)

      result.push({
        ...product,
        totalSold,
        totalSalesValue,
        totalProfit
      })
    })

    return result
  })

  const getTransactionsByProduct = computed(() => {
    return (productId: string) => transactions.value.filter(t => t.productId === productId)
  })

  const getInventoryStatsByType = computed(() => {
    const productTypes = productStore.getProductTypes
    const result: Record<string, { totalAvailable: number, totalSold: number, totalProfit: number }> = {}
    
    for (const type of productTypes) {
      const productsOfType = productStore.products.filter(p => p.type === type)
      const productIds = productsOfType.map(p => p.id)
      const totalAvailable = productsOfType.reduce((sum, p) => sum + p.stock, 0)
      
      const outTransactions = transactions.value.filter(
        t => productIds.includes(t.productId) && t.type === 'output'
      )
      const totalSold = outTransactions.reduce((sum, t) => sum + t.quantity, 0)
      
      let totalProfit = 0
      outTransactions.forEach(transaction => {
        const product = productStore.getProductById(transaction.productId)
        if (product) {
          const profit = transaction.quantity * (product.sellingPrice - product.supplierPrice)
          totalProfit += profit
        }
      })
      
      result[type] = { totalAvailable, totalSold, totalProfit }
    }
    
    return result
  })

  async function initialize() {
    await fetchTransactions()
  }


  const groupedMovements = ref<Record<string, UIGroupedMovement[]>>({})
  function mapApiMovementsToUI(apiMovements: Record<string, ApiGroupedMovement[]>): Record<string, UIGroupedMovement[]> {
    const result: Record<string, UIGroupedMovement[]> = {}
    
    for (const [productId, movements] of Object.entries(apiMovements)) {
      result[productId] = movements.map(movement => ({
        productId: movement.productId,
        productName: movement.productName,
        quantity: movement.quantity,
        // Mapeia movementType para type
        type: movement.movementType,
        value: movement.value,
        date: movement.date,
        description: movement.description
      }))
    }
    
    return result
  }
  
  async function fetchMovementsGroupedByProduct() {
    isLoading.value = true
    error.value = null
    try {
      const response = await stockMovementsAPI.getGroupedByProduct()
      // Primeiro convertemos para unknown para evitar erros de tipagem
      const apiData = response.data as unknown as Record<string, ApiGroupedMovement[]>
      groupedMovements.value = mapApiMovementsToUI(apiData)
    } catch (err: any) {
      let errorMessage = 'Erro ao buscar movimentos agrupados'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha ao buscar dados'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      groupedMovements.value = {}
    } finally {
      isLoading.value = false
    }
  }

  return {
    transactions,
    isLoading,
    error,
    getTransactionsByProduct,
    getProductsWithProfitData,
    getInventoryStatsByType,
    fetchTransactions,
    addTransaction,
    updateTransaction,
    deleteTransaction,

    initialize,
    groupedMovements,
    fetchMovementsGroupedByProduct,
    transactionsWithProductInfo
  }
})