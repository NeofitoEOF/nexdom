package desafio.nexdom.desafio.controller;

import desafio.nexdom.desafio.dto.CreateStockMovementResponse;
import desafio.nexdom.desafio.dto.DashboardStatsDto;
import desafio.nexdom.desafio.dto.ProductProfitDto;
import desafio.nexdom.desafio.dto.StockMovementRequest;
import desafio.nexdom.desafio.hateoas.StockMovementModel;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
    value = "/api/stock-movements",
    produces = { 
        MediaType.APPLICATION_JSON_VALUE,
        "application/hal+json"
    }
)
public class StockMovementController {
    private final IStockMovementService stockMovementService;
    private final desafio.nexdom.desafio.hateoas.HateoasResponseAssembler hateoasAssembler;
    
    @Autowired
    public StockMovementController(IStockMovementService stockMovementService, 
                                  desafio.nexdom.desafio.hateoas.HateoasResponseAssembler hateoasAssembler) {
        this.stockMovementService = stockMovementService;
        this.hateoasAssembler = hateoasAssembler;
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovementModel> getMovementById(@PathVariable Long id) {
        StockMovementModel model = stockMovementService.getMovementModelById(id);
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockMovementModel> updateMovement(@PathVariable Long id, @Valid @RequestBody StockMovementRequest request) {
        StockMovementModel model = stockMovementService.updateMovement(id, request);
        return ResponseEntity.ok(model);
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
    public ResponseEntity<CreateStockMovementResponse> createMovement(@Valid @RequestBody StockMovementRequest request) {
        CreateStockMovementResponse response = stockMovementService.createStockMovement(request);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getMovement().getId())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/by-product")
    public ResponseEntity<java.util.Map<Long, java.util.List<desafio.nexdom.desafio.dto.StockMovementDTO>>> getMovementsGroupedByProduct() {
        return ResponseEntity.ok(stockMovementService.findAllGroupedByProduct());
    }
    
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<CollectionModel<StockMovementModel>> getMovementsByProduct(
             @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        org.springframework.data.domain.Page<StockMovement> pageResult = 
            stockMovementService.getMovementsByProduct(productId, pageable);
            
        List<StockMovementModel> models = pageResult.getContent().stream()
            .map(StockMovementModel::fromStockMovement)
            .collect(Collectors.toList());
            
        return hateoasAssembler.createPaginatedResponseFromPage(models, pageResult, productId, page, size);
    }

    @GetMapping("/profit/{productId}")
    public ResponseEntity<com.fasterxml.jackson.databind.node.ObjectNode> getProfitByProduct(@PathVariable Long productId) {
        var profitResult = stockMovementService.calculateProfitAndTotalSold(productId);
        
        return hateoasAssembler.createProfitResponse(profitResult, productId);
    }
    
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(stockMovementService.getDashboardStats());
    }
}
