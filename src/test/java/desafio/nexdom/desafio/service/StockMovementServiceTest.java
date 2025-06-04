package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.exception.InsufficientStockException;
import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.dto.ProfitResultDto;
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
        StockMovement entryMovement = new StockMovement();
        entryMovement.setProduct(testProduct);
        entryMovement.setMovementType(MovementType.ENTRADA);
        entryMovement.setPurchaseValue(BigDecimal.valueOf(100));
        entryMovement.setMovementDate(LocalDateTime.now().minusDays(2));
        entryMovement.setQuantity(5);
        entryMovement.setDescription("Compra inicial");

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
        assertEquals(BigDecimal.valueOf(500), profit); 
    }

    @Test
    void testCalculateProfitWithSupplierValue() {
        StockMovement entryMovement = new StockMovement();
        entryMovement.setProduct(testProduct);
        entryMovement.setMovementType(MovementType.ENTRADA);
        entryMovement.setMovementDate(LocalDateTime.now().minusDays(2));
        entryMovement.setQuantity(2);
        entryMovement.setDescription("Compra sem valor explícito");

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
        assertEquals(BigDecimal.valueOf(100), profit);
    }

    @Test
    void testCalculateProfitWithOptimizedMethod() {
        StockMovement entry1 = new StockMovement();
        entry1.setProduct(testProduct);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setPurchaseValue(BigDecimal.valueOf(120));
        entry1.setMovementDate(LocalDateTime.now().minusDays(2));
        entry1.setQuantity(2);
        entry1.setDescription("Compra otimizada");

        StockMovement exit1 = new StockMovement();
        exit1.setProduct(testProduct);
        exit1.setMovementType(MovementType.SAIDA);
        exit1.setSaleValue(BigDecimal.valueOf(200));
        exit1.setMovementDate(LocalDateTime.now());
        exit1.setQuantity(2);
        exit1.setDescription("Venda otimizada");

        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(java.util.List.of(entry1, exit1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        var result = stockMovementService.calculateProfitAndTotalSold(1L);
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(160), result.getProfit()); 
        assertEquals(2, result.getTotalSold());
    }
    
    @Test
    void testCalculateProfitWithMultipleMovements() {
        StockMovement entry1 = new StockMovement();
        entry1.setProduct(testProduct);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setPurchaseValue(BigDecimal.valueOf(100));
        entry1.setMovementDate(LocalDateTime.now().minusDays(3));
        entry1.setQuantity(3);
        entry1.setDescription("Compra lote 1");

        StockMovement entry2 = new StockMovement();
        entry2.setProduct(testProduct);
        entry2.setMovementType(MovementType.ENTRADA);
        entry2.setPurchaseValue(BigDecimal.valueOf(120));
        entry2.setMovementDate(LocalDateTime.now().minusDays(2));
        entry2.setQuantity(2);
        entry2.setDescription("Compra lote 2");

        StockMovement exit1 = new StockMovement();
        exit1.setProduct(testProduct);
        exit1.setMovementType(MovementType.SAIDA);
        exit1.setSaleValue(BigDecimal.valueOf(200));
        exit1.setMovementDate(LocalDateTime.now().minusDays(1));
        exit1.setQuantity(3);
        exit1.setDescription("Venda 1");

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
        assertEquals(BigDecimal.valueOf(560), profit);
    }

    @Test
    void testCalculateProfitWithInsufficientEntries() {
        StockMovement entry = new StockMovement();
        entry.setProduct(testProduct);
        entry.setMovementType(MovementType.ENTRADA);
        entry.setSaleValue(BigDecimal.valueOf(100));
        entry.setMovementDate(LocalDateTime.now().minusDays(2));
        entry.setQuantity(2);
        entry.setDescription("Compra pequena");

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
    
    @Test
    void testCalculateProfitWithOnlyEntryMovements() {
        StockMovement entry1 = new StockMovement();
        entry1.setProduct(testProduct);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setPurchaseValue(BigDecimal.valueOf(100));
        entry1.setMovementDate(LocalDateTime.now().minusDays(2));
        entry1.setQuantity(5);
        entry1.setDescription("Investimento inicial");
        
        StockMovement entry2 = new StockMovement();
        entry2.setProduct(testProduct);
        entry2.setMovementType(MovementType.ENTRADA);
        entry2.setPurchaseValue(BigDecimal.valueOf(120));
        entry2.setMovementDate(LocalDateTime.now().minusDays(1));
        entry2.setQuantity(3);
        entry2.setDescription("Investimento adicional");
        
        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L))
            .thenReturn(java.util.List.of(entry1, entry2));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        BigDecimal profit = stockMovementService.calculateProfit(1L);
        assertNotNull(profit);
        assertEquals(BigDecimal.ZERO, profit, "O lucro deve ser zero quando há apenas movimentos de entrada");
        
        ProfitResultDto result = stockMovementService.calculateProfitAndTotalSold(1L);
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getProfit(), "O lucro deve ser zero quando há apenas movimentos de entrada");
        assertEquals(0, result.getTotalSold(), "Total vendido deve ser zero quando há apenas movimentos de entrada");
    }
    
    @Test
    void testCalculateProfitAndTotalSoldWithFIFO() {
        // Cenário: Entradas com valores diferentes seguidas de saídas
        // Deve usar o método FIFO para calcular o custo das saídas
        
        // Configurar produto
        Product product = new Product();
        product.setId(1L);
        product.setCode("IPHONE15");
        product.setDescription("iPhone 15 Pro Max");
        product.setType("Eletrônico");
        product.setSupplierValue(BigDecimal.valueOf(4000.00));
        product.setStockQuantity(10);
        
        // Primeira entrada: 10 unidades a 3000.00 cada
        StockMovement entry1 = new StockMovement();
        entry1.setId(1L);
        entry1.setProduct(product);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setPurchaseValue(BigDecimal.valueOf(3000.00));
        entry1.setSaleValue(BigDecimal.valueOf(5000.00));
        entry1.setMovementDate(LocalDateTime.now().minusDays(5));
        entry1.setQuantity(10);
        entry1.setDescription("Entrada inicial");
        
        // Segunda entrada: 5 unidades a 3500.00 cada
        StockMovement entry2 = new StockMovement();
        entry2.setId(2L);
        entry2.setProduct(product);
        entry2.setMovementType(MovementType.ENTRADA);
        entry2.setPurchaseValue(BigDecimal.valueOf(3500.00));
        entry2.setSaleValue(BigDecimal.valueOf(5500.00));
        entry2.setMovementDate(LocalDateTime.now().minusDays(3));
        entry2.setQuantity(5);
        entry2.setDescription("Segunda entrada");
        
        // Primeira saída: 8 unidades a 5800.00 cada
        // Deve consumir 8 unidades da primeira entrada (a 3000.00 cada)
        StockMovement exit1 = new StockMovement();
        exit1.setId(3L);
        exit1.setProduct(product);
        exit1.setMovementType(MovementType.SAIDA);
        exit1.setSaleValue(BigDecimal.valueOf(5800.00));
        exit1.setMovementDate(LocalDateTime.now().minusDays(2));
        exit1.setQuantity(8);
        exit1.setDescription("Primeira venda");
        
        // Segunda saída: 4 unidades a 6000.00 cada
        // Deve consumir 2 unidades da primeira entrada (a 3000.00 cada) e 2 unidades da segunda entrada (a 3500.00 cada)
        StockMovement exit2 = new StockMovement();
        exit2.setId(4L);
        exit2.setProduct(product);
        exit2.setMovementType(MovementType.SAIDA);
        exit2.setSaleValue(BigDecimal.valueOf(6000.00));
        exit2.setMovementDate(LocalDateTime.now().minusDays(1));
        exit2.setQuantity(4);
        exit2.setDescription("Segunda venda");
        
        // Configurar mocks
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L))
            .thenReturn(java.util.List.of(entry1, entry2, exit1, exit2));
        
        // Executar o método a ser testado
        ProfitResultDto result = stockMovementService.calculateProfitAndTotalSold(1L);
        
        // Verificações
        assertNotNull(result);
        
        // Cálculo esperado:
        // Receita: (8 * 5800.00) + (4 * 6000.00) = 46400.00 + 24000.00 = 70400.00
        // Custo: (8 * 3000.00) + (2 * 3000.00) + (2 * 3500.00) = 24000.00 + 6000.00 + 7000.00 = 37000.00
        // Lucro: 70400.00 - 37000.00 = 33400.00
        assertEquals(BigDecimal.valueOf(33400.00), result.getProfit());
        assertEquals(12, result.getTotalSold());
    }
}
