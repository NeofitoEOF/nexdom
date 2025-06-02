export interface InventoryTransaction {
  id: string;
  productId: string;
  type: 'input' | 'output';
  quantity: number;
  value?: number;
  date: Date;
  notes?: string;
}

export interface InventoryStats {
  totalAvailable: number;
  totalSold: number;
  totalProfit: number;
}

export type InventoryStatsByType = Record<string, InventoryStats>;
