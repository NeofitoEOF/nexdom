package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.exception.InsufficientStockException;
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
    private ProductService productService;

    @InjectMocks
    private StockMovementService stockMovementService;

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
        testMovement.setQuantity(15); // More than available stock
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(InsufficientStockException.class, () -> {
            stockMovementService.save(testMovement);
        });
    }

    @Test
    void testCalculateProfit() {
        // Configurar uma movimentação de saída para gerar receita
        StockMovement exitMovement = new StockMovement();
        exitMovement.setProduct(testProduct);
        exitMovement.setMovementType(MovementType.SAIDA);
        exitMovement.setSaleValue(BigDecimal.valueOf(200)); // Valor de venda maior que o custo
        exitMovement.setMovementDate(LocalDateTime.now());
        exitMovement.setQuantity(5);
        
        // Configurar o mock para retornar a movimentação de saída
        when(stockMovementRepository.findAll()).thenReturn(java.util.List.of(exitMovement));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        BigDecimal profit = stockMovementService.calculateProfit(1L);

        assertNotNull(profit);
        // Verificar se o lucro é calculado corretamente
        // Receita: 5 * 200 = 1000
        // Custo: 10 * 100 = 1000 (estoque atual * valor do fornecedor)
        // Lucro esperado: 1000 - 1000 = 0
        assertEquals(BigDecimal.ZERO, profit);
    }
    
    @Test
    void testCalculateProfitWithMultipleMovements() {
        // Criar múltiplas movimentações para testar cenário mais complexo
        StockMovement exitMovement1 = new StockMovement();
        exitMovement1.setProduct(testProduct);
        exitMovement1.setMovementType(MovementType.SAIDA);
        exitMovement1.setSaleValue(BigDecimal.valueOf(200));
        exitMovement1.setMovementDate(LocalDateTime.now());
        exitMovement1.setQuantity(3);
        
        StockMovement exitMovement2 = new StockMovement();
        exitMovement2.setProduct(testProduct);
        exitMovement2.setMovementType(MovementType.SAIDA);
        exitMovement2.setSaleValue(BigDecimal.valueOf(250));
        exitMovement2.setMovementDate(LocalDateTime.now());
        exitMovement2.setQuantity(2);
        
        StockMovement entryMovement = new StockMovement();
        entryMovement.setProduct(testProduct);
        entryMovement.setMovementType(MovementType.ENTRADA);
        entryMovement.setSaleValue(BigDecimal.valueOf(150));
        entryMovement.setMovementDate(LocalDateTime.now());
        entryMovement.setQuantity(5);
        
        when(stockMovementRepository.findAll()).thenReturn(java.util.List.of(exitMovement1, exitMovement2, entryMovement));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        BigDecimal profit = stockMovementService.calculateProfit(1L);

        assertNotNull(profit);
        // Receita: (3 * 200) + (2 * 250) = 600 + 500 = 1100
        // Custo: 10 * 100 = 1000
        // Lucro esperado: 1100 - 1000 = 100
        assertEquals(BigDecimal.valueOf(100), profit);
    }
}
