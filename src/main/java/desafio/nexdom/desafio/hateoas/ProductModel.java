package desafio.nexdom.desafio.hateoas;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import desafio.nexdom.desafio.controller.ProductController;
import desafio.nexdom.desafio.controller.StockMovementController;
import desafio.nexdom.desafio.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id", "code", "description", "type", 
    "supplierValue", "stockQuantity", "_links", "_embedded"
})
@Schema(description = "Representação de um produto no sistema de estoque")
public class ProductModel extends BaseModel<ProductModel> {

    @Schema(description = "Identificador único do produto", example = "1")
    private Long id;

    @Schema(description = "Código único do produto", example = "ELEC-001", required = true)
    private String code;

    @Schema(description = "Descrição do produto", example = "Smartphone Samsung Galaxy S22", required = true)
    private String description;

    @Schema(description = "Tipo do produto", example = "ELECTRONIC", required = true)
    private String type;

    @Schema(description = "Valor de custo do fornecedor", example = "1500.00", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal supplierValue;

    @Schema(description = "Quantidade atual em estoque", example = "10")
    private Integer stockQuantity;
    
    public ProductModel() {
        super();
    }
    
    /**
     * Sets the content of the model.
     * 
     * @param content The content to set
     */
    public void setContent(Map<String, Object> content) {
        if (content != null) {
            this.id = (Long) content.get("id");
            this.code = (String) content.get("code");
            this.description = (String) content.get("description");
            this.type = (String) content.get("type");
            this.supplierValue = (BigDecimal) content.get("supplierValue");
            this.stockQuantity = (Integer) content.get("stockQuantity");
        }
    }

    /**
     * Cria um ProductModel a partir de uma entidade Product.
     * 
     * @param product A entidade Product a ser convertida
     * @return Um ProductModel com os dados do produto e links HATEOAS
     */
    public static ProductModel fromProduct(Product product) {
        if (product == null) {
            return null;
        }

        ProductModel model = new ProductModel();
        model.setId(product.getId());
        model.setCode(product.getCode());
        model.setDescription(product.getDescription());
        model.setType(product.getType());
        model.setSupplierValue(product.getSupplierValue());
        model.setStockQuantity(product.getStockQuantity());
        
    
        model.addProductLinks(product.getId());
        
        return model;
    }

    /**
     * Adiciona os links HATEOAS ao modelo.
     * 
     * @param productId O ID do produto
     */
    private void addProductLinks(Long productId) {
        // Link para o próprio recurso
        addSelfLink(ProductController.class, productId)
         
            .addActionLink(ProductController.class, "update", "PUT", productId)
            .addActionLink(ProductController.class, "delete", "DELETE", productId)
            .addCollectionLink(ProductController.class, "products")
            .addActionLink(StockMovementController.class, "stock-movements", "GET", "by-product", productId)
            .addActionLink(StockMovementController.class, "profit", "GET", "profit", productId);
    }

    /**
     * Adiciona um link para a coleção de produtos do mesmo tipo.
     * 
     * @param type O tipo de produto
     * @return O modelo atual para encadeamento
     */
    public ProductModel addProductsByTypeLink(String type) {
        return addActionLink(ProductController.class, "products-by-type", "GET", "type", type);
    }

    /**
     * Adiciona um link para criar uma nova movimentação de estoque para este produto.
     * 
     * @return O modelo atual para encadeamento
     */
    public ProductModel addCreateStockMovementLink() {
        return addActionLink(StockMovementController.class, "create-stock-movement", "POST");
    }
}
