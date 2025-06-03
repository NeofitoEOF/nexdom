package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.dto.StockMovementDTO;
import desafio.nexdom.desafio.dto.ProfitResultDto;
import desafio.nexdom.desafio.exception.InsufficientEntryStockForProfitException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class StockMovementServiceImpl implements IStockMovementService {

    @Override
    public BigDecimal calculateProfit(Long productId) {
        ProfitResultDto result = calculateProfitAndTotalSold(productId);
        if (result == null) {
            return BigDecimal.ZERO;
        }
        return result.getProfit();
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
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<StockMovement> movements = stockMovementRepository
                .findByProduct_IdOrderByMovementDateAsc(productId);

        if (movements.isEmpty()) {
            return new ProfitResultDto(BigDecimal.ZERO, 0);
        }

        List<StockMovement> entradas = movements.stream()
                .filter(m -> m.getMovementType() == MovementType.ENTRADA)
                .collect(Collectors.toList());

        List<StockMovement> saidas = movements.stream()
                .filter(m -> m.getMovementType() == MovementType.SAIDA)
                .collect(Collectors.toList());

        if (saidas.isEmpty()) {
            return new ProfitResultDto(BigDecimal.ZERO, 0);
        }

        int totalEntradas = entradas.stream().mapToInt(StockMovement::getQuantity).sum();
        int totalSaidas = saidas.stream().mapToInt(StockMovement::getQuantity).sum();

        if (totalSaidas > totalEntradas) {
            throw new InsufficientEntryStockForProfitException(productId, totalSaidas, totalEntradas);
        }

        Queue<EntradaInfo> fifoQueue = new LinkedList<>();
        for (StockMovement entrada : entradas) {
            BigDecimal valorCompra = entrada.getPurchaseValue();
            if (valorCompra == null) {
                valorCompra = entrada.getProduct().getSupplierValue();
            }
            fifoQueue.offer(new EntradaInfo(entrada.getQuantity(), valorCompra));
        }

        BigDecimal totalReceita = BigDecimal.ZERO;
        BigDecimal totalCusto = BigDecimal.ZERO;
        int totalVendido = 0;

        for (StockMovement saida : saidas) {
            int quantidadeVenda = saida.getQuantity();
            totalVendido += quantidadeVenda;

            BigDecimal valorVenda = saida.getSaleValue();
            if (valorVenda == null) {
                valorVenda = saida.getProduct().getSupplierValue();
            }
            totalReceita = totalReceita.add(valorVenda.multiply(BigDecimal.valueOf(quantidadeVenda)));

            int restanteParaVender = quantidadeVenda;
            while (restanteParaVender > 0 && !fifoQueue.isEmpty()) {
                EntradaInfo entrada = fifoQueue.peek();

                int quantidadeConsumida = Math.min(restanteParaVender, entrada.getQuantidadeDisponivel());
                BigDecimal custoConsumido = entrada.getValorUnitario()
                        .multiply(BigDecimal.valueOf(quantidadeConsumida));

                totalCusto = totalCusto.add(custoConsumido);
                entrada.consumir(quantidadeConsumida);
                restanteParaVender -= quantidadeConsumida;

                if (entrada.getQuantidadeDisponivel() == 0) {
                    fifoQueue.poll();
                }
            }
        }

        BigDecimal lucroTotal = totalReceita.subtract(totalCusto);
        return new ProfitResultDto(lucroTotal, totalVendido);
    }

    @Transactional(readOnly = true)
    public ProfitResultDto calculateProfitAndTotalSoldOptimized(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        List<StockMovement> movements = stockMovementRepository
                .findByProduct_IdOrderByMovementDateAsc(productId);
        if (movements.isEmpty()) {
            return new ProfitResultDto(BigDecimal.ZERO, 0);
        }

        BigDecimal totalReceita = BigDecimal.ZERO;
        BigDecimal totalCusto = BigDecimal.ZERO;
        int totalVendido = 0;

        Queue<Map.Entry<Integer, BigDecimal>> estoqueFifo = new LinkedList<>();

        for (StockMovement movement : movements) {
            if (movement.getMovementType() == MovementType.ENTRADA) {
                BigDecimal valorCompra = movement.getPurchaseValue() != null ? movement.getPurchaseValue()
                        : movement.getProduct().getSupplierValue();
                estoqueFifo.offer(Map.entry(movement.getQuantity(), valorCompra));

            } else if (movement.getMovementType() == MovementType.SAIDA) {
                int quantidadeVenda = movement.getQuantity();
                totalVendido += quantidadeVenda;

                BigDecimal valorVenda = movement.getSaleValue() != null ? movement.getSaleValue()
                        : movement.getProduct().getSupplierValue();
                totalReceita = totalReceita.add(valorVenda.multiply(BigDecimal.valueOf(quantidadeVenda)));

                int restante = quantidadeVenda;
                while (restante > 0 && !estoqueFifo.isEmpty()) {
                    Map.Entry<Integer, BigDecimal> entrada = estoqueFifo.peek();
                    int disponivel = entrada.getKey();
                    BigDecimal custoUnitario = entrada.getValue();

                    int consumir = Math.min(restante, disponivel);
                    totalCusto = totalCusto.add(custoUnitario.multiply(BigDecimal.valueOf(consumir)));

                    restante -= consumir;

                    if (consumir == disponivel) {
                        estoqueFifo.poll();
                    } else {
                        estoqueFifo.poll();
                        estoqueFifo.offer(Map.entry(disponivel - consumir, custoUnitario));
                    }
                }

                if (restante > 0) {
                    throw new InsufficientEntryStockForProfitException(productId, quantidadeVenda,
                            quantidadeVenda - restante);
                }
            }
        }
        return new ProfitResultDto(totalReceita.subtract(totalCusto), totalVendido);
    }

    private static class EntradaInfo {
        private int quantidadeDisponivel;
        private final BigDecimal valorUnitario;

        public EntradaInfo(int quantidade, BigDecimal valorUnitario) {
            this.quantidadeDisponivel = quantidade;
            this.valorUnitario = valorUnitario;
        }

        public void consumir(int quantidade) {
            this.quantidadeDisponivel -= quantidade;
        }

        public int getQuantidadeDisponivel() {
            return quantidadeDisponivel;
        }

        public BigDecimal getValorUnitario() {
            return valorUnitario;
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, List<StockMovementDTO>> findAllGroupedByProduct() {
        List<StockMovement> all = stockMovementRepository.findAll();
        return all.stream()
                .map(StockMovementDTO::fromEntity)
                .collect(Collectors.groupingBy(StockMovementDTO::getProductId));
    }
}
