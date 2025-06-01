package desafio.nexdom.desafio.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import desafio.nexdom.desafio.dto.StockMovementRequest;
import desafio.nexdom.desafio.exception.ProductNotFoundException;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import desafio.nexdom.desafio.exception.InsufficientStockException;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import jakarta.validation.Valid;

import java.net.URI;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/stock-movements")
@Tag(name = "Movimentações de Estoque", description = "API para gerenciamento de entradas e saídas de produtos no estoque")
public class StockMovementController {
    @Autowired
    private StockMovementService stockMovementService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Operation(summary = "Listar todas as movimentações", 
             description = """
             Retorna uma lista com todas as movimentações de estoque (entradas e saídas) formatadas com HATEOAS.
             
             ### Tipos de Movimentação:
             - **ENTRY**: Entrada de produtos no estoque (aumenta a quantidade)
             - **EXIT**: Saída de produtos do estoque (diminui a quantidade)
             
             ### Links HATEOAS Incluídos:
             - **self**: Link para o próprio recurso da movimentação
             - **stock-movements**: Link para a lista de movimentações
             - **product**: Link para o produto relacionado à movimentação
             
             A lista pode estar vazia se não houver movimentações registradas.
             """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operação bem-sucedida",
                    content = @Content(mediaType = "application/hal+json",
                    schema = @Schema(implementation = StockMovement.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<StockMovementModel>> getAllMovements(
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        List<StockMovementModel> allMovements = stockMovementService.findAll().stream()
            .map(StockMovementModel::fromStockMovement)
            .collect(Collectors.toList());
        int total = allMovements.size();
        int totalPages = (int) Math.ceil((double) total / size);
        page = Math.min(page, Math.max(0, totalPages - 1));
        int start = page * size;
        int end = Math.min(start + size, total);
        List<StockMovementModel> pageContent = allMovements.subList(start, end);
        CollectionModel<StockMovementModel> collectionModel = CollectionModel.of(pageContent);
        collectionModel.add(linkTo(methodOn(StockMovementController.class).getAllMovements(page, size)).withSelfRel());
        if (page > 0) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getAllMovements(0, size)).withRel("first"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getAllMovements(page - 1, size)).withRel("prev"));
        }
        if (end < total) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getAllMovements(page + 1, size)).withRel("next"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getAllMovements(totalPages - 1, size)).withRel("last"));
        }
        collectionModel.add(linkTo(methodOn(StockMovementController.class).createMovement(null)).withRel("create-movement").withType("POST"));
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(page));
        headers.add("X-Page-Size", String.valueOf(size));
        headers.add("X-Total-Elements", String.valueOf(total));
        headers.add("X-Total-Pages", String.valueOf(totalPages));
        return new ResponseEntity<>(collectionModel, headers, org.springframework.http.HttpStatus.OK);
    }

    @Operation(summary = "Criar nova movimentação", 
             description = """
             Cria uma nova movimentação de estoque (entrada ou saída) e retorna os dados salvos com links HATEOAS.
             
             ### Comportamento:
             - Para **ENTRY**: Aumenta o estoque do produto na quantidade especificada
             - Para **EXIT**: Reduz o estoque do produto na quantidade especificada
             - Valida se há estoque suficiente para saídas
             - Atualiza automaticamente o estoque do produto relacionado
             
             ### Exemplo de Requisição:
             ```json
             {
               "productId": 1,
               "type": "ENTRY",
               "quantity": 10,
               "description": "Entrada de estoque inicial"
             }
             ```
             
             ### Links HATEOAS Incluídos na Resposta:
             - **self**: Link para o recurso da movimentação criada
             - **stock-movements**: Link para a lista de movimentações
             - **product**: Link para o produto relacionado
             
             ### Códigos de Erro:
             - 400: Dados inválidos ou estoque insuficiente
             - 404: Produto não encontrado
             """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimentação criada com sucesso",
                    content = @Content(mediaType = "application/hal+json",
                    schema = @Schema(implementation = StockMovement.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou estoque insuficiente",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado - O produto informado não existe no sistema",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<?> createMovement(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da movimentação a ser criada", 
                required = true
            ) 
            @Valid @RequestBody StockMovementRequest request) {
        
        try {
            System.out.println("Received request: " + request);
            
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
            
            System.out.println("Found product: " + product);
            
            StockMovement stockMovement = new StockMovement();
            stockMovement.setProduct(product);
            stockMovement.setMovementType(request.getMovementType());
            stockMovement.setSaleValue(request.getSaleValue());
            stockMovement.setQuantity(request.getQuantity());
            stockMovement.setMovementDate(java.time.LocalDateTime.now());
            
            System.out.println("Created stock movement: " + stockMovement);
            
            StockMovement savedMovement = stockMovementRepository.save(stockMovement);
            
            if (savedMovement.getMovementType() == MovementType.ENTRADA) {
                product.setStockQuantity(product.getStockQuantity() + savedMovement.getQuantity());
            } else if (savedMovement.getMovementType() == MovementType.SAIDA) {
                if (product.getStockQuantity() < savedMovement.getQuantity()) {
                    throw new InsufficientStockException(
                        product.getCode(),
                        product.getStockQuantity(),
                        savedMovement.getQuantity()
                    );
                }
                product.setStockQuantity(product.getStockQuantity() - savedMovement.getQuantity());
            }
            
            productRepository.save(product);
            
            StockMovementModel model = StockMovementModel.fromStockMovement(savedMovement);
            
            model.add(linkTo(methodOn(StockMovementController.class)
                .getMovementsByProduct(savedMovement.getProduct().getId(), 0, 20))
                .withRel("stock-movements"));
            
            URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedMovement.getId())
                .toUri();
                
            return ResponseEntity.created(location).body(model);
            
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(404).body("{\"error\": \"Produto não encontrado com o ID: " + request.getProductId() + "\"}");
        } catch (InsufficientStockException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("{\"error\": \"Erro ao criar movimentação de estoque: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Buscar movimentações por produto", 
             description = """
             Retorna uma lista com todas as movimentações de um produto específico, formatadas com HATEOAS.
             
             ### Utilidade:
             - Rastrear o histórico completo de entradas e saídas de um produto
             - Verificar o saldo atual com base nas movimentações
             - Auditar as alterações de estoque ao longo do tempo
             
             ### Links HATEOAS Incluídos:
             - **self**: Link para o próprio recurso de busca por produto
             - **stock-movements**: Link para a lista completa de movimentações
             - **product**: Link para o produto relacionado
             - Para cada movimentação: Links para detalhes e para o produto relacionado
             
             A lista pode estar vazia se não houver movimentações para o produto.
             """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operação bem-sucedida",
                    content = @Content(mediaType = "application/hal+json",
                    schema = @Schema(implementation = StockMovement.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado - O ID fornecido não corresponde a nenhum produto cadastrado",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<CollectionModel<StockMovementModel>> getMovementsByProduct(
            @Parameter(description = "ID do produto", required = true) @PathVariable Long productId,
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        List<StockMovementModel> allMovements = stockMovementService.getMovementsByProduct(productId).stream()
            .map(StockMovementModel::fromStockMovement)
            .collect(Collectors.toList());
        int total = allMovements.size();
        int totalPages = (int) Math.ceil((double) total / size);
        page = Math.min(page, Math.max(0, totalPages - 1));
        int start = page * size;
        int end = Math.min(start + size, total);
        List<StockMovementModel> pageContent = allMovements.subList(start, end);
        CollectionModel<StockMovementModel> collectionModel = CollectionModel.of(pageContent);
        collectionModel.add(linkTo(methodOn(StockMovementController.class).getMovementsByProduct(productId, page, size)).withSelfRel());
        if (page > 0) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getMovementsByProduct(productId, 0, size)).withRel("first"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getMovementsByProduct(productId, page - 1, size)).withRel("prev"));
        }
        if (end < total) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getMovementsByProduct(productId, page + 1, size)).withRel("next"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class).getMovementsByProduct(productId, totalPages - 1, size)).withRel("last"));
        }
        collectionModel.add(linkTo(methodOn(StockMovementController.class).createMovement(null)).withRel("create-movement").withType("POST"));
        collectionModel.add(linkTo(methodOn(ProductController.class).getProductById(productId)).withRel("product"));
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(page));
        headers.add("X-Page-Size", String.valueOf(size));
        headers.add("X-Total-Elements", String.valueOf(total));
        headers.add("X-Total-Pages", String.valueOf(totalPages));
        return new ResponseEntity<>(collectionModel, headers, org.springframework.http.HttpStatus.OK);
    }

    @Operation(summary = "Calcular lucro por produto", 
             description = "Calcula o lucro total para um produto específico com base nas movimentações de estoque. O lucro é calculado subtraindo o custo total (valor do fornecedor multiplicado pela quantidade em estoque) da receita total (soma dos valores de venda de todas as saídas).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cálculo realizado com sucesso",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado - O ID fornecido não corresponde a nenhum produto cadastrado",
                    content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @GetMapping("/profit/{productId}")
    public ResponseEntity<Object> getProfitByProduct(
            @Parameter(description = "ID do produto", required = true) @PathVariable Long productId) {
        BigDecimal profit = stockMovementService.calculateProfit(productId);
        
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("profit", profit);
        
        ObjectNode linksNode = JsonNodeFactory.instance.objectNode();
        
        ObjectNode selfNode = JsonNodeFactory.instance.objectNode();
        selfNode.put("href", linkTo(methodOn(StockMovementController.class).getProfitByProduct(productId)).toString());
        linksNode.set("self", selfNode);
        
        ObjectNode productNode = JsonNodeFactory.instance.objectNode();
        productNode.put("href", linkTo(methodOn(ProductController.class).getProductById(productId)).toString());
        linksNode.set("product", productNode);
        
        ObjectNode stockMovementsNode = JsonNodeFactory.instance.objectNode();
        stockMovementsNode.put("href", linkTo(methodOn(StockMovementController.class).getMovementsByProduct(productId, 0, 20)).toString());
        linksNode.set("stock-movements", stockMovementsNode);
        
        response.set("_links", linksNode);
        
        return ResponseEntity.ok(response);
    }
}
