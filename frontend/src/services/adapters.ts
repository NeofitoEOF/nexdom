import { InventoryTransaction } from '../interfaces';

export function mapTransactionToBackend(transaction: Partial<InventoryTransaction>) {
  const productId = transaction.productId ? Number(transaction.productId) : null;
  
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
