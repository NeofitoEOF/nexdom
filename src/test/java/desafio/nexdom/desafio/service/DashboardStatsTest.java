package desafio.nexdom.desafio.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import desafio.nexdom.desafio.dto.DashboardStatsDto;
import desafio.nexdom.desafio.dto.ProductProfitDto;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;

public class DashboardStatsTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private StockMovementRepository stockMovementRepository;
    
    @InjectMocks
    private StockMovementServiceImpl stockMovementService;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testGetDashboardStats() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setCode("P001");
        product1.setDescription("Produto 1");
        product1.setStockQuantity(10);
        product1.setSupplierValue(new BigDecimal("100.00"));
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setCode("P002");
        product2.setDescription("Produto 2");
        product2.setStockQuantity(5);
        product2.setSupplierValue(new BigDecimal("200.00"));
        
        List<Product> products = Arrays.asList(product1, product2);
        
        when(productRepository.findAll()).thenReturn(products);
        
        StockMovement entry1 = new StockMovement();
        entry1.setId(1L);
        entry1.setProduct(product1);
        entry1.setMovementType(MovementType.ENTRADA);
        entry1.setQuantity(20);
        entry1.setPurchaseValue(new BigDecimal("80.00"));
        entry1.setSaleValue(new BigDecimal("150.00"));
        entry1.setMovementDate(LocalDateTime.now().minusDays(5));
        
        StockMovement exit1 = new StockMovement();
        exit1.setId(2L);
        exit1.setProduct(product1);
        exit1.setMovementType(MovementType.SAIDA);
        exit1.setQuantity(10);
        exit1.setPurchaseValue(new BigDecimal("80.00"));
        exit1.setSaleValue(new BigDecimal("150.00"));
        exit1.setMovementDate(LocalDateTime.now().minusDays(2));
        
        List<StockMovement> movements1 = Arrays.asList(entry1, exit1);
        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(movements1);
        
        StockMovement entry2 = new StockMovement();
        entry2.setId(3L);
        entry2.setProduct(product2);
        entry2.setMovementType(MovementType.ENTRADA);
        entry2.setQuantity(15);
        entry2.setPurchaseValue(new BigDecimal("150.00"));
        entry2.setSaleValue(new BigDecimal("300.00"));
        entry2.setMovementDate(LocalDateTime.now().minusDays(4));
        
        StockMovement exit2 = new StockMovement();
        exit2.setId(4L);
        exit2.setProduct(product2);
        exit2.setMovementType(MovementType.SAIDA);
        exit2.setQuantity(10);
        exit2.setPurchaseValue(new BigDecimal("150.00"));
        exit2.setSaleValue(new BigDecimal("300.00"));
        exit2.setMovementDate(LocalDateTime.now().minusDays(1));
        
        List<StockMovement> movements2 = Arrays.asList(entry2, exit2);
        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(2L)).thenReturn(movements2);
        
        DashboardStatsDto result = stockMovementService.getDashboardStats();
        
        // Verificar o valor total do estoque
        // Produto 1: 10 * 100.00 = 1000.00
        // Produto 2: 5 * 200.00 = 1000.00
        // Total: 2000.00
        assertEquals(new BigDecimal("2000.00"), result.getTotalStockValue());
        
        // Verificar os produtos com maior lucro
        List<ProductProfitDto> topProfitProducts = result.getTopProfitProducts();
        assertNotNull(topProfitProducts);
        assertTrue(topProfitProducts.size() <= 5);
        
        // Verificar se os produtos estão ordenados por lucro (decrescente)
        if (topProfitProducts.size() >= 2) {
            assertTrue(
                topProfitProducts.get(0).getTotalProfit().compareTo(
                topProfitProducts.get(1).getTotalProfit()) >= 0
            );
        }
    }
    
    @Test
    public void testGetDashboardStatsWithNoProducts() {
        // Mock do repositório de produtos retornando lista vazia
        when(productRepository.findAll()).thenReturn(List.of());
        
        // Executar o método a ser testado
        DashboardStatsDto result = stockMovementService.getDashboardStats();
        
        // Verificar o valor total do estoque
        assertEquals(BigDecimal.ZERO, result.getTotalStockValue());
        
        // Verificar os produtos com maior lucro
        List<ProductProfitDto> topProfitProducts = result.getTopProfitProducts();
        assertNotNull(topProfitProducts);
        assertTrue(topProfitProducts.isEmpty());
    }
    
    @Test
    public void testGetDashboardStatsWithNoMovements() {
        Product product = new Product();
        product.setId(1L);
        product.setCode("P001");
        product.setDescription("Produto 1");
        product.setStockQuantity(10);
        product.setSupplierValue(new BigDecimal("100.00"));
        
        List<Product> products = List.of(product);
        
        when(productRepository.findAll()).thenReturn(products);
        
        when(stockMovementRepository.findByProduct_IdOrderByMovementDateAsc(1L)).thenReturn(List.of());
        
        DashboardStatsDto result = stockMovementService.getDashboardStats();
        
        assertEquals(new BigDecimal("1000.00"), result.getTotalStockValue());
        
        List<ProductProfitDto> topProfitProducts = result.getTopProfitProducts();
        assertNotNull(topProfitProducts);
        assertTrue(topProfitProducts.isEmpty());
    }
}
