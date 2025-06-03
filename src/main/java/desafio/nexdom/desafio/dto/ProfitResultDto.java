package desafio.nexdom.desafio.dto;

import java.math.BigDecimal;

public class ProfitResultDto {
    private BigDecimal profit;
    private int totalSold;

    public ProfitResultDto(BigDecimal profit, int totalSold) {
        this.profit = profit;
        this.totalSold = totalSold;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }
}
