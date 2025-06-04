package desafio.nexdom.desafio.hateoas;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import desafio.nexdom.desafio.model.StockMovement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id", "productId", "movementType", "saleValue", 
    "movementDate", "quantity", "_links", "_embedded"
})
@Schema(description = "Representação de uma movimentação de estoque")
public class StockMovementModel extends BaseModel<StockMovementModel> {

    @Schema(description = "Identificador único da movimentação", example = "1")
    private Long id;

    @Schema(description = "ID do produto relacionado", example = "1", required = true)
    @JsonProperty("productId")
    private Long productId;

    @Schema(
        description = "Tipo de movimentação", 
        example = "ENTRY", 
        allowableValues = {"ENTRY", "EXIT"},
        required = true
    )
    @JsonProperty("movementType")
    private String movementType;

    @Schema(
        description = "Valor de venda (opcional para entradas, obrigatório para saídas)", 
        example = "1999.90"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal saleValue;

    @Schema(
        description = "Data e hora da movimentação", 
        example = "2025-05-31T22:30:45",
        type = "string"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime movementDate;
    
    public StockMovementModel() {
        super();
    }
    
    public void setContent(Map<String, Object> content) {
        if (content != null) {
            this.id = (Long) content.get("id");
            this.productId = (Long) content.get("productId");
            this.movementType = (String) content.get("movementType");
            this.saleValue = (BigDecimal) content.get("saleValue");
            this.movementDate = (LocalDateTime) content.get("movementDate");
            this.quantity = (Integer) content.get("quantity");
            this.description = (String) content.get("description");
        }
    }

    @Schema(
        description = "Quantidade movimentada (positiva para entradas, negativa para saídas)", 
        example = "10",
        required = true
    )
    private Integer quantity;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Descrição da movimentação de estoque (ex: venda, ajuste, devolução, etc.)", example = "Venda balcão 01")
    private String description;

    public static StockMovementModel fromStockMovement(StockMovement movement) {
        if (movement == null) {
            return null;
        }
        StockMovementModel model = new StockMovementModel();
        model.setId(movement.getId());
        model.setProductId(movement.getProduct() != null ? movement.getProduct().getId() : null);
        model.setMovementType(movement.getMovementType() != null ? movement.getMovementType().name() : null);
        model.setSaleValue(movement.getSaleValue());
        model.setMovementDate(movement.getMovementDate());
        model.setQuantity(movement.getQuantity());
        model.setDescription(movement.getDescription()); 
        model.addStockMovementLinks(movement);
        
        return model;
    }

    private void addStockMovementLinks(StockMovement movement) {
        this.addSelfLink(desafio.nexdom.desafio.controller.StockMovementController.class, movement.getId());
        this.addActionLink(desafio.nexdom.desafio.controller.StockMovementController.class, "delete", "DELETE", movement.getId());
        this.addCollectionLink(desafio.nexdom.desafio.controller.StockMovementController.class, "all-movements");
        if (movement.getProduct() != null) {
            this.addActionLink(desafio.nexdom.desafio.controller.ProductController.class, "product", "GET", movement.getProduct().getId());
        }
    }
}
