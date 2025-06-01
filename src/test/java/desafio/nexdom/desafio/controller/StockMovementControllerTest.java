package desafio.nexdom.desafio.controller;

import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.service.StockMovementService;
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
    void testGetAllMovements() throws Exception {
        when(stockMovementService.findAll()).thenReturn(List.of(testMovement));
        mockMvc.perform(get("/api/stock-movements"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "20"))
                .andExpect(header().string("X-Total-Elements", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.create-movement").exists());
        verify(stockMovementService, times(1)).findAll();
    }
    
    @Test
    void testGetMovementsByProduct() throws Exception {
        when(stockMovementService.getMovementsByProduct(1L)).thenReturn(List.of(testMovement));
        mockMvc.perform(get("/api/stock-movements/by-product/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "20"))
                .andExpect(header().string("X-Total-Elements", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.create-movement").exists())
                .andExpect(jsonPath("$._links.product").exists());
        verify(stockMovementService, times(1)).getMovementsByProduct(1L);
    }

    @Test
    void testCreateMovement() throws Exception {
        when(stockMovementService.save(any(StockMovement.class))).thenReturn(testMovement);
        mockMvc.perform(post("/api/stock-movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"product\": {\"id\": 1}," +
                        "\"movementType\": \"ENTRADA\"," +
                        "\"saleValue\": 150.0," +
                        "\"movementDate\": \"2025-05-31T12:00:00\"," +
                        "\"quantity\": 5" +
                        "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$._links.self").exists());
        verify(stockMovementService, times(1)).save(any(StockMovement.class));
    }

    @Test
    void testGetProfitByProduct() throws Exception {
        when(stockMovementService.calculateProfit(1L)).thenReturn(BigDecimal.valueOf(250));
        mockMvc.perform(get("/api/stock-movements/profit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profit").value(250))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.product").exists())
                .andExpect(jsonPath("$._links.stock-movements").exists());
        verify(stockMovementService, times(1)).calculateProfit(1L);
    }
}
