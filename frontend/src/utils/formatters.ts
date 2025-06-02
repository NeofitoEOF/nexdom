/**
 * Formata um valor numérico para o formato de moeda brasileira (BRL)
 * @param value - O valor numérico a ser formatado
 * @returns Uma string formatada como moeda brasileira
 */
export function formatCurrency(value: number | undefined | null): string {
  // Verifica se o valor é undefined, null ou NaN e retorna um valor padrão
  if (value === undefined || value === null || isNaN(value)) {
    return 'R$ 0,00';
  }
  
  return value.toLocaleString('pt-BR', { 
    style: 'currency', 
    currency: 'BRL' 
  });
}

/**
 * Formata uma data para o formato brasileiro (DD/MM/YYYY)
 * @param date - A data a ser formatada
 * @returns Uma string formatada como data brasileira
 */
export function formatDate(date: Date | undefined | null): string {
  // Verifica se a data é undefined ou null e retorna um valor padrão
  if (!date) {
    return '--/--/----';
  }
  
  try {
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  } catch (error) {
    console.error('Erro ao formatar data:', error);
    return '--/--/----';
  }
}

/**
 * Formata um valor numérico com o número especificado de casas decimais
 * @param value - O valor numérico a ser formatado
 * @param decimals - O número de casas decimais (padrão: 2)
 * @returns Uma string formatada com o número especificado de casas decimais
 */
export function formatNumber(value: number | undefined | null, decimals: number = 2): string {
  // Verifica se o valor é undefined, null ou NaN e retorna um valor padrão
  if (value === undefined || value === null || isNaN(value)) {
    return '0' + (decimals > 0 ? ',' + '0'.repeat(decimals) : '');
  }
  
  return value.toLocaleString('pt-BR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  });
}
