export const PRODUCT_TYPES = [
  'Eletrônico',
  'Eletrodoméstico',
  'Móvel'
] as const;

export type ProductType = typeof PRODUCT_TYPES[number];

export function getTypeDisplayName(type: string): string {
  switch(type) {
    case 'Eletrônico': return 'Eletrônico'
    case 'Eletrodoméstico': return 'Eletrodoméstico'
    case 'Móvel': return 'Móvel'
    default: return type
  }
}
