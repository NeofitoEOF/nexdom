package desafio.nexdom.desafio.hateoas;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import desafio.nexdom.desafio.controller.ProductController;
import desafio.nexdom.desafio.controller.StockMovementController;
import desafio.nexdom.desafio.dto.ProfitResultDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public class HateoasResponseAssembler {

    @Deprecated
    public ResponseEntity<CollectionModel<StockMovementModel>> createPaginatedResponse(
            List<StockMovementModel> models, Long productId, int page, int size) {
        
        int total = models.size();
        int totalPages = (int) Math.ceil((double) total / size);
        page = Math.min(page, Math.max(0, totalPages - 1));
        int start = page * size;
        int end = Math.min(start + size, total);
        
        List<StockMovementModel> pageContent = models.subList(start, end);
        CollectionModel<StockMovementModel> collectionModel = CollectionModel.of(pageContent);
        
        addNavigationLinks(collectionModel, productId, page, size, total, end, totalPages);
        
        addRelatedResourceLinks(collectionModel, productId);
        
        HttpHeaders headers = createPaginationHeaders(page, size, total, totalPages);
        
        return new ResponseEntity<>(collectionModel, headers, HttpStatus.OK);
    }
    
    private void addNavigationLinks(
            CollectionModel<StockMovementModel> collectionModel, 
            Long productId, 
            int page, 
            int size, 
            int total, 
            int end, 
            int totalPages) {
        
        collectionModel.add(linkTo(methodOn(StockMovementController.class)
                .getMovementsByProduct(productId, page, size)).withSelfRel());
        
        if (page > 0) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, 0, size)).withRel("first"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, page - 1, size)).withRel("prev"));
        }
        
        if (end < total) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, page + 1, size)).withRel("next"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, totalPages - 1, size)).withRel("last"));
        }
    }
    
    private void addRelatedResourceLinks(
            CollectionModel<StockMovementModel> collectionModel, 
            Long productId) {
        
        collectionModel.add(linkTo(methodOn(StockMovementController.class)
                .createMovement(null)).withRel("create-movement").withType("POST"));
        collectionModel.add(linkTo(methodOn(ProductController.class)
                .getProductById(productId)).withRel("product"));
    }
    
    private HttpHeaders createPaginationHeaders(int page, int size, int total, int totalPages) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(page));
        headers.add("X-Page-Size", String.valueOf(size));
        headers.add("X-Total-Elements", String.valueOf(total));
        headers.add("X-Total-Pages", String.valueOf(totalPages));
        return headers;
    }
    
    public ResponseEntity<CollectionModel<StockMovementModel>> createPaginatedResponseFromPage(
            List<StockMovementModel> models, 
            org.springframework.data.domain.Page<desafio.nexdom.desafio.model.StockMovement> pageResult, 
            Long productId, 
            int page, 
            int size) {
        
        CollectionModel<StockMovementModel> collectionModel = CollectionModel.of(models);
        
        addNavigationLinksFromPage(collectionModel, productId, page, size, pageResult);
        
        addRelatedResourceLinks(collectionModel, productId);
        
        HttpHeaders headers = createPaginationHeadersFromPage(pageResult);
        
        return new ResponseEntity<>(collectionModel, headers, HttpStatus.OK);
    }
    
    private void addNavigationLinksFromPage(
            CollectionModel<StockMovementModel> collectionModel, 
            Long productId, 
            int page, 
            int size,
            org.springframework.data.domain.Page<?> pageResult) {
        
        collectionModel.add(linkTo(methodOn(StockMovementController.class)
                .getMovementsByProduct(productId, page, size)).withSelfRel());
        
        if (pageResult.hasPrevious()) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, 0, size)).withRel("first"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, page - 1, size)).withRel("prev"));
        }
        
        if (pageResult.hasNext()) {
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, page + 1, size)).withRel("next"));
            collectionModel.add(linkTo(methodOn(StockMovementController.class)
                    .getMovementsByProduct(productId, pageResult.getTotalPages() - 1, size)).withRel("last"));
        }
    }
    
    private HttpHeaders createPaginationHeadersFromPage(org.springframework.data.domain.Page<?> pageResult) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Page-Number", String.valueOf(pageResult.getNumber()));
        headers.add("X-Page-Size", String.valueOf(pageResult.getSize()));
        headers.add("X-Total-Elements", String.valueOf(pageResult.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(pageResult.getTotalPages()));
        return headers;
    }
    
    
    public ResponseEntity<ObjectNode> createProfitResponse(ProfitResultDto profitResult, Long productId) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("profit", profitResult.getProfit());
        response.put("totalSold", profitResult.getTotalSold());
        
        ObjectNode linksNode = JsonNodeFactory.instance.objectNode();
        
        linksNode.set("self", JsonNodeFactory.instance.objectNode()
                .put("href", linkTo(methodOn(StockMovementController.class)
                        .getProfitByProduct(productId)).toString()));
        
        linksNode.set("product", JsonNodeFactory.instance.objectNode()
                .put("href", linkTo(methodOn(ProductController.class)
                        .getProductById(productId)).toString()));
        
        linksNode.set("stock-movements", JsonNodeFactory.instance.objectNode()
                .put("href", linkTo(methodOn(StockMovementController.class)
                        .getMovementsByProduct(productId, 0, 20)).toString()));
        
        response.set("_links", linksNode);
        
        return ResponseEntity.ok(response);
    }
}
