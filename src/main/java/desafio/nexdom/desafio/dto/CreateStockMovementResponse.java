package desafio.nexdom.desafio.dto;

import desafio.nexdom.desafio.hateoas.StockMovementModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStockMovementResponse {
    private StockMovementModel movement;
    private String message;
}
