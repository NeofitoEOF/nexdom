package desafio.nexdom.desafio.dto;

import java.math.BigDecimal;

public class ProductProfitDto {
    private Long id;
    private String code;
    private String description;
    private BigDecimal totalProfit;
    private int totalSold;
    
    public ProductProfitDto(Long id, String code, String description, BigDecimal totalProfit, int totalSold) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.totalProfit = totalProfit;
        this.totalSold = totalSold;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getTotalProfit() {
        return totalProfit;
    }
    
    public int getTotalSold() {
        return totalSold;
    }
}
