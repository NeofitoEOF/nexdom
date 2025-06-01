package desafio.nexdom.desafio.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(Long id) {
        super(String.format("Produto com ID %d não encontrado", id));
    }
}
