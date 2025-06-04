package desafio.nexdom.desafio.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStatsDto {
    private BigDecimal totalStockValue;
    private List<ProductProfitDto> topProfitProducts;
    
    public DashboardStatsDto(BigDecimal totalStockValue, List<ProductProfitDto> topProfitProducts) {
        this.totalStockValue = totalStockValue;
        this.topProfitProducts = topProfitProducts;
    }
    
    public BigDecimal getTotalStockValue() {
        return totalStockValue;
    }
    
    public List<ProductProfitDto> getTopProfitProducts() {
        return topProfitProducts;
    }
}
