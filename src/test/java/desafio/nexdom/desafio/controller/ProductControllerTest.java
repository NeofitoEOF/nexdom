package desafio.nexdom.desafio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import desafio.nexdom.desafio.hateoas.ProductModel;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private ProductService productService;
    
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCode("TEST-001");
        testProduct.setDescription("Test Product");
        testProduct.setType("ELECTRONIC");
        testProduct.setSupplierValue(BigDecimal.valueOf(100));
        testProduct.setStockQuantity(10);
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(testProduct));
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "20"))
                .andExpect(header().string("X-Total-Elements", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.create-product").exists())
                .andExpect(jsonPath("$._embedded.productModelList[0].code").value(testProduct.getCode()));
        verify(productService, times(1)).findAll();
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.of(testProduct));
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testProduct.getCode()))
                .andExpect(jsonPath("$._links.self").exists());
        verify(productService, times(1)).findById(1L);
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.save(any(Product.class))).thenReturn(testProduct);
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value(testProduct.getCode()))
                .andExpect(jsonPath("$._links.self").exists());
        verify(productService, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() throws Exception {
        when(productService.save(any(Product.class))).thenReturn(testProduct);
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testProduct.getCode()))
                .andExpect(jsonPath("$._links.self").exists());
        verify(productService, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() throws Exception {
        when(productService.existsById(1L)).thenReturn(true);
        doNothing().when(productService).deleteById(1L);
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Link"));
        verify(productService, times(1)).existsById(1L);
        verify(productService, times(1)).deleteById(1L);
    }

    @Test
    void testGetProductsByType() throws Exception {
        when(productService.findByType(any())).thenReturn(List.of(testProduct));
        mockMvc.perform(get("/api/products/type/ELECTRONIC"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(header().string("X-Page-Size", "20"))
                .andExpect(header().string("X-Total-Elements", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.create-product").exists())
                .andExpect(jsonPath("$._embedded.productModelList[0].code").value(testProduct.getCode()));
        verify(productService, times(1)).findByType(any());
    }
}
