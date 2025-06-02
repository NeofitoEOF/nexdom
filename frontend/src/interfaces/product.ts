export interface Product {
  id: string
  name: string
  description: string
  type: string
  supplierPrice: number
  sellingPrice: number
  stock: number
  createdAt: string
  updatedAt: string
}

export interface ProductCreatePayload {
  code: string;
  description: string;
  type: string;
  name: string;
  supplierPrice: number;
  sellingPrice: number;
  stock: number;
}

export interface ProductUpdatePayload extends Partial<ProductCreatePayload> {}

export interface ProductWithProfit extends Product {
  totalSold: number;
  totalSalesValue?: number;
  totalProfit: number;
}

export interface ProductSearchFilters {
  name?: string;
  type?: string;
  minStock?: number;
  maxStock?: number;
}
