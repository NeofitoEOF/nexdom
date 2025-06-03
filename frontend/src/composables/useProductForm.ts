import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore } from '../stores/productStore'
import { PRODUCT_TYPES, getTypeDisplayName } from '../utils/productTypes'

interface ProductFormState {
  code: string;
  description: string;
  type: string;
  supplierValue: number;
  stockQuantity: number;
}

export interface ProductFormErrors {
  code?: string
  description?: string
  type?: string
  supplierValue?: string
  stockQuantity?: string
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
    code: '',
    description: '',
    type: '',
    supplierValue: 0,
    stockQuantity: 0
  })

  const resetForm = () => {
    form.value = {
      code: '',
      description: '',
      type: '',
      supplierValue: 0,
      stockQuantity: 0
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
          code: product.code || '',
          description: product.description || '',
          type: product.type || '',
          supplierValue: product.supplierPrice || 0,
          stockQuantity: product.stock || 0
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

    if (!form.value.code.trim()) {
      newErrors.code = 'Código é obrigatório'
    }

    if (!form.value.description.trim()) {
      newErrors.description = 'Descrição é obrigatória'
    }

    if (!form.value.type) {
      newErrors.type = 'Tipo é obrigatório'
    }

    if (form.value.supplierValue <= 0) {
      newErrors.supplierValue = 'Preço de custo deve ser maior que zero'
    }

    if (!isEdit.value && form.value.stockQuantity < 0) {
      newErrors.stockQuantity = 'Estoque inicial não pode ser negativo'
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
      const formData = {
        code: form.value.code,
        description: form.value.description,
        type: form.value.type,
        supplierValue: Number(form.value.supplierValue),
        stockQuantity: Number(form.value.stockQuantity)
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
