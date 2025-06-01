package desafio.nexdom.desafio.dto;

import desafio.nexdom.desafio.model.MovementType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockMovementRequest {
    @NotNull(message = "O ID do produto é obrigatório")
    private Long productId;
    
    @NotNull(message = "O tipo de movimentação é obrigatório")
    private MovementType movementType;
    
    @NotNull(message = "O valor de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor de venda deve ser maior que zero")
    private BigDecimal saleValue;
    
    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    private Integer quantity;
    
    private String description;
}
