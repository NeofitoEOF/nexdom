import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Product, ProductSearchFilters } from '../interfaces'
import { productsAPI } from '../services/api'
import { mapProductToBackend } from '../services/adapters'

export const useProductStore = defineStore('products', () => {
  const products = ref<Product[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const getProductById = computed(() => {
    return (id: string): Product | undefined => products.value.find(p => String(p.id) === String(id))
  })

  const getFilteredProducts = computed(() => {
    return (filters: ProductSearchFilters): Product[] => {
      return products.value.filter(product => {
        if (filters.name && !product.name.toLowerCase().includes(filters.name.toLowerCase())) {
          return false
        }
        
        if (filters.type && product.type !== filters.type) {
          return false
        }
        
        if (filters.minStock !== undefined && product.stock < filters.minStock) {
          return false
        }
        
        if (filters.maxStock !== undefined && product.stock > filters.maxStock) {
          return false
        }
        
        return true
      })
    }
  })

  const getProductTypes = computed((): string[] => {
    const types = new Set<string>()
    products.value.forEach(product => {
      types.add(product.type)
    })
    return Array.from(types)
  })

  async function fetchProducts(): Promise<Product[]> {
    isLoading.value = true
    error.value = null
    try {
      const response = await productsAPI.getAll()
      const data = response.data as any[]
      products.value = data.map((item: any) => ({
        id: String(item.id),
        code: item.code || '',
        name: item.name || item.description || '',
        description: item.description || '',
        type: item.type || '',
        supplierPrice: parseFloat(item.supplierValue) || 0,
        sellingPrice: parseFloat(item.sellingPrice || item.saleValue) || 0,
        stock: parseInt(item.stockQuantity) || 0,
        createdAt: item.createdAt ? new Date(item.createdAt).toISOString() : new Date().toISOString(),
        updatedAt: item.updatedAt ? new Date(item.updatedAt).toISOString() : new Date().toISOString()
      }))
      return products.value
    } catch (err: any) {
      const errorMessage = err.message || 'Erro ao buscar produtos'
      error.value = errorMessage
      throw new Error(errorMessage)
    } finally {
      isLoading.value = false
    }
  }

  async function addProduct(productData: any): Promise<Product | null> {
    isLoading.value = true
    error.value = null
    try {
      const payload = mapProductToBackend(productData)
      const response = await productsAPI.create(payload)
      const newProduct = response.data as Product
      products.value.push(newProduct)
      return newProduct
    } catch (err: any) {
      let errorMessage = 'Falha ao criar produto'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha na requisição'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function updateProduct(id: string, data: Partial<Omit<Product, 'id' | 'createdAt' | 'updatedAt'>>): Promise<Product | null> {
    isLoading.value = true
    error.value = null
    try {
      const payload = mapProductToBackend(data)
      const response = await productsAPI.update(id, payload)
      const updatedProduct = response.data as Product
      const index = products.value.findIndex(p => p.id === id)
      if (index !== -1) {
        products.value[index] = updatedProduct
      }
      return updatedProduct
    } catch (err: any) {
      let errorMessage = 'Falha ao atualizar produto'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha na atualização'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function removeProduct(id: string): Promise<boolean> {
    isLoading.value = true
    error.value = null
    try {
      await productsAPI.delete(id)
      const index = products.value.findIndex(p => p.id === id)
      if (index !== -1) {
        products.value.splice(index, 1)
      }
      return true
    } catch (err: any) {
      let errorMessage = 'Falha ao remover produto'
      
      if (err.response) {
        errorMessage = `Erro ${err.response.status}: ${err.response.data?.message || 'Falha na remoção'}`

      } else if (err.request) {
        errorMessage = 'Servidor não respondeu à requisição'

      } else {
        errorMessage = err.message || errorMessage

      }
      
      error.value = errorMessage
      return false
    } finally {
      isLoading.value = false
    }
  }

  function updateStock(id: string, quantity: number): { success: boolean; message?: string } {
    const product = products.value.find(p => String(p.id) === String(id))
    
    if (!product) {
      return { success: false, message: 'Produto não encontrado' }
    }
    
    if (quantity < 0 && product.stock + quantity < 0) {
      return { 
        success: false, 
        message: `Estoque insuficiente. Disponível: ${product.stock} unidades`
      }
    }
    
    product.stock += quantity
    product.updatedAt = new Date().toISOString()
    
    return { success: true }
  }


  return {
    products,
    isLoading,
    error,
    getProductById,
    getFilteredProducts,
    getProductTypes,
    fetchProducts,
    addProduct,
    updateProduct,
    removeProduct,
    updateStock,
  }
})