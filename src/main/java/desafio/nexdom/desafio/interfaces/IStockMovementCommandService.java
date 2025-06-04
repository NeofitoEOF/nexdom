package desafio.nexdom.desafio.interfaces;

import desafio.nexdom.desafio.dto.CreateStockMovementResponse;
import desafio.nexdom.desafio.dto.StockMovementRequest;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.model.StockMovement;

public interface IStockMovementCommandService {
    StockMovement save(StockMovement movement);
    StockMovementModel updateMovement(Long id, StockMovementRequest request);
    CreateStockMovementResponse createStockMovement(StockMovementRequest request);
}
