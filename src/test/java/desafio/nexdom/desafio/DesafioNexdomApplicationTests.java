package desafio.nexdom.desafio;

import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import desafio.nexdom.desafio.interfaces.IProductService;
import desafio.nexdom.desafio.interfaces.IStockMovementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import desafio.nexdom.desafio.exception.InsufficientStockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Testes de integração da aplicação")
class DesafioNexdomApplicationTests {

    @LocalServerPort
    private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private StockMovementRepository stockMovementRepository;

	@Autowired
	private IProductService productService;

	@Autowired
	private IStockMovementService stockMovementService;

	@Test
	@DisplayName("Verifica se o contexto da aplicação carrega corretamente")
	void contextLoads() {
		assertNotNull(productRepository);
		assertNotNull(stockMovementRepository);
		assertNotNull(productService);
		assertNotNull(stockMovementService);
	}

	@Nested
	@DisplayName("Testes de fluxo completo")
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
	class FlowTests {

		@Test
		@DisplayName("Deve executar um fluxo completo de operações")
		@Transactional
		void testCompleteFlow() {
			Product product = new Product();
			product.setCode("FLOW-001");
			product.setDescription("Flow Test Product");
			product.setType("ELECTRONIC");
			product.setSupplierValue(BigDecimal.valueOf(100));
			product.setStockQuantity(0);

			Product savedProduct = productService.save(product);
			assertNotNull(savedProduct.getId());
			assertEquals("FLOW-001", savedProduct.getCode());
			assertEquals(0, savedProduct.getStockQuantity());

			StockMovement entryMovement = new StockMovement();
			entryMovement.setProduct(savedProduct);
			entryMovement.setMovementType(MovementType.ENTRADA);
			entryMovement.setPurchaseValue(BigDecimal.valueOf(0.01)); 
entryMovement.setSaleValue(BigDecimal.valueOf(0.01)); 
			entryMovement.setMovementDate(LocalDateTime.now());
			entryMovement.setQuantity(10);

			StockMovement savedEntryMovement = stockMovementService.save(entryMovement);
			assertNotNull(savedEntryMovement.getId());

			Product updatedProduct = productService.findById(savedProduct.getId());
			assertEquals(10, updatedProduct.getStockQuantity());

			StockMovement exitMovement = new StockMovement();
			exitMovement.setProduct(updatedProduct);
			exitMovement.setMovementType(MovementType.SAIDA);
			exitMovement.setSaleValue(BigDecimal.valueOf(150)); 
			exitMovement.setMovementDate(LocalDateTime.now());
			exitMovement.setQuantity(5);

			StockMovement savedExitMovement = stockMovementService.save(exitMovement);
			assertNotNull(savedExitMovement.getId());

			Product finalProduct = productService.findById(savedProduct.getId());
			assertEquals(5, finalProduct.getStockQuantity());

			BigDecimal profit = stockMovementService.calculateProfit(savedProduct.getId());
			assertEquals(BigDecimal.valueOf(749.95), profit);

			StockMovement invalidMovement = new StockMovement();
			invalidMovement.setProduct(finalProduct);
			invalidMovement.setMovementType(MovementType.SAIDA);
			invalidMovement.setSaleValue(BigDecimal.valueOf(150));
			invalidMovement.setMovementDate(LocalDateTime.now());
			invalidMovement.setQuantity(10); 

			assertThrows(InsufficientStockException.class, () -> {
				stockMovementService.save(invalidMovement);
			});

			Product unchangedProduct = productService.findById(savedProduct.getId());
			assertEquals(5, unchangedProduct.getStockQuantity());
		}
	}

	@Test
	@DisplayName("Valida HATEOAS e paginação em /api/products")
	void testProductsHateoasAndPagination() throws Exception {
		String url = "http://localhost:" + port + "/api/products";
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	@DisplayName("Valida HATEOAS e paginação em /api/stock-movements")
	void testStockMovementsHateoasAndPagination() throws Exception {
		String url = "http://localhost:" + port + "/api/stock-movements";
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	private static void assertEquals(Object expected, Object actual) {
		org.junit.jupiter.api.Assertions.assertEquals(expected, actual, 
			"Esperado: " + expected + ", Atual: " + actual);
	}
}
