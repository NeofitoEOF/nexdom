package desafio.nexdom.desafio.interfaces;

import desafio.nexdom.desafio.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IProductService {
    Product save(Product product);
    Product update(Long id, Product product);
    Product findById(Long id);
    List<Product> findAll();
    void deleteById(Long id);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByType(String type, Pageable pageable);
}
