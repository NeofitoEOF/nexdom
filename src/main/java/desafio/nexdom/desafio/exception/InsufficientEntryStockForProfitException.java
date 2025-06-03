package desafio.nexdom.desafio.exception;

public class InsufficientEntryStockForProfitException extends RuntimeException {
    public InsufficientEntryStockForProfitException(Long productId, int sold, int available) {
        super("[ERRO DE LUCRO] Não é possível calcular o lucro do produto (ID: " + productId + ") porque a quantidade vendida excede o total de entradas registradas. " +
              "Quantidade vendida: " + sold + ", quantidade disponível em entradas: " + available + ". " +
              "Verifique se há lançamentos de compras suficientes para cobrir todas as vendas deste produto antes de calcular o lucro.");
    }
}
