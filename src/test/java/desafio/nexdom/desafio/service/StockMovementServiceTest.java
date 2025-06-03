package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.exception.InsufficientStockException;
import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {
    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IProductService productService;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    private Product testProduct;
    private StockMovement testMovement;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCode("TEST-001");
        testProduct.setDescription("Test Product");
        testProduct.setType("ELECTRONIC");
        testProduct.setSupplierValue(BigDecimal.valueOf(100));
        testProduct.setStockQuantity(10);

        testMovement = new StockMovement();
        testMovement.setProduct(testProduct);
        testMovement.setMovementType(MovementType.ENTRADA);
        testMovement.setSaleValue(BigDecimal.valueOf(150));
        testMovement.setMovementDate(LocalDateTime.now());
        testMovement.setQuantity(5);
        testMovement.setDescription("Movimentação de teste");
    }

    @Test
    void testSaveStockMovement() {
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(testMovement);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        StockMovement savedMovement = stockMovementService.save(testMovement);

        assertNotNull(savedMovement);
        assertEquals(testMovement.getMovementType(), savedMovement.getMovementType());
        assertEquals(testMovement.getSaleValue(), savedMovement.getSaleValue());
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void testStockMovementInsufficientStock() {
        testMovement.setMovementType(MovementType.SAIDA);
        testMovement.setQuantity(15);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(InsufficientStockException.class, () -> {
            stockMovementService.save(testMovement);
        });
    }

    @Test
    void testCalculateProfit() {
        // Entrada (compra): 5 unidades a 100 (purchaseValue)
        StockMovement entryMovement = new StockMovement();
        entryMovement.setProduct(testProduct);
        entryMovement.setMovementType(MovementType.ENTRADA);
        entryMovement.setPurchaseValue(BigDecimal.valueOf(100));
        entryMovement.setMovementDate(LocalDateTime.now().minusDays(2));
        entryMovement.setQuantity(5);
        entryMovement.setDescription("Compra inicial");

        // Saída (venda): 5 unidades a 200
        StockMovement exitMovement = new StockMovement();
        exitMovement.setProduct(testProduct);
        exitMovement.setMovementType(MovementType.SAIDA);
        exitMovement.setSaleValue(BigDecimal.valueOf(200));
        exitMovement.setMovementDate(LocalDateTime.now());
        exitMovement.setQuantity(5);
        exitMovement.setDescription("Venda de estoque");

        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(java.util.List.of(entryMovement, exitMovement));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        BigDecimal profit = stockMovementService.calculateProfit(1L);

        assertNotNull(profit);
        assertEquals(BigDecimal.valueOf(500), profit); // (5*200) - (5*100)
    }

    @Test
    void testCalculateProfitWithSupplierValue() {
        // Entrada (compra): 2 unidades sem purchaseValue (deve usar supplierValue=100)
        StockMovement entryMovement = new StockMovement();
        entryMovement.setProduct(testProduct);
        entryMovement.setMovementType(MovementType.ENTRADA);
        entryMovement.setMovementDate(LocalDateTime.now().minusDays(2));
        entryMovement.setQuantity(2);
        entryMovement.setDescription("Compra sem valor explícito");

        // Saída (venda): 2 unidades a 150
        StockMovement exitMovement = new StockMovement();
        exitMovement.setProduct(testProduct);
        exitMovement.setMovementType(MovementType.SAIDA);
        exitMovement.setSaleValue(BigDecimal.valueOf(150));
        exitMovement.setMovementDate(LocalDateTime.now());
        exitMovement.setQuantity(2);
        exitMovement.setDescription("Venda de estoque");

        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(java.util.List.of(entryMovement, exitMovement));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        BigDecimal profit = stockMovementService.calculateProfit(1L);
        assertNotNull(profit);
        assertEquals(BigDecimal.valueOf(100), profit); // (2*150) - (2*100)
    }

    @Test
    void testCalculateProfitWithOptimizedMethod() {
        // Entrada 1: 2 unidades a 120
        StockMovement entry1 = new StockMovement();
        entry1.setProduct(testProduct);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setPurchaseValue(BigDecimal.valueOf(120));
        entry1.setMovementDate(LocalDateTime.now().minusDays(2));
        entry1.setQuantity(2);
        entry1.setDescription("Compra otimizada");

        // Saída 1: 2 unidades a 200
        StockMovement exit1 = new StockMovement();
        exit1.setProduct(testProduct);
        exit1.setMovementType(MovementType.SAIDA);
        exit1.setSaleValue(BigDecimal.valueOf(200));
        exit1.setMovementDate(LocalDateTime.now());
        exit1.setQuantity(2);
        exit1.setDescription("Venda otimizada");

        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(java.util.List.of(entry1, exit1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        var result = stockMovementService.calculateProfitAndTotalSoldOptimized(1L);
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(160), result.getProfit()); // (2*200) - (2*120)
        assertEquals(2, result.getTotalSold());
    }
    
    @Test
    void testCalculateProfitWithMultipleMovements() {
        // Entrada 1: 3 unidades a 100
        StockMovement entry1 = new StockMovement();
        entry1.setProduct(testProduct);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setPurchaseValue(BigDecimal.valueOf(100));
        entry1.setMovementDate(LocalDateTime.now().minusDays(3));
        entry1.setQuantity(3);
        entry1.setDescription("Compra lote 1");

        // Entrada 2: 2 unidades a 120
        StockMovement entry2 = new StockMovement();
        entry2.setProduct(testProduct);
        entry2.setMovementType(MovementType.ENTRADA);
        entry2.setPurchaseValue(BigDecimal.valueOf(120));
        entry2.setMovementDate(LocalDateTime.now().minusDays(2));
        entry2.setQuantity(2);
        entry2.setDescription("Compra lote 2");

        // Saída 1: 3 unidades vendidas a 200
        StockMovement exit1 = new StockMovement();
        exit1.setProduct(testProduct);
        exit1.setMovementType(MovementType.SAIDA);
        exit1.setSaleValue(BigDecimal.valueOf(200));
        exit1.setMovementDate(LocalDateTime.now().minusDays(1));
        exit1.setQuantity(3);
        exit1.setDescription("Venda 1");

        // Saída 2: 2 unidades vendidas a 250
        StockMovement exit2 = new StockMovement();
        exit2.setProduct(testProduct);
        exit2.setMovementType(MovementType.SAIDA);
        exit2.setSaleValue(BigDecimal.valueOf(250));
        exit2.setMovementDate(LocalDateTime.now());
        exit2.setQuantity(2);
        exit2.setDescription("Venda 2");

        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(java.util.List.of(entry1, entry2, exit1, exit2));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        BigDecimal profit = stockMovementService.calculateProfit(1L);

        assertNotNull(profit);
        // FIFO: (3*100 + 2*120) = 300 + 240 = 540 (custo)
        // Receita: (3*200 + 2*250) = 600 + 500 = 1100
        // Lucro: 1100 - 540 = 560
        assertEquals(BigDecimal.valueOf(560), profit);
    }

    @Test
    void testCalculateProfitWithInsufficientEntries() {
        // Entrada: 2 unidades a 100
        StockMovement entry = new StockMovement();
        entry.setProduct(testProduct);
        entry.setMovementType(MovementType.ENTRADA);
        entry.setSaleValue(BigDecimal.valueOf(100));
        entry.setMovementDate(LocalDateTime.now().minusDays(2));
        entry.setQuantity(2);
        entry.setDescription("Compra pequena");

        // Saída: 5 unidades a 200 (mais do que disponível nas entradas)
        StockMovement exit = new StockMovement();
        exit.setProduct(testProduct);
        exit.setMovementType(MovementType.SAIDA);
        exit.setSaleValue(BigDecimal.valueOf(200));
        exit.setMovementDate(LocalDateTime.now());
        exit.setQuantity(5);
        exit.setDescription("Venda excedente");

        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(java.util.List.of(entry, exit));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(desafio.nexdom.desafio.exception.InsufficientEntryStockForProfitException.class, () -> {
            stockMovementService.calculateProfit(1L);
        });
    }
}
