package desafio.nexdom.desafio.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import desafio.nexdom.desafio.dto.StockMovementRequest;
import desafio.nexdom.desafio.exception.ProductNotFoundException;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.beans.factory.annotation.Autowired;
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
public class StockMovementController {

    @Autowired
    private desafio.nexdom.desafio.service.StockMovementService stockMovementServiceCustom;

  
    @GetMapping("/{id}")
    public ResponseEntity<StockMovementModel> getMovementById(@PathVariable Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new desafio.nexdom.desafio.exception.ProductNotFoundException(id));
        StockMovementModel model = StockMovementModel.fromStockMovement(movement);
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockMovementModel> updateMovement(@PathVariable Long id, @Valid @RequestBody desafio.nexdom.desafio.dto.StockMovementRequest request) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new desafio.nexdom.desafio.exception.ProductNotFoundException(id));
        movement.setMovementType(request.getMovementType());
        movement.setSaleValue(request.getSaleValue());
        movement.setQuantity(request.getQuantity());
        movement.setDescription(request.getDescription());
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new desafio.nexdom.desafio.exception.ProductNotFoundException(request.getProductId()));
            movement.setProduct(product);
        }
        StockMovement updated = stockMovementRepository.save(movement);
        StockMovementModel model = StockMovementModel.fromStockMovement(updated);
        return ResponseEntity.ok(model);
    }
    private final IStockMovementService stockMovementService;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockMovementController(IStockMovementService stockMovementService,
                                   ProductRepository productRepository,
                                   StockMovementRepository stockMovementRepository) {
        this.stockMovementService = stockMovementService;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<StockMovementModel>> getAllMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<StockMovement> pageResult = stockMovementService.findAll(pageable);
        List<StockMovementModel> models = pageResult.stream()
            .map(StockMovementModel::fromStockMovement)
            .collect(java.util.stream.Collectors.toList());
        return new ResponseEntity<>(models, org.springframework.http.HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createMovement(
            @Valid @RequestBody StockMovementRequest request) {
        
        try {
            
            
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
            
            
            
            StockMovement stockMovement = new StockMovement();
            stockMovement.setProduct(product);
            stockMovement.setMovementType(request.getMovementType());
            stockMovement.setSaleValue(request.getSaleValue());
            stockMovement.setQuantity(request.getQuantity());
            stockMovement.setMovementDate(java.time.LocalDateTime.now());
            stockMovement.setDescription(request.getDescription());
            
            
            
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

    @GetMapping("/by-product")
    public ResponseEntity<java.util.Map<Long, java.util.List<desafio.nexdom.desafio.dto.StockMovementDTO>>> getMovementsGroupedByProduct() {
        return ResponseEntity.ok(stockMovementServiceCustom.findAllGroupedByProduct());
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<CollectionModel<StockMovementModel>> getMovementsByProduct(
             @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
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

    @GetMapping("/profit/{productId}")
    public ResponseEntity<Object> getProfitByProduct(
             @PathVariable Long productId) {
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
