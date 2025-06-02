export interface ApiGroupedMovement {
  productId: string
  productName?: string
  quantity: number
  movementType: 'ENTRADA' | 'SAIDA'
  value: number
  date: string
  description?: string
}

export interface UIGroupedMovement {
  productId: string
  productName?: string
  quantity: number
  type: 'ENTRADA' | 'SAIDA'
  value: number
  date: string
  description?: string
}
