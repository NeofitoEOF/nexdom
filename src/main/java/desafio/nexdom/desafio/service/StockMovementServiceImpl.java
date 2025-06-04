package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.dto.CreateStockMovementResponse;
import desafio.nexdom.desafio.dto.DashboardStatsDto;
import desafio.nexdom.desafio.dto.ProductProfitDto;
import desafio.nexdom.desafio.dto.ProfitResultDto;
import desafio.nexdom.desafio.dto.StockMovementDTO;
import desafio.nexdom.desafio.dto.StockMovementRequest;
import desafio.nexdom.desafio.exception.InsufficientEntryStockForProfitException;
import desafio.nexdom.desafio.exception.InsufficientStockException;
import desafio.nexdom.desafio.exception.ProductNotFoundException;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class StockMovementServiceImpl implements IStockMovementService {

    @Override
    public StockMovementModel getMovementModelById(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        return StockMovementModel.fromStockMovement(movement);
    }

    @Override
    @Transactional
    public StockMovementModel updateMovement(Long id, StockMovementRequest request) {
        StockMovement movement = stockMovementRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        movement.setMovementType(request.getMovementType());
        movement.setSaleValue(request.getSaleValue());
        movement.setPurchaseValue(request.getPurchaseValue());
        movement.setQuantity(request.getQuantity());
        movement.setDescription(request.getDescription());

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
            movement.setProduct(product);
        }

        validateStockMovementData(movement);
        StockMovement updated = stockMovementRepository.save(movement);
        return StockMovementModel.fromStockMovement(updated);
    }

    @Override
    public BigDecimal calculateProfit(Long productId) {
        ProfitResultDto result = calculateProfitAndTotalSold(productId);
        if (result == null) {
            return BigDecimal.ZERO;
        }
        return result.getProfit();
    }
    
    @Override
    @Transactional
    public CreateStockMovementResponse createStockMovement(StockMovementRequest request) {
        try {
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
            
            StockMovement stockMovement = new StockMovement();
            stockMovement.setProduct(product);
            stockMovement.setMovementType(request.getMovementType());
            stockMovement.setSaleValue(request.getSaleValue());
            stockMovement.setPurchaseValue(request.getPurchaseValue());
            stockMovement.setQuantity(request.getQuantity());
            stockMovement.setMovementDate(LocalDateTime.now());
            stockMovement.setDescription(request.getDescription());
            
            validateStockMovementData(stockMovement);
            StockMovement savedMovement = stockMovementRepository.save(stockMovement);
            updateProductStock(product, request.getMovementType(), request.getQuantity());
            StockMovementModel model = StockMovementModel.fromStockMovement(savedMovement);
            return new CreateStockMovementResponse(model, "Movimentação de estoque criada com sucesso");
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar movimentação de estoque: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    private void updateProductStock(Product product, MovementType movementType, int quantity) {
        Product lockedProduct = productRepository.findByIdForUpdate(product.getId())
                .orElseThrow(() -> new ProductNotFoundException(product.getId()));
        
        int currentStock = lockedProduct.getStockQuantity();
        
        if (movementType == MovementType.ENTRADA) {
            lockedProduct.setStockQuantity(currentStock + quantity);
        } else if (movementType == MovementType.SAIDA) {
            if (currentStock < quantity) {
                throw new InsufficientStockException("Estoque insuficiente para o produto ID " + lockedProduct.getId() + 
                    ". Disponível: " + currentStock + ", Solicitado: " + quantity);
            }
            lockedProduct.setStockQuantity(currentStock - quantity);
        }
        
        productRepository.save(lockedProduct);
    }

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
        validateStockMovementData(stockMovement);
        loadAndSetProduct(stockMovement);
        setDefaultMovementDate(stockMovement);
        validateStockMovement(stockMovement);
        return stockMovementRepository.save(stockMovement);
    }

    private void validateStockMovementData(StockMovement stockMovement) {
        java.util.Objects.requireNonNull(stockMovement, "Stock movement cannot be null");

        java.util.List<String> validationErrors = new java.util.ArrayList<>();

        if (stockMovement.getProduct() == null || stockMovement.getProduct().getId() == null) {
            validationErrors.add("Product ID is required for stock movement");
        }

        if (stockMovement.getQuantity() == null || stockMovement.getQuantity() <= 0) {
            validationErrors.add("Quantity must be greater than zero");
        }

        if (stockMovement.getMovementType() == null) {
            validationErrors.add("Movement type is required");
        }

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", validationErrors));
        }
    }

    private void loadAndSetProduct(StockMovement stockMovement) {
        Long productId = stockMovement.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        stockMovement.setProduct(product);
    }

    private void setDefaultMovementDate(StockMovement stockMovement) {
        if (stockMovement.getMovementDate() == null) {
            stockMovement.setMovementDate(java.time.LocalDateTime.now());
        }
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
                        stockMovement.getQuantity());
            }
            product.setStockQuantity(product.getStockQuantity() - stockMovement.getQuantity());
        } else {
            product.setStockQuantity(product.getStockQuantity() + stockMovement.getQuantity());
        }
        productService.save(product);
    }

    @Transactional(readOnly = true)
    public ProfitResultDto calculateProfitAndTotalSold(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        List<StockMovement> movements = stockMovementRepository
                .findByProduct_IdOrderByMovementDateAsc(productId);
        
        if (movements.isEmpty()) {
            return new ProfitResultDto(BigDecimal.ZERO, 0);
        }

        Queue<Map.Entry<Integer, BigDecimal>> fifoQueue = new LinkedList<>();
        processEntryMovements(product, movements, fifoQueue);
        
        FifoResult result = processExitMovements(product, productId, movements, fifoQueue);
        
        return new ProfitResultDto(result.getTotalRevenue().subtract(result.getTotalCost()), result.getTotalSold());
    }

    private void processEntryMovements(Product product, List<StockMovement> movements, 
        Queue<Map.Entry<Integer, BigDecimal>> fifoQueue) {
        movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.ENTRADA)
                .forEach(movement -> {
                    BigDecimal purchaseValue = Optional.ofNullable(movement.getPurchaseValue())
                            .orElse(product.getSupplierValue());
                    fifoQueue.offer(Map.entry(movement.getQuantity(), purchaseValue));
                });
    }

    private FifoResult processExitMovements(Product product, Long productId, 
        List<StockMovement> movements, Queue<Map.Entry<Integer, BigDecimal>> fifoQueue) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        int totalSold = 0;
        
        for (StockMovement movement : movements) {
            if (movement.getMovementType() == MovementType.SAIDA) {
                int quantitySold = movement.getQuantity();
                totalSold += quantitySold;

                BigDecimal saleValue = Optional.ofNullable(movement.getSaleValue())
                        .orElse(product.getSupplierValue());
                
                BigDecimal movementRevenue = saleValue.multiply(BigDecimal.valueOf(quantitySold));
                totalRevenue = totalRevenue.add(movementRevenue);

                int remaining = quantitySold;
                
                Queue<Map.Entry<Integer, BigDecimal>> tempFifo = new LinkedList<>(fifoQueue);
                Queue<Map.Entry<Integer, BigDecimal>> updatedFifo = new LinkedList<>();
                
                while (remaining > 0 && !tempFifo.isEmpty()) {
                    Map.Entry<Integer, BigDecimal> entry = tempFifo.poll();
                    int available = entry.getKey();
                    BigDecimal unitCost = entry.getValue();

                    int consumed = Math.min(remaining, available);
                    BigDecimal partialCost = unitCost.multiply(BigDecimal.valueOf(consumed));
                    totalCost = totalCost.add(partialCost);
                    
                    remaining -= consumed;

                    if (consumed < available) {
                        updatedFifo.offer(Map.entry(available - consumed, unitCost));
                    }
                }
                
                while (!tempFifo.isEmpty()) {
                    updatedFifo.offer(tempFifo.poll());
                }
                
                fifoQueue = updatedFifo;

                if (remaining > 0) {
                    throw new InsufficientEntryStockForProfitException(productId, quantitySold,
                            quantitySold - remaining);
                }
            }
        }
        
        return new FifoResult(totalRevenue, totalCost, totalSold);
    }

    private static class FifoResult {
        private final BigDecimal totalRevenue;
        private final BigDecimal totalCost;
        private final int totalSold;
        
        public FifoResult(BigDecimal totalRevenue, BigDecimal totalCost, int totalSold) {
            this.totalRevenue = totalRevenue;
            this.totalCost = totalCost;
            this.totalSold = totalSold;
        }
        
        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }
        
        public BigDecimal getTotalCost() {
            return totalCost;
        }
        
        public int getTotalSold() {
            return totalSold;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        BigDecimal totalStockValue = BigDecimal.ZERO;
        List<Product> allProducts = productRepository.findAll();
        
        for (Product product : allProducts) {
            BigDecimal productStockValue = product.getSupplierValue()
                    .multiply(BigDecimal.valueOf(product.getStockQuantity()));
            totalStockValue = totalStockValue.add(productStockValue);
        }
        
        List<ProductProfitDto> topProfitProducts = new ArrayList<>();
        
        for (Product product : allProducts) {
            try {
                ProfitResultDto profitResult = calculateProfitAndTotalSold(product.getId());
                if (profitResult.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                    topProfitProducts.add(new ProductProfitDto(
                            product.getId(),
                            product.getCode(),
                            product.getDescription(),
                            profitResult.getProfit(),
                            profitResult.getTotalSold()
                    ));
                }
            } catch (Exception e) {
            }
        }
        
        topProfitProducts.sort((a, b) -> b.getTotalProfit().compareTo(a.getTotalProfit()));
        if (topProfitProducts.size() > 5) {
            topProfitProducts = topProfitProducts.subList(0, 5);
        }
        
        return new DashboardStatsDto(totalStockValue, topProfitProducts);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<StockMovementDTO>> findAllGroupedByProduct() {
        List<StockMovement> all = stockMovementRepository.findAll();
        return all.stream()
                .map(StockMovementDTO::fromEntity)
                .collect(Collectors.groupingBy(StockMovementDTO::getProductId));
    }
}
