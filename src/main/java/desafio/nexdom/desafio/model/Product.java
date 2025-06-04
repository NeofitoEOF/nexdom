package desafio.nexdom.desafio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String code;

    @NotBlank
    private String description;

    @NotNull
    private String type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal supplierValue;

    @NotNull
    @Min(0)
    private Integer stockQuantity;
    
    @Version
    private Long version;
}
