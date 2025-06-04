package desafio.nexdom.desafio.controller;

import desafio.nexdom.desafio.dto.ProductRequest;
import desafio.nexdom.desafio.dto.ProductResponse;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.interfaces.IProductService;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(
    value = "/api/products",
    produces = { 
        MediaType.APPLICATION_JSON_VALUE,
        "application/hal+json"
    }
)
public class ProductController {
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    private final IProductService productService;


    @Autowired
    public ProductController(IProductService productService) {
        this.productService = productService;
    }

   
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @org.springframework.data.web.PageableDefault(size = 20, page = 0) Pageable pageable) {
        LOG.info("Buscando todos os produtos, página: {}", pageable);
        Page<Product> pageResult = productService.findAll(pageable);
        List<ProductResponse> responses = pageResult.stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id) {
        LOG.info("Buscando produto por ID: {}", id);
        Product product = productService.findById(id);
        ProductResponse response = ProductResponse.fromEntity(product);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest) {
        LOG.info("Criando novo produto: {}", productRequest);
        
        Product product = new Product();
        product.setCode(productRequest.getCode());
        product.setDescription(productRequest.getDescription());
        product.setType(productRequest.getType());
        product.setSupplierValue(productRequest.getSupplierValue());
        product.setStockQuantity(productRequest.getStockQuantity());
        
        Product savedProduct = productService.save(product);
        
        ProductResponse response = ProductResponse.fromEntity(savedProduct);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedProduct.getId())
                .toUri();
                
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        LOG.info("Produto criado com ID: {}", savedProduct.getId());
        
        return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
    }

  
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        LOG.info("Atualizando produto ID: {} Detalhes: {}", id, productRequest);
        
        Product existingProduct = productService.findById(id);
        
        existingProduct.setCode(productRequest.getCode());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setType(productRequest.getType());
        existingProduct.setSupplierValue(productRequest.getSupplierValue());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        
        Product updatedProduct = productService.save(existingProduct);
        
        ProductResponse response = ProductResponse.fromEntity(updatedProduct);
        
        LOG.info("Produto atualizado ID: {}", id);
        return ResponseEntity.ok(response);
    }

 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {
        LOG.info("Deletando produto ID: {}", id);
        productService.deleteById(id);
        LOG.info("Produto deletado ID: {}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Link", linkTo(methodOn(ProductController.class).getAllProducts(Pageable.unpaged())).withRel("products").toString());
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ProductResponse>> getProductsByType(
         @PathVariable String type,
        @org.springframework.data.web.PageableDefault(size = 20, page = 0) Pageable pageable) {
        LOG.info("Buscando produtos por tipo: {} página: {}", type, pageable);
        Page<Product> pageResult = productService.findByType(type, pageable);
        List<ProductResponse> responses = pageResult.stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
