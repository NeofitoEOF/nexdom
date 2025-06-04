package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductServiceImpl(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            throw new IllegalStateException("Não é possível excluir o produto enquanto o estoque for maior que zero.");
        }
        stockMovementRepository.findByProduct_Id(id).forEach(stockMovementRepository::delete);
        productRepository.deleteById(id);
    }

    @Transactional
    public Product update(Long id, Product product) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        existing.setCode(product.getCode());
        existing.setDescription(product.getDescription());
        existing.setType(product.getType());
        existing.setSupplierValue(product.getSupplierValue());
    
        existing.setStockQuantity(product.getStockQuantity());
        return productRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByType(String type, Pageable pageable) {
        Page<Product> allPage = productRepository.findAll(pageable);
        List<Product> filtered = allPage.getContent().stream()
            .filter(p -> p.getType().equalsIgnoreCase(type))
            .toList();
        return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
    }
}
