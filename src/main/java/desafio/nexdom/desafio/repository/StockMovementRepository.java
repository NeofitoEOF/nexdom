package desafio.nexdom.desafio.repository;

import desafio.nexdom.desafio.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProduct_Id(Long productId, org.springframework.data.domain.Pageable pageable);
    List<StockMovement> findByProduct_Id(Long productId);
    List<StockMovement> findByProduct_IdOrderByMovementDateAsc(Long productId);
}
