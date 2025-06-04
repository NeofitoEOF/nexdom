export interface Product {
  id: string
  name: string
  code?: string
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
  sellingPrice?: number;
  stock: number;
}

export interface ProductUpdatePayload extends Partial<ProductCreatePayload> {}

export interface ProductWithProfit extends Product {
  totalSold: number;
  
  totalSalesValue: number;
  
  totalCost: number;
  
  totalProfit: number;
  
  averageSellingPrice: number;
  
  unitProfit: number;
  
  profitMarginPercent: number;
  
  roi: number;
  
  currentStockValue: number;
  
  potentialProfit: number;
}

export interface ProductSearchFilters {
  name?: string;
  type?: string;
  minStock?: number;
  maxStock?: number;
}
