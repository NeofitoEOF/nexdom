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

/**
 * Interface estendida de Product com informações detalhadas de lucro e performance
 */
export interface ProductWithProfit extends Product {
  /** Quantidade total vendida do produto */
  totalSold: number;
  
  /** Valor total das vendas */
  totalSalesValue: number;
  
  /** Custo total baseado no preço do fornecedor */
  totalCost: number;
  
  /** Lucro total (totalSalesValue - totalCost) */
  totalProfit: number;
  
  /** Preço médio de venda */
  averageSellingPrice: number;
  
  /** Lucro por unidade */
  unitProfit: number;
  
  /** Margem de lucro percentual */
  profitMarginPercent: number;
  
  /** Retorno sobre investimento (%) */
  roi: number;
  
  /** Valor do estoque atual baseado no preço do fornecedor */
  currentStockValue: number;
  
  /** Lucro potencial do estoque atual */
  potentialProfit: number;
}

export interface ProductSearchFilters {
  name?: string;
  type?: string;
  minStock?: number;
  maxStock?: number;
}
