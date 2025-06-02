package desafio.nexdom.desafio.controller;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMovementController.class)
class StockMovementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IStockMovementService stockMovementService;
    @MockBean
    private desafio.nexdom.desafio.repository.ProductRepository productRepository;
    @MockBean
    private desafio.nexdom.desafio.repository.StockMovementRepository stockMovementRepository;

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
    void testGetAllMovements() throws Exception {
        when(stockMovementService.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testMovement)));
        mockMvc.perform(get("/api/stock-movements?page=0&size=20"))
                .andExpect(status().isOk());
        verify(stockMovementService, times(1)).findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
    }
    
    @Test
    void testGetMovementsByProduct() throws Exception {
        when(stockMovementService.getMovementsByProduct(1L)).thenReturn(List.of(testMovement));
        mockMvc.perform(get("/api/stock-movements/by-product/1"))
                .andExpect(status().isOk());
        verify(stockMovementService, times(1)).getMovementsByProduct(1L);
    }

    @Test
    void testCreateAndGetMovement() throws Exception {
        StockMovement savedMovement = new StockMovement();
        savedMovement.setId(77L);
        savedMovement.setProduct(testProduct);
        savedMovement.setMovementType(MovementType.ENTRADA);
        savedMovement.setSaleValue(BigDecimal.valueOf(150));
        savedMovement.setMovementDate(java.time.LocalDateTime.parse("2025-05-31T12:00:00"));
        savedMovement.setQuantity(5);
        savedMovement.setDescription("Movimentação detalhada");
        when(stockMovementService.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));
        when(stockMovementRepository.findById(77L)).thenReturn(java.util.Optional.of(savedMovement));

        mockMvc.perform(post("/api/stock-movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"productId\": 1," +
                        "\"movementType\": \"ENTRADA\"," +
                        "\"saleValue\": 150.0," +
                        "\"quantity\": 5," +
                        "\"description\": \"Movimentação detalhada\"" +
                        "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.description").value("Movimentação detalhada"));

        mockMvc.perform(get("/api/stock-movements/77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77L))
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.movementType").value("ENTRADA"))
                .andExpect(jsonPath("$.saleValue").value(150.0))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.description").value("Movimentação detalhada"));
    }

    @Test
    void testCreateMovement() throws Exception {
        StockMovement savedMovement = new StockMovement();
        savedMovement.setId(99L);
        savedMovement.setProduct(testProduct);
        savedMovement.setMovementType(MovementType.ENTRADA);
        savedMovement.setSaleValue(BigDecimal.valueOf(150));
        savedMovement.setMovementDate(java.time.LocalDateTime.parse("2025-05-31T12:00:00"));
        savedMovement.setQuantity(5);
        savedMovement.setDescription("Movimentação de teste");
        when(stockMovementService.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(testProduct));
        mockMvc.perform(post("/api/stock-movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"productId\": 1," +
                        "\"movementType\": \"ENTRADA\"," +
                        "\"saleValue\": 150.0," +
                        "\"quantity\": 5," +
                        "\"description\": \"Movimentação de teste\"" +
                        "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.description").value("Movimentação de teste"));
    }

    @Test
    void testGetProfitByProduct() throws Exception {
        when(stockMovementService.calculateProfit(1L)).thenReturn(BigDecimal.valueOf(250));
        mockMvc.perform(get("/api/stock-movements/profit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profit").value(250));
        verify(stockMovementService, times(1)).calculateProfit(1L);
    }
}
