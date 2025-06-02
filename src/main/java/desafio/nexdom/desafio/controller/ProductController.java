package desafio.nexdom.desafio.controller;

import desafio.nexdom.desafio.exception.ProductNotFoundException;
import desafio.nexdom.desafio.hateoas.ProductModel;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.interfaces.IProductService;
import java.net.URI;
import java.util.*;
import jakarta.validation.Valid;
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
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProductController.class);

    private final IProductService productService;


    @Autowired
    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ProductModel>> getAllProducts(
            @org.springframework.data.web.PageableDefault(size = 20, page = 0) Pageable pageable) {
        LOG.info("Buscando todos os produtos, página: {}", pageable);
        Page<Product> pageResult = productService.findAll(pageable);
        List<ProductModel> models = pageResult.stream()
            .map(ProductModel::fromProduct)
            .collect(java.util.stream.Collectors.toList());
        return new ResponseEntity<>(models, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> getProductById(
            @PathVariable Long id) {
        LOG.info("Buscando produto por ID: {}", id);
        Product product = productService.findById(id);
        ProductModel model = ProductModel.fromProduct(product);
        return ResponseEntity.ok(model);
    }

    @PostMapping
    public ResponseEntity<ProductModel> createProduct(
            @Valid @RequestBody Product product) {
        LOG.info("Criando novo produto: {}", product);
        Product savedProduct = productService.save(product);
        ProductModel model = ProductModel.fromProduct(savedProduct);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedProduct.getId())
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        LOG.info("Produto criado com ID: {}", savedProduct.getId());
        return new ResponseEntity<>(model, headers, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product productDetails) {
        LOG.info("Atualizando produto ID: {} Detalhes: {}", id, productDetails);
        productDetails.setId(id);
        Product updatedProduct = productService.save(productDetails);
        ProductModel model = ProductModel.fromProduct(updatedProduct);
        LOG.info("Produto atualizado ID: {}", id);
        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {
        LOG.info("Deletando produto ID: {}", id);
        if (productService.findById(id) == null) {
            LOG.warn("Tentativa de deletar produto inexistente ID: {}", id);
            throw new ProductNotFoundException(id);
        }
        productService.deleteById(id);
        LOG.info("Produto deletado ID: {}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Link", linkTo(methodOn(ProductController.class).getAllProducts(Pageable.unpaged())).withRel("products").toString());
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ProductModel>> getProductsByType(
         @PathVariable String type,
        @org.springframework.data.web.PageableDefault(size = 20, page = 0) Pageable pageable) {
        LOG.info("Buscando produtos por tipo: {} página: {}", type, pageable);
        Page<Product> pageResult = productService.findByType(type, pageable);
        List<ProductModel> models = pageResult.stream()
            .map(ProductModel::fromProduct)
            .collect(java.util.stream.Collectors.toList());
        return new ResponseEntity<>(models, org.springframework.http.HttpStatus.OK);
    }
}
