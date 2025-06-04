package desafio.nexdom.desafio.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import desafio.nexdom.desafio.dto.ProductRequest;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IProductService productService;

    @MockBean
    private IStockMovementService stockMovementService;
    
    private Product testProduct;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCode("TEST-001");
        testProduct.setDescription("Test Product");
        testProduct.setType("ELECTRONIC");
        testProduct.setSupplierValue(BigDecimal.valueOf(100));
        testProduct.setStockQuantity(10);
        
        testProductRequest = new ProductRequest();
        testProductRequest.setCode("TEST-001");
        testProductRequest.setDescription("Test Product");
        testProductRequest.setType("ELECTRONIC");
        testProductRequest.setSupplierValue(BigDecimal.valueOf(100));
        testProductRequest.setStockQuantity(10);
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testProduct)));
            
        mockMvc.perform(get("/api/products?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value(testProduct.getCode()));
                
        verify(productService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.findById(anyLong())).thenReturn(testProduct);
        
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testProduct.getCode()));
                
        verify(productService, times(1)).findById(1L);
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.save(any(Product.class))).thenReturn(testProduct);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value(testProduct.getCode()));
                
        verify(productService, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() throws Exception {
        when(productService.findById(anyLong())).thenReturn(testProduct);
        when(productService.save(any(Product.class))).thenReturn(testProduct);
        
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testProduct.getCode()));
                
        verify(productService, times(1)).findById(1L);
        verify(productService, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteById(1L);
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Link"));
        verify(productService, times(1)).deleteById(1L);
    }

    @Test
    void testGetProductsByType() throws Exception {
        when(productService.findByType(any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testProduct)));
            
        mockMvc.perform(get("/api/products/type/ELECTRONIC?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value(testProduct.getCode()));
                
        verify(productService, times(1)).findByType(any(), any(Pageable.class));
    }
}
