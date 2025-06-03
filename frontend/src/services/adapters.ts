/**
 * Adaptadores para conversão de dados entre o frontend e o backend
 * Garantem que os objetos sejam convertidos para o formato esperado por cada camada
 */

import { InventoryTransaction } from '../interfaces';

/**
 * Converte uma transação do frontend para o formato esperado pelo backend
 * @param transaction - Transação no formato do frontend
 * @returns Objeto no formato esperado pelo backend
 */
export function mapTransactionToBackend(transaction: Partial<InventoryTransaction>) {
  // Converte o productId para número (Long no backend)
  const productId = transaction.productId ? Number(transaction.productId) : null;
  
  // Define o saleValue com base no tipo de movimentação
  // Para entradas, usamos 0.01 como valor mínimo (backend exige > 0)
  // Para saídas, usamos o valor informado ou 0.01 como fallback
  const saleValue = transaction.type === 'input' 
    ? 0.01 
    : (transaction.value && transaction.value > 0 ? transaction.value : 0.01);
  
  return {
    productId: productId,
    movementType: transaction.type === 'input' ? 'ENTRADA' : 'SAIDA',
    quantity: transaction.quantity,
    saleValue: saleValue,
    description: transaction.notes || ''
  };
}

/**
 * Converte uma transação do backend para o formato esperado pelo frontend
 */
export function mapTransactionFromBackend(backendTransaction: any): InventoryTransaction {
  return {
    id: String(backendTransaction.id),
    productId: String(backendTransaction.product?.id || backendTransaction.productId),
    type: backendTransaction.movementType === 'ENTRADA' ? 'input' : 'output',
    quantity: backendTransaction.quantity,
    value: backendTransaction.saleValue || 0,
    date: new Date(backendTransaction.movementDate || backendTransaction.date),
    notes: backendTransaction.description || ''
  };
}

/**
 * Converte um produto do frontend para o formato esperado pelo backend
 */
export function mapProductToBackend(product: any) {
  return {
    name: product.name,
    description: product.description,
    type: product.type,
    supplierPrice: product.supplierPrice,
    sellingPrice: product.sellingPrice || 0,
    stock: product.stock
  };
}

/**
 * Converte um produto do backend para o formato esperado pelo frontend
 */
export function mapProductFromBackend(backendProduct: any) {
  return {
    id: String(backendProduct.id),
    name: backendProduct.name,
    description: backendProduct.description,
    type: backendProduct.type,
    supplierPrice: backendProduct.supplierPrice,
    sellingPrice: backendProduct.sellingPrice || 0,
    stock: backendProduct.stock
  };
}
