package desafio.nexdom.desafio.repository;

import desafio.nexdom.desafio.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByCode(String code);
    Product findByDescription(String description);
    List<Product> findByType(String type);
}
