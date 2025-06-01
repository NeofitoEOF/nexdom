package desafio.nexdom.desafio.controller;

import desafio.nexdom.desafio.exception.ProductNotFoundException;
import desafio.nexdom.desafio.hateoas.ProductModel;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.service.ProductService;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(
    name = "Produtos",
    description = """
        API para gerenciamento de produtos no sistema de estoque.
        Fornece operações CRUD completas para produtos, com suporte a HATEOAS.
        """
)
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
        summary = "Listar todos os produtos",
        description = """
            Retorna uma lista paginada de todos os produtos cadastrados no sistema,
            formatados com HATEOAS para navegação relacionada.
            
            ### Links HATEOAS Incluídos:
            - **self**: Link para a própria coleção de produtos
            - **create**: Link para criar um novo produto (POST)
            - **by-type/{type}**: Link para buscar produtos por tipo
            - **{id}**: Links individuais para cada produto
            
            ### Exemplo de Resposta:
            ```json
            {
                "_embedded": {
                    "products": [
                        {
                            "id": 1,
                            "code": "ELEC-001",
                            "description": "Smartphone Samsung Galaxy S22",
                            "type": "ELECTRONIC",
                            "supplierValue": 1500.00,
                            "stockQuantity": 10,
                            "_links": {
                                "self": { "href": "/api/products/1", "type": "GET" },
                                "update": { "href": "/api/products/1", "type": "PUT" },
                                "delete": { "href": "/api/products/1", "type": "DELETE" },
                                "stock-movements": { 
                                    "href": "/api/stock-movements/by-product/1",
                                    "type": "GET"
                                },
                                "profit": {
                                    "href": "/api/stock-movements/profit/1",
                                    "type": "GET"
                                }
                            }
                        }
                    ]
                },
                "_links": {
                    "self": { "href": "/api/products", "type": "GET" },
                    "create": { "href": "/api/products", "type": "POST" },
                    "by-type": {
                        "href": "/api/products/type/{type}",
                        "templated": true
                    }
                },
                "page": {
                    "size": 20,
                    "totalElements": 1,
                    "totalPages": 1,
                    "number": 0
                }
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de produtos recuperada com sucesso",
            content = @Content(
                mediaType = "application/hal+json",
                schema = @Schema(implementation = ProductModel.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "_embedded": {
                            "products": [
                                {
                                    "id": 1,
                                    "code": "ELEC-001",
                                    "description": "Smartphone Samsung Galaxy S22",
                                    "type": "ELECTRONIC",
                                    "supplierValue": 1500.00,
                                    "stockQuantity": 10,
                                    "_links": {
                                        "self": { "href": "/api/products/1", "type": "GET" },
                                        "update": { "href": "/api/products/1", "type": "PUT" },
                                        "delete": { "href": "/api/products/1", "type": "DELETE" },
                                        "stock-movements": { "href": "/api/stock-movements/by-product/1", "type": "GET" },
                                        "profit": { "href": "/api/stock-movements/profit/1", "type": "GET" }
                                    }
                                }
                            ]
                        },
                        "_links": {
                            "self": { "href": "/api/products", "type": "GET" },
                            "create": { "href": "/api/products", "type": "POST" },
                            "by-type": { "href": "/api/products/type/{type}", "templated": true }
                        },
                        "page": { "size": 20, "totalElements": 1, "totalPages": 1, "number": 0 }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(mediaType = "application/problem+json")
        )
    })
    @GetMapping
    public ResponseEntity<CollectionModel<ProductModel>> getAllProducts(
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        if (page < 0) {
            throw new IllegalArgumentException("O número da página não pode ser negativo");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("O tamanho da página deve ser maior que zero");
        }
        
        List<Product> products = productService.findAll();
        
        int totalProducts = products.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        
        page = Math.min(page, Math.max(0, totalPages - 1));
        
        int start = page * size;
        int end = Math.min(start + size, totalProducts);
        
        List<Product> pageContent = products.subList(start, end);
        
        List<ProductModel> productModels = pageContent.stream()
                .map(ProductModel::fromProduct)
                .collect(Collectors.toList());
                
        CollectionModel<ProductModel> collectionModel = CollectionModel.of(productModels,
                linkTo(methodOn(ProductController.class).getAllProducts(page, size)).withSelfRel());
        
        
        if (page > 0) {
            collectionModel.add(linkTo(methodOn(ProductController.class)
                    .getAllProducts(0, size))
                    .withRel(IanaLinkRelations.FIRST));
        }
        
        if (page > 0) {
            collectionModel.add(linkTo(methodOn(ProductController.class)
                    .getAllProducts(page - 1, size))
                    .withRel(IanaLinkRelations.PREV));
        }
        
        if (end < totalProducts) {
            collectionModel.add(linkTo(methodOn(ProductController.class)
                    .getAllProducts(page + 1, size))
                    .withRel(IanaLinkRelations.NEXT));
        }
        
        if (end < totalProducts) {
            collectionModel.add(linkTo(methodOn(ProductController.class)
                    .getAllProducts(totalPages - 1, size))
                    .withRel(IanaLinkRelations.LAST));
        }
        
        collectionModel.add(WebMvcLinkBuilder.linkTo(ProductController.class)
                .withRel("create").withType("POST"));
        
        collectionModel.add(linkTo(methodOn(ProductController.class)
                .getProductsByType("ELECTRONIC", 0, size))
                .withRel("by-type").withType("GET").withTitle("ELECTRONIC"));
                
        collectionModel.add(linkTo(methodOn(ProductController.class)
                .getProductsByType("APPLIANCE", 0, size))
                .withRel("by-type").withType("GET").withTitle("APPLIANCE"));
                
        collectionModel.add(linkTo(methodOn(ProductController.class)
                .getProductsByType("FURNITURE", 0, size))
                .withRel("by-type").withType("GET").withTitle("FURNITURE"));
                
        collectionModel.add(linkTo(methodOn(ProductController.class)
                .getProductsByType("BOOK", 0, size))
                .withRel("by-type").withType("GET").withTitle("BOOK"));
        

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(page));
        headers.add("X-Page-Size", String.valueOf(size));
        headers.add("X-Total-Elements", String.valueOf(totalProducts));
        headers.add("X-Total-Pages", String.valueOf(totalPages));
        
        return new ResponseEntity<>(collectionModel, headers, HttpStatus.OK);
    }

    @Operation(
        summary = "Buscar produto por ID",
        description = """
            Busca um produto específico com base no ID fornecido, retornando os detalhes formatados com HATEOAS.
            
            ### Links HATEOAS Incluídos:
            - **self**: Link para o próprio recurso do produto (GET)
            - **update**: Link para atualizar o produto (PUT)
            - **delete**: Link para excluir o produto (DELETE)
            - **products**: Link para a lista de produtos (GET)
            - **stock-movements**: Link para as movimentações de estoque do produto (GET)
            - **profit**: Link para calcular o lucro do produto (GET)
            - **by-type**: Link para buscar produtos do mesmo tipo (GET, templated)
            
            ### Exemplo de Resposta:
            ```json
            {
                "id": 1,
                "code": "ELEC-001",
                "description": "Smartphone Samsung Galaxy S22",
                "type": "ELECTRONIC",
                "supplierValue": 1500.00,
                "stockQuantity": 10,
                "_links": {
                    "self": { "href": "/api/products/1", "type": "GET" },
                    "update": { "href": "/api/products/1", "type": "PUT" },
                    "delete": { "href": "/api/products/1", "type": "DELETE" },
                    "products": { "href": "/api/products", "type": "GET" },
                    "stock-movements": { "href": "/api/stock-movements/by-product/1", "type": "GET" },
                    "profit": { "href": "/api/stock-movements/profit/1", "type": "GET" },
                    "by-type": { "href": "/api/products/type/ELECTRONIC", "type": "GET" }
                }
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado com sucesso",
            content = @Content(
                mediaType = "application/hal+json",
                schema = @Schema(implementation = ProductModel.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "id": 1,
                        "code": "ELEC-001",
                        "description": "Smartphone Samsung Galaxy S22",
                        "type": "ELECTRONIC",
                        "supplierValue": 1500.00,
                        "stockQuantity": 10,
                        "_links": {
                            "self": { "href": "/api/products/1", "type": "GET" },
                            "update": { "href": "/api/products/1", "type": "PUT" },
                            "delete": { "href": "/api/products/1", "type": "DELETE" },
                            "products": { "href": "/api/products", "type": "GET" },
                            "stock-movements": { "href": "/api/stock-movements/by-product/1", "type": "GET" },
                            "profit": { "href": "/api/stock-movements/profit/1", "type": "GET" },
                            "by-type": { "href": "/api/products/type/ELECTRONIC", "type": "GET" }
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado - O ID fornecido não corresponde a nenhum produto cadastrado",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/not-found",
                        "title": "Recurso não encontrado",
                        "status": 404,
                        "detail": "Produto com ID 999 não encontrado",
                        "instance": "/api/products/999"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(mediaType = "application/problem+json")
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> getProductById(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long id) {
                
        Product product = productService.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
                
        ProductModel model = ProductModel.fromProduct(product)
                .addProductsByTypeLink(product.getType())
                .addCreateStockMovementLink();
        
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Criar novo produto",
        description = """
            Cria um novo produto no sistema e retorna os dados salvos incluindo o ID gerado, 
            juntamente com links HATEOAS para navegação relacionada.
            
            ### Links HATEOAS Incluídos na Resposta:
            - **self**: Link para o recurso do produto criado (GET)
            - **update**: Link para atualizar o produto (PUT)
            - **delete**: Link para excluir o produto (DELETE)
            - **products**: Link para a lista de produtos (GET)
            - **stock-movements**: Link para as movimentações de estoque do produto (GET)
            - **profit**: Link para calcular o lucro do produto (GET)
            - **by-type**: Link para buscar produtos do mesmo tipo (GET)
            
            ### Códigos de Resposta:
            - 201: Produto criado com sucesso
            - 400: Dados inválidos
            - 409: Código de produto já existe
            - 500: Erro interno do servidor
            
            ### Exemplo de Requisição:
            ```json
            {
                "code": "ELEC-001",
                "description": "Smartphone Samsung Galaxy S22",
                "type": "ELECTRONIC",
                "supplierValue": 1500.00,
                "stockQuantity": 10
            }
            ```
            
            ### Exemplo de Resposta (201 Created):
            ```json
            {
                "id": 1,
                "code": "ELEC-001",
                "description": "Smartphone Samsung Galaxy S22",
                "type": "ELECTRONIC",
                "supplierValue": 1500.00,
                "stockQuantity": 10,
                "_links": {
                    "self": { "href": "/api/products/1", "type": "GET" },
                    "update": { "href": "/api/products/1", "type": "PUT" },
                    "delete": { "href": "/api/products/1", "type": "DELETE" },
                    "products": { "href": "/api/products", "type": "GET" },
                    "stock-movements": { "href": "/api/stock-movements/by-product/1", "type": "GET" },
                    "profit": { "href": "/api/stock-movements/profit/1", "type": "GET" },
                    "by-type": { "href": "/api/products/type/ELECTRONIC", "type": "GET" }
                }
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Produto criado com sucesso",
            content = @Content(
                mediaType = "application/hal+json",
                schema = @Schema(implementation = ProductModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos - Verifique se todos os campos obrigatórios foram preenchidos corretamente",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/bad-request",
                        "title": "Requisição inválida",
                        "status": 400,
                        "detail": "O campo 'description' é obrigatório",
                        "instance": "/api/products"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito - O código do produto já existe no sistema",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/conflict",
                        "title": "Conflito",
                        "status": 409,
                        "detail": "Já existe um produto com o código 'ELEC-001'",
                        "instance": "/api/products"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(mediaType = "application/problem+json")
        )
    })
    @PostMapping
    public ResponseEntity<ProductModel> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do produto a ser criado",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Product.class),
                    examples = @ExampleObject(
                        name = "Exemplo de criação de produto",
                        value = """
                        {
                            "code": "ELEC-001",
                            "description": "Smartphone Samsung Galaxy S22",
                            "type": "ELECTRONIC",
                            "supplierValue": 1500.00,
                            "stockQuantity": 10
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody Product product) {
                
        Product savedProduct = productService.save(product);
        
        ProductModel model = ProductModel.fromProduct(savedProduct)
                .addProductsByTypeLink(savedProduct.getType())
                .addCreateStockMovementLink();
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedProduct.getId())
                .toUri();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        
        Link productsLink = WebMvcLinkBuilder.linkTo(ProductController.class)
                .withRel("products")
                .withType("GET");
        headers.add("Link", String.format("<%s>; rel=\"%s\"; type=\"%s\"", 
            productsLink.getHref(), 
            productsLink.getRel().value(),
            "GET"));
        
        return new ResponseEntity<>(model, headers, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar produto existente",
        description = """
            Atualiza um produto existente com base no ID fornecido. 
            
            ### Comportamento:
            - Apenas os campos fornecidos serão atualizados
            - Campos não fornecidos permanecerão inalterados
            - O campo ID não pode ser alterado
            
            ### Exemplo de Requisição:
            ```json
            {
                "code": "ELEC-001-UPDATED",
                "description": "Smartphone Samsung Galaxy S22 (Atualizado)",
                "type": "ELECTRONIC",
                "supplierValue": 1600.00,
                "stockQuantity": 15
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto atualizado com sucesso",
            content = @Content(
                mediaType = "application/hal+json",
                schema = @Schema(implementation = ProductModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/bad-request",
                        "title": "Requisição inválida",
                        "status": 400,
                        "detail": "O campo 'description' é obrigatório",
                        "instance": "/api/products/1"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/not-found",
                        "title": "Recurso não encontrado",
                        "status": 404,
                        "detail": "Produto com ID 999 não encontrado",
                        "instance": "/api/products/999"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito - Código já existe",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/conflict",
                        "title": "Conflito",
                        "status": 409,
                        "detail": "Já existe um produto com o código 'ELEC-001'",
                        "instance": "/api/products/1"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(mediaType = "application/problem+json")
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> updateProduct(
            @Parameter(description = "ID do produto a ser atualizado", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados atualizados do produto",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Product.class),
                    examples = @ExampleObject(
                        name = "Exemplo de atualização de produto",
                        value = """
                        {
                            "code": "ELEC-001-UPDATED",
                            "description": "Smartphone Samsung Galaxy S22 (Atualizado)",
                            "type": "ELECTRONIC",
                            "supplierValue": 1600.00,
                            "stockQuantity": 15
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody Product productDetails) {
                
        productDetails.setId(id);
        Product updatedProduct = productService.save(productDetails);
        
        ProductModel model = ProductModel.fromProduct(updatedProduct)
                .addProductsByTypeLink(updatedProduct.getType())
                .addCreateStockMovementLink();
        
        HttpHeaders headers = new HttpHeaders();
        
        Link productsLink = WebMvcLinkBuilder.linkTo(ProductController.class)
                .withRel("products")
                .withType("GET");
        headers.add("Link", String.format("<%s>; rel=\"%s\"; type=\"%s\"", 
            productsLink.getHref(), 
            productsLink.getRel().value(),
            "GET"));
        
        return new ResponseEntity<>(model, headers, HttpStatus.OK);
    }

    /**
     * Exclui um produto existente.
     *
     * @param id ID do produto a ser excluído
     */
    @Operation(
        summary = "Excluir produto",
        description = """
            Remove um produto do sistema com base no ID fornecido.
            
            ### Comportamento:
            - Remove o produto do banco de dados
            - Qualquer tentativa de acessar o produto após a exclusão resultará em erro 404
            - As movimentações de estoque associadas ao produto não são afetadas
            
            ### Resposta:
            - Código 204 (No Content) em caso de sucesso
            - Inclui um cabeçalho 'Link' com referência à lista de produtos
            """)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Produto excluído com sucesso",
            content = @Content,
            headers = @io.swagger.v3.oas.annotations.headers.Header(
                name = "Link", 
                description = "Link para a lista de produtos", 
                schema = @Schema(type = "string")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(
                    value = """
                    {
                        "type": "https://api.desafionexdom.com/errors/not-found",
                        "title": "Recurso não encontrado",
                        "status": 404,
                        "detail": "Produto com ID 999 não encontrado",
                        "instance": "/api/products/999"
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto a ser excluído", required = true, example = "1")
            @PathVariable Long id) {
        
        if (!productService.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        
        productService.deleteById(id);
        
        HttpHeaders headers = new HttpHeaders();
        
        Link productsLink = WebMvcLinkBuilder.linkTo(ProductController.class)
                .withRel("products")
                .withType("GET");
        headers.add("Link", String.format("<%s>; rel=\"%s\"; type=\"%s\"", 
            productsLink.getHref(), 
            productsLink.getRel().value(),
            "GET"));
        
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Buscar produtos por tipo",
        description = """
            Retorna uma lista paginada de produtos de um tipo específico, formatados com HATEOAS para navegação relacionada.
            
            ### Tipos Válidos:
            - `ELETRONIC`: Eletrônicos
            - `APPLIANCE`: Eletrodomésticos
            - `FURNITURE`: Móveis
            - `BOOK`: Livros
            - Ou qualquer outro tipo personalizado
            
            A lista pode estar vazia se não houver produtos do tipo especificado.
            
            ### Links HATEOAS Incluídos:
            - **self**: Link para o próprio recurso de busca por tipo
            - **products**: Link para a lista completa de produtos
            - Para cada produto: Links para detalhes, atualização e exclusão
            """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operação bem-sucedida",
                    content = @Content(mediaType = "application/hal+json", 
                    schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<CollectionModel<ProductModel>> getProductsByType(
            @Parameter(description = "Tipo do produto", required = true) @PathVariable String type,
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        List<Product> products = productService.findByType(type);
        int totalProducts = products.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);
        page = Math.min(page, Math.max(0, totalPages - 1));
        int start = page * size;
        int end = Math.min(start + size, totalProducts);
        List<Product> pageContent = products.subList(start, end);
        List<ProductModel> productModels = pageContent.stream()
                .map(ProductModel::fromProduct)
                .collect(Collectors.toList());
        CollectionModel<ProductModel> collectionModel = CollectionModel.of(productModels,
                linkTo(methodOn(ProductController.class).getProductsByType(type, page, size)).withSelfRel());
        if (page > 0) {
            collectionModel.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ProductController.class).getProductsByType(type, 0, size))
                    .withRel(IanaLinkRelations.FIRST));
            collectionModel.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ProductController.class).getProductsByType(type, page - 1, size))
                    .withRel(IanaLinkRelations.PREV));
        }
        if (end < totalProducts) {
            collectionModel.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ProductController.class).getProductsByType(type, page + 1, size))
                    .withRel(IanaLinkRelations.NEXT));
            collectionModel.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(ProductController.class).getProductsByType(type, totalPages - 1, size))
                    .withRel(IanaLinkRelations.LAST));
        }
        collectionModel.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).getAllProducts(0, size))
                .withRel("all-products"));
        collectionModel.add(WebMvcLinkBuilder.linkTo(ProductController.class)
                .withRel("create-product").withType("POST"));
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(page));
        headers.add("X-Page-Size", String.valueOf(size));
        headers.add("X-Total-Elements", String.valueOf(totalProducts));
        headers.add("X-Total-Pages", String.valueOf(totalPages));
        return new ResponseEntity<>(collectionModel, headers, HttpStatus.OK);
    }
}
