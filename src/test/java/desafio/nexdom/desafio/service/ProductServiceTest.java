package desafio.nexdom.desafio.service;

import desafio.nexdom.desafio.model.Product;
import org.springframework.data.domain.Pageable;

import desafio.nexdom.desafio.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setCode("TEST-001");
        testProduct.setDescription("Test Product");
        testProduct.setType("ELECTRONIC");
        testProduct.setSupplierValue(BigDecimal.valueOf(100));
        testProduct.setStockQuantity(10);
    }

    @Test
    void testSaveNewProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product savedProduct = productService.save(testProduct);

        assertNotNull(savedProduct);
        assertEquals(testProduct.getCode(), savedProduct.getCode());
        assertEquals(testProduct.getDescription(), savedProduct.getDescription());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testFindProductById() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        Product foundProduct = productService.findById(1L);

        assertNotNull(foundProduct);
        assertEquals(testProduct, foundProduct);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteProduct() {
        productService.deleteById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindProductsByType() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testProduct)));

        var products = productService.findByType("ELECTRONIC", Pageable.unpaged());

        assertTrue(products.getContent().size() > 0);
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }
}
