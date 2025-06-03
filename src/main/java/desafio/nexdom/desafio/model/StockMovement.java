package desafio.nexdom.desafio.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
// getter e setter de purchaseValue serão gerados pelo Lombok
@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("movementType")
    private MovementType movementType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal saleValue;

    /**
     * Valor de compra do produto na movimentação de ENTRADA.
     * Para movimentações de SAÍDA, pode ser nulo.
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal purchaseValue;

    @NotNull
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime movementDate;

    @NotNull
    @Min(1)
    private Integer quantity;

    @Column(length = 255)
    @io.swagger.v3.oas.annotations.media.Schema(description = "Descrição da movimentação de estoque (ex: venda, ajuste, devolução, etc.)", example = "Venda balcão 01")
    private String description;
    
    @JsonIgnore
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

}
