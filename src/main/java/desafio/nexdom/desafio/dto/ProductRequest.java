package desafio.nexdom.desafio.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;


@Data
public class ProductRequest {
    @NotNull(message = "O código do produto é obrigatório")
    @Schema(description = "Código único do produto", example = "PROD-001")
    private String code;
    
    @NotBlank(message = "A descrição do produto é obrigatória")
    @Schema(description = "Descrição do produto", example = "Smartphone Galaxy S21")
    private String description;
    
    @NotNull(message = "O tipo do produto é obrigatório")
    @Schema(description = "Tipo/categoria do produto", example = "ELETRÔNICO")
    private String type;
    
    @NotNull(message = "O valor de fornecedor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor de fornecedor deve ser maior que zero")
    @Schema(description = "Valor de compra do fornecedor", example = "1500.00")
    private BigDecimal supplierValue;
    
    @NotNull(message = "A quantidade em estoque é obrigatória")
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    @Schema(description = "Quantidade inicial em estoque", example = "10")
    private Integer stockQuantity;
}
