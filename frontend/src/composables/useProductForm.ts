import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore } from '../stores/productStore'
import { PRODUCT_TYPES, getTypeDisplayName } from '../utils/productTypes'

interface ProductFormState {
  name: string
  description: string
  type: string
  supplierPrice: number
  stock: number
}

export interface ProductFormErrors {
  name?: string
  description?: string
  type?: string
  supplierPrice?: string
  stock?: string
  submit?: string
}

export function useProductForm() {
  const router = useRouter()
  const route = useRoute()
  const productStore = useProductStore()

  const isEdit = computed(() => !!route.params.id)
  const productId = route.params.id as string
  const loading = ref(false)

  const form = ref<ProductFormState>({
    name: '',
    description: '',
    type: '',
    supplierPrice: 0,
    stock: 0
  })

  const resetForm = () => {
    form.value = {
      name: '',
      description: '',
      type: '',
      supplierPrice: 0,
      stock: 0
    }
    errors.value = {}
    formSubmitted.value = false
  }

  const productTypes = PRODUCT_TYPES

  const errors = ref<ProductFormErrors>({})
  const formSubmitted = ref(false)

  const originalProduct = ref<any>(null)

  const loadProduct = async () => {
    if (!isEdit.value) return

    try {
      const product = await productStore.getProductById(productId)
      if (product) {
        originalProduct.value = { ...product }
        form.value = {
          name: product.name,
          description: product.description,
          type: product.type,
          supplierPrice: product.supplierPrice,
          stock: product.stock
        }
      }
    } catch (error) {
      console.error('Error loading product:', error)
      router.push('/products')
    }
  }

  onMounted(async () => {
    await loadProduct()
  })

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!form.value.name.trim()) {
      newErrors.name = 'Nome é obrigatório'
    }

    if (!form.value.description.trim()) {
      newErrors.description = 'Descrição é obrigatória'
    }

    if (!form.value.type) {
      newErrors.type = 'Tipo é obrigatório'
    }

    if (form.value.supplierPrice <= 0) {
      newErrors.supplierPrice = 'Preço de custo deve ser maior que zero'
    }

    if (!isEdit.value && form.value.stock < 0) {
      newErrors.stock = 'Estoque inicial não pode ser negativo'
    }

    errors.value = newErrors
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: Event) => {
    e.preventDefault()
    formSubmitted.value = true

    if (!validateForm()) {
      return
    }

    loading.value = true

    try {
      let formData: any

      if (isEdit.value && originalProduct.value) {
        formData = {
          ...originalProduct.value,
          ...form.value
        }
      } else {
        formData = {
          ...form.value,
          sellingPrice: 0 // Valor padrão para o backend em novos produtos
        }
      }

      if (isEdit.value) {
        await productStore.updateProduct(productId, formData)
      } else {
        await productStore.addProduct(formData)
      }

      router.push('/products')
    } catch (error) {
      errors.value.submit = error instanceof Error ? error.message : 'Erro ao salvar produto'
    } finally {
      loading.value = false
    }
  }

  return {
    form,
    errors,
    loading,
    formSubmitted,
    isEdit,
    handleSubmit,
    validateForm,
    resetForm,
    productTypes,
    getTypeDisplayName
  }
}
