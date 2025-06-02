package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.dto.StockMovementDTO;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockMovementService {
    @Autowired
    private StockMovementRepository repository;

    public Map<Long, List<StockMovementDTO>> findAllGroupedByProduct() {
        List<StockMovement> all = repository.findAll();
        return all.stream()
            .map(StockMovementDTO::fromEntity)
            .collect(Collectors.groupingBy(StockMovementDTO::getProductId));
    }
}
