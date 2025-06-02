package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.exception.InsufficientStockException;
import desafio.nexdom.desafio.exception.ProductNotFoundException;
import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class StockMovementServiceImpl implements IStockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final IProductService productService;

    public StockMovementServiceImpl(StockMovementRepository stockMovementRepository,
                                ProductRepository productRepository,
                                IProductService productService) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> findAll(Pageable pageable) {
        return stockMovementRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findAll() {
        return stockMovementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> getMovementsByProduct(Long productId, Pageable pageable) {
        return stockMovementRepository.findByProduct_Id(productId, pageable);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getMovementsByProduct(Long productId) {
        return stockMovementRepository.findByProduct_Id(productId);
    }

    @Transactional
    public StockMovement save(StockMovement stockMovement) {
        if (stockMovement.getProduct() != null && stockMovement.getProduct().getId() != null) {
            Product product = productRepository.findById(stockMovement.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException(stockMovement.getProduct().getId()));
            stockMovement.setProduct(product);
        } else {
            throw new IllegalArgumentException("Product ID is required for stock movement");
        }
        if (stockMovement.getMovementDate() == null) {
            stockMovement.setMovementDate(java.time.LocalDateTime.now());
        }
        validateStockMovement(stockMovement);
        return stockMovementRepository.save(stockMovement);
    }

    @Transactional
    public void validateStockMovement(StockMovement stockMovement) {
        Product product = productRepository.findById(stockMovement.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException(stockMovement.getProduct().getId()));
        if (stockMovement.getMovementType() == MovementType.SAIDA) {
            if (product.getStockQuantity() < stockMovement.getQuantity()) {
                throw new InsufficientStockException(
                    product.getCode(), 
                    product.getStockQuantity(), 
                    stockMovement.getQuantity()
                );
            }
            product.setStockQuantity(product.getStockQuantity() - stockMovement.getQuantity());
        } else {
            product.setStockQuantity(product.getStockQuantity() + stockMovement.getQuantity());
        }
        productService.save(product);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateProfit(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        BigDecimal totalCost = product.getSupplierValue().multiply(BigDecimal.valueOf(product.getStockQuantity()));
        BigDecimal totalRevenue = stockMovementRepository.findAll().stream()
                .filter(m -> m.getProduct().getId().equals(productId) && m.getMovementType() == MovementType.SAIDA)
                .map(m -> m.getSaleValue().multiply(BigDecimal.valueOf(m.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalRevenue.subtract(totalCost);
    }
}
