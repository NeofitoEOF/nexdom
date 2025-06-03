import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { useProductStore } from './productStore'
import { InventoryTransaction, ProductWithProfit } from '../interfaces'
import { ApiGroupedMovement, UIGroupedMovement } from '../interfaces'
import { stockMovementsAPI } from '../services/api'
import { mapTransactionFromBackend } from '../services/adapters'

export const useInventoryStore = defineStore('inventory', () => {
  const transactions = ref<InventoryTransaction[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const productStore = useProductStore()

  /**
   * Busca todas as transações de estoque do backend
   * @returns Promise<boolean> - Indica se a operação foi bem-sucedida
   */
  async function fetchTransactions(): Promise<boolean> {
    isLoading.value = true
    error.value = null
    try {
      console.log('Buscando transações do servidor...')
      const response = await stockMovementsAPI.getAll()
      const data = response.data as any
      
      if (data && Array.isArray(data)) {
        console.log(`Encontradas ${data.length} transações`)
        transactions.value = data.map((item: any) => mapTransactionFromBackend(item))
        return true
      } else {
        console.warn('Resposta do servidor não contém um array de transações:', data)
        transactions.value = []
        return false
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
      
      console.error('Erro ao buscar transações:', errorMessage)
      error.value = errorMessage
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Adiciona uma nova transação de estoque
   * @param transaction - Dados da transação a ser adicionada
   * @returns Objeto com status de sucesso e mensagem
   */
  async function addTransaction(transaction: any) {
    isLoading.value = true
    error.value = null

    try {
      // Validação de estoque para saídas
      if (transaction.type === 'output') {
        const stockResult = productStore.updateStock(transaction.productId, -transaction.quantity)
        if (!stockResult.success) {
          error.value = stockResult.message || 'Erro ao validar estoque'
          return { success: false, message: error.value }
        }
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

      try {
        const response = await stockMovementsAPI.create(newTransaction)
        
        if (response.data && response.data.id) {
          newTransaction.id = response.data.id
          
          transactions.value.push(newTransaction)
          
          if (transaction.type === 'input') {
            productStore.updateStock(transaction.productId, transaction.quantity)
          }
          
          return { success: true }
        } else {
          throw new Error('Resposta do servidor não contém ID da transação')
        }
      } catch (apiError: any) {
        console.error('Erro na API ao criar transação:', apiError)
        let errorMessage = 'Erro ao salvar transação no servidor'
        
        if (apiError.response) {
          errorMessage = `Erro ${apiError.response.status}: ${apiError.response.data?.message || 'Falha ao salvar no servidor'}`
        } else if (apiError.request) {
          errorMessage = 'Servidor não respondeu à requisição'
        } else {
          errorMessage = apiError.message || errorMessage
        }
        
        error.value = errorMessage
        return { success: false, message: errorMessage }
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
      
      const outputTransactions = productTransactions.filter(t => t.type === 'output')
      
      const totalSold = outputTransactions.reduce((sum, t) => sum + t.quantity, 0)
      
      const totalSalesValue = outputTransactions.reduce((sum, t) => sum + (t.value || 0) * t.quantity, 0)
      
      const totalCost = totalSold * product.supplierPrice
      
      const totalProfit = totalSalesValue - totalCost
      
      const averageSellingPrice = totalSold > 0 ? totalSalesValue / totalSold : product.sellingPrice
      
      const unitProfit = averageSellingPrice - product.supplierPrice
      
      const profitMarginPercent = product.supplierPrice > 0 
        ? (unitProfit / product.supplierPrice) * 100 
        : 0
      
      const roi = totalCost > 0 
        ? (totalProfit / totalCost) * 100 
        : 0
      
      const currentStockValue = product.stock * product.supplierPrice
      
      const potentialProfit = product.stock * unitProfit
      
      result.push({
        ...product,
        totalSold,
        totalSalesValue,
        totalCost,
        totalProfit,
        averageSellingPrice,
        unitProfit,
        profitMarginPercent,
        roi,
        currentStockValue,
        potentialProfit
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

 
  async function initialize(): Promise<void> {
    console.log('Inicializando inventoryStore...')
    const success = await fetchTransactions()
    
    if (!success) {
      console.warn('Falha ao inicializar o inventoryStore. Tentando novamente em 3 segundos...')
      setTimeout(() => {
        fetchTransactions()
      }, 3000)
    }
  }


  const groupedMovements = ref<Record<string, UIGroupedMovement[]>>({})
  function mapApiMovementsToUI(apiMovements: Record<string, ApiGroupedMovement[]>): Record<string, UIGroupedMovement[]> {
    const result: Record<string, UIGroupedMovement[]> = {}
    
    for (const [productId, movements] of Object.entries(apiMovements)) {
      result[productId] = movements.map(movement => ({
        productId: movement.productId,
        productName: movement.productName,
        quantity: movement.quantity,
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