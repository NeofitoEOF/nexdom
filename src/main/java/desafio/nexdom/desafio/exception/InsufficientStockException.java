package desafio.nexdom.desafio.exception;

public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String productCode, int available, int requested) {
        super(String.format("Estoque insuficiente para o produto %s. Disponível: %d, Solicitado: %d", 
                productCode, available, requested));
    }
}
