package desafio.nexdom.desafio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import desafio.nexdom.desafio.model.StockMovement;

public class StockMovementDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String type;
    private BigDecimal value;
    private Integer quantity;
    private LocalDate date;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public static StockMovementDTO fromEntity(StockMovement movement) {
        StockMovementDTO dto = new StockMovementDTO();
        dto.setId(movement.getId());
        dto.setProductId(movement.getProduct().getId());
        dto.setProductName(movement.getProduct().getDescription());
        dto.setType(movement.getMovementType().name());
        dto.setValue(movement.getSaleValue());
        dto.setQuantity(movement.getQuantity());
        dto.setDate(movement.getMovementDate().toLocalDate());
        return dto;
    }
}
