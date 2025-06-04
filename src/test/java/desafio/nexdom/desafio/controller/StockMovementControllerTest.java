package desafio.nexdom.desafio.controller;

import desafio.nexdom.desafio.dto.CreateStockMovementResponse;
import desafio.nexdom.desafio.dto.ProfitResultDto;
import desafio.nexdom.desafio.dto.StockMovementRequest;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMovementController.class)
class StockMovementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IStockMovementService stockMovementService;
    
    @MockBean
    private desafio.nexdom.desafio.repository.ProductRepository productRepository;
    
    @MockBean
    private desafio.nexdom.desafio.repository.StockMovementRepository stockMovementRepository;
    
    @MockBean
    private desafio.nexdom.desafio.hateoas.HateoasResponseAssembler hateoasAssembler;

    private Product testProduct;
    private StockMovement testMovement;
    private StockMovementRequest testMovementRequest;
    private StockMovementModel testMovementModel;

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
        testMovement.setId(1L);
        testMovement.setProduct(testProduct);
        testMovement.setMovementType(MovementType.ENTRADA);
        testMovement.setSaleValue(BigDecimal.valueOf(150));
        testMovement.setMovementDate(LocalDateTime.now());
        testMovement.setQuantity(5);
        testMovement.setDescription("Movimentação de teste");
        
        testMovementRequest = new StockMovementRequest();
        testMovementRequest.setProductId(1L);
        testMovementRequest.setMovementType(MovementType.ENTRADA);
        testMovementRequest.setSaleValue(BigDecimal.valueOf(150));
        testMovementRequest.setQuantity(5);
        testMovementRequest.setDescription("Movimentação de teste");
        
        testMovementModel = StockMovementModel.fromStockMovement(testMovement);
    }

    @Test
    void testGetAllMovements() throws Exception {
        when(stockMovementService.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testMovement)));
            
        mockMvc.perform(get("/api/stock-movements?page=0&size=20"))
                .andExpect(status().isOk());
                
        verify(stockMovementService, times(1)).findAll(any(Pageable.class));
    }
    
    @Test
    void testGetMovementsByProduct() throws Exception {
        Page<StockMovement> page = new PageImpl<>(List.of(testMovement));
        when(stockMovementService.getMovementsByProduct(anyLong(), any(Pageable.class)))
            .thenReturn(page);
        when(hateoasAssembler.createPaginatedResponseFromPage(
            any(), any(), anyLong(), anyInt(), anyInt()))
            .thenReturn(ResponseEntity.ok().body(CollectionModel.of(List.of(testMovementModel))));
            
        mockMvc.perform(get("/api/stock-movements/by-product/1?page=0&size=20"))
                .andExpect(status().isOk());
                
        verify(stockMovementService, times(1)).getMovementsByProduct(anyLong(), any(Pageable.class));
    }

    @Test
    void testCreateMovement() throws Exception {
        StockMovement savedMovement = new StockMovement();
        savedMovement.setId(77L);
        savedMovement.setProduct(testProduct);
        savedMovement.setMovementType(MovementType.ENTRADA);
        savedMovement.setSaleValue(BigDecimal.valueOf(150));
        savedMovement.setMovementDate(java.time.LocalDateTime.parse("2025-05-31T12:00:00"));
        savedMovement.setQuantity(5);
        savedMovement.setDescription("Movimentação detalhada");
        
        CreateStockMovementResponse response = new CreateStockMovementResponse();
        response.setMovement(StockMovementModel.fromStockMovement(savedMovement));
        response.setMessage("Movimentação criada com sucesso");
        
        when(stockMovementService.createStockMovement(any(StockMovementRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/stock-movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMovementRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.message").value("Movimentação criada com sucesso"));
                
        verify(stockMovementService, times(1)).createStockMovement(any(StockMovementRequest.class));
    }
    
    @Test
    void testGetMovementById() throws Exception {
        when(stockMovementService.getMovementModelById(anyLong()))
            .thenReturn(testMovementModel);
            
        mockMvc.perform(get("/api/stock-movements/1"))
                .andExpect(status().isOk());
                
        verify(stockMovementService, times(1)).getMovementModelById(1L);
    }

    @Test
    void testUpdateMovement() throws Exception {
        when(stockMovementService.updateMovement(anyLong(), any(StockMovementRequest.class)))
            .thenReturn(testMovementModel);
            
        mockMvc.perform(put("/api/stock-movements/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMovementRequest)))
                .andExpect(status().isOk());
                
        verify(stockMovementService, times(1)).updateMovement(anyLong(), any(StockMovementRequest.class));
    }

    @Test
    void testGetProfitByProduct() throws Exception {
        ProfitResultDto profitResult = new ProfitResultDto(BigDecimal.valueOf(250), 10);
        
        when(stockMovementService.calculateProfitAndTotalSold(1L)).thenReturn(profitResult);
        
        ObjectNode responseNode = objectMapper.createObjectNode();
        responseNode.put("profit", 250);
        responseNode.put("totalSold", 10);
        
        when(hateoasAssembler.createProfitResponse(any(ProfitResultDto.class), anyLong()))
            .thenReturn(ResponseEntity.ok().body(responseNode));
            
        mockMvc.perform(get("/api/stock-movements/profit/1"))
                .andExpect(status().isOk());
                
        verify(stockMovementService, times(1)).calculateProfitAndTotalSold(1L);
    }
}
