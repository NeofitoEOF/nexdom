package desafio.nexdom.desafio.dto;

import desafio.nexdom.desafio.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;


@Data
public class ProductResponse {
    @Schema(description = "ID do produto", example = "1")
    private Long id;
    
    @Schema(description = "Código único do produto", example = "PROD-001")
    private String code;
    
    @Schema(description = "Descrição do produto", example = "Smartphone Galaxy S21")
    private String description;
    
    @Schema(description = "Tipo/categoria do produto", example = "ELETRÔNICO")
    private String type;
    
    @Schema(description = "Valor de compra do fornecedor", example = "1500.00")
    private BigDecimal supplierValue;
    
    @Schema(description = "Quantidade atual em estoque", example = "10")
    private Integer stockQuantity;
 
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCode(product.getCode());
        response.setDescription(product.getDescription());
        response.setType(product.getType());
        response.setSupplierValue(product.getSupplierValue());
        response.setStockQuantity(product.getStockQuantity());
        return response;
    }
}
