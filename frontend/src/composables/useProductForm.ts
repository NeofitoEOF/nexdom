import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore } from '../stores/productStore'
import { PRODUCT_TYPES, getTypeDisplayName } from '../utils/productTypes'

interface ProductFormState {
  name: string
  description: string
  type: string
  supplierPrice: number
  sellingPrice: number
  stock: number
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
    sellingPrice: 0,
    stock: 0
  })

  const resetForm = () => {
    form.value = {
      name: '',
      description: '',
      type: '',
      supplierPrice: 0,
      sellingPrice: 0,
      stock: 0
    }
    errors.value = {}
    formSubmitted.value = false
  }

  const productTypes = PRODUCT_TYPES

  const errors = ref<Record<string, string>>({})
  const formSubmitted = ref(false)

  const loadProduct = async () => {
    if (!isEdit.value) return

    try {
      const product = await productStore.getProductById(productId)
      if (product) {
        form.value = {
          name: product.name,
          description: product.description,
          type: product.type,
          supplierPrice: product.supplierPrice,
          sellingPrice: product.sellingPrice,
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

    if (form.value.sellingPrice <= 0) {
      newErrors.sellingPrice = 'Preço de venda deve ser maior que zero'
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
      const firstErrorElement = document.querySelector('.error-message')
      if (firstErrorElement) {
        firstErrorElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
      return
    }

    loading.value = true

    try {
      if (isEdit.value) {
        await productStore.updateProduct(productId, form.value)
      } else {
        await productStore.addProduct(form.value)
      }

      router.push('/products')
    } catch (error) {
      console.error('Error saving product:', error)
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
