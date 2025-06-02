import axios from 'axios';
import type { Product, InventoryTransaction } from '../interfaces';

type TransactionCreatePayload = Omit<InventoryTransaction, 'id'>;
type TransactionUpdatePayload = Partial<TransactionCreatePayload>;

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

export const productsAPI = {
  getAll: () => api.get<Product[]>('/products'),
  getById: (id: string) => api.get<Product>(`/products/${id}`),
  create: (data: any) => api.post<Product>('/products', {
    code: data.name,
    description: data.description,
    type: data.type,
    supplierValue: data.supplierPrice,
    stockQuantity: data.stock
  }),
  update: (id: string, data: any) => api.put<Product>(`/products/${id}`, {
    code: data.name,
    description: data.description,
    type: data.type,
    supplierValue: data.supplierPrice,
    stockQuantity: data.stock
  }),
  delete: (id: string) => api.delete(`/products/${id}`)
};

export const stockMovementsAPI = {
  getGroupedByProduct: () => api.get<Record<string, InventoryTransaction[]>>('/stock-movements/by-product'),
  getAll: () => api.get<InventoryTransaction[]>('/stock-movements'),
  getById: (id: string) => api.get<InventoryTransaction>(`/stock-movements/${id}`),
  create: (data: TransactionCreatePayload) => api.post<InventoryTransaction>('/stock-movements', data),
  update: (id: string, data: TransactionUpdatePayload) => api.put<InventoryTransaction>(`/stock-movements/${id}`, data),
  delete: (id: string) => api.delete(`/stock-movements/${id}`)
};

export default api;