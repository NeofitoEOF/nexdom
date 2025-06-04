package desafio.nexdom.desafio.interfaces;

import desafio.nexdom.desafio.dto.DashboardStatsDto;
import desafio.nexdom.desafio.dto.ProfitResultDto;
import desafio.nexdom.desafio.dto.StockMovementDTO;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IStockMovementQueryService {
    Page<StockMovement> findAll(Pageable pageable);
    List<StockMovement> findAll();
    Page<StockMovement> getMovementsByProduct(Long productId, Pageable pageable);
    List<StockMovement> getMovementsByProduct(Long productId);
    BigDecimal calculateProfit(Long productId);
    ProfitResultDto calculateProfitAndTotalSold(Long productId);
    Map<Long, List<StockMovementDTO>> findAllGroupedByProduct();
    StockMovementModel getMovementModelById(Long id);
    DashboardStatsDto getDashboardStats();
}
