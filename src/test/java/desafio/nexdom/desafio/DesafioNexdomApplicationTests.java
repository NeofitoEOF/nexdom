package desafio.nexdom.desafio;

import desafio.nexdom.desafio.model.Product;
import desafio.nexdom.desafio.model.StockMovement;
import desafio.nexdom.desafio.model.MovementType;
import desafio.nexdom.desafio.repository.ProductRepository;
import desafio.nexdom.desafio.repository.StockMovementRepository;
import desafio.nexdom.desafio.service.ProductService;
import desafio.nexdom.desafio.service.StockMovementService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.json.JSONObject;
import org.json.JSONArray;

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
	private ProductService productService;

	@Autowired
	private StockMovementService stockMovementService;

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
			// 1. Criar um produto
			Product product = new Product();
			product.setCode("FLOW-001");
			product.setDescription("Flow Test Product");
			product.setType("ELECTRONIC");
			product.setSupplierValue(BigDecimal.valueOf(100));
			product.setStockQuantity(0); // Começa com estoque zero

			Product savedProduct = productService.save(product);
			assertNotNull(savedProduct.getId());
			assertEquals("FLOW-001", savedProduct.getCode());
			assertEquals(0, savedProduct.getStockQuantity());

			// 2. Adicionar estoque (entrada)
			StockMovement entryMovement = new StockMovement();
			entryMovement.setProduct(savedProduct);
			entryMovement.setMovementType(MovementType.ENTRADA);
			entryMovement.setSaleValue(BigDecimal.valueOf(0.01)); // Valor mínimo para passar na validação
			entryMovement.setMovementDate(LocalDateTime.now());
			entryMovement.setQuantity(10);

			StockMovement savedEntryMovement = stockMovementService.save(entryMovement);
			assertNotNull(savedEntryMovement.getId());

			// 3. Verificar se o estoque foi atualizado
			Product updatedProduct = productService.findById(savedProduct.getId()).orElseThrow();
			assertEquals(10, updatedProduct.getStockQuantity());

			// 4. Realizar uma venda (saída)
			StockMovement exitMovement = new StockMovement();
			exitMovement.setProduct(updatedProduct);
			exitMovement.setMovementType(MovementType.SAIDA);
			exitMovement.setSaleValue(BigDecimal.valueOf(150)); // Valor de venda
			exitMovement.setMovementDate(LocalDateTime.now());
			exitMovement.setQuantity(5);

			StockMovement savedExitMovement = stockMovementService.save(exitMovement);
			assertNotNull(savedExitMovement.getId());

			// 5. Verificar se o estoque foi atualizado novamente
			Product finalProduct = productService.findById(savedProduct.getId()).orElseThrow();
			assertEquals(5, finalProduct.getStockQuantity());

			// 6. Calcular o lucro
			BigDecimal profit = stockMovementService.calculateProfit(savedProduct.getId());
			// Receita: 5 * 150 = 750
			// Custo: 5 * 100 = 500 (estoque atual * valor do fornecedor)
			// Lucro esperado: 750 - 500 = 250
			assertEquals(BigDecimal.valueOf(250), profit);

			// 7. Verificar que não é possível vender mais do que o estoque disponível
			StockMovement invalidMovement = new StockMovement();
			invalidMovement.setProduct(finalProduct);
			invalidMovement.setMovementType(MovementType.SAIDA);
			invalidMovement.setSaleValue(BigDecimal.valueOf(150));
			invalidMovement.setMovementDate(LocalDateTime.now());
			invalidMovement.setQuantity(10); // Mais do que o estoque disponível

			assertThrows(InsufficientStockException.class, () -> {
				stockMovementService.save(invalidMovement);
			});

			// 8. Verificar que o estoque não foi alterado após a tentativa inválida
			Product unchangedProduct = productService.findById(savedProduct.getId()).orElseThrow();
			assertEquals(5, unchangedProduct.getStockQuantity());
		}
	}

	@Test
	@DisplayName("Valida HATEOAS e paginação em /api/products")
	void testProductsHateoasAndPagination() throws Exception {
		String url = "http://localhost:" + port + "/api/products";
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		HttpHeaders headers = response.getHeaders();
		assertTrue(headers.containsKey("X-Page-Number"));
		assertTrue(headers.containsKey("X-Page-Size"));
		assertTrue(headers.containsKey("X-Total-Elements"));
		assertTrue(headers.containsKey("X-Total-Pages"));
		JSONObject json = new JSONObject(response.getBody());
		assertTrue(json.has("_links"), "Deve conter _links HATEOAS");
		JSONObject links = json.getJSONObject("_links");
		assertTrue(links.has("self"), "Deve conter link self");
		assertTrue(links.has("create-product"), "Deve conter link create-product");
	}

	@Test
	@DisplayName("Valida HATEOAS e paginação em /api/stock-movements")
	void testStockMovementsHateoasAndPagination() throws Exception {
		String url = "http://localhost:" + port + "/api/stock-movements";
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		HttpHeaders headers = response.getHeaders();
		assertTrue(headers.containsKey("X-Page-Number"));
		assertTrue(headers.containsKey("X-Page-Size"));
		assertTrue(headers.containsKey("X-Total-Elements"));
		assertTrue(headers.containsKey("X-Total-Pages"));
		JSONObject json = new JSONObject(response.getBody());
		assertTrue(json.has("_links"), "Deve conter _links HATEOAS");
		JSONObject links = json.getJSONObject("_links");
		assertTrue(links.has("self"), "Deve conter link self");
		assertTrue(links.has("create-movement"), "Deve conter link create-movement");
	}

	// Método auxiliar para assertEquals com mensagem de erro clara
	private static void assertEquals(Object expected, Object actual) {
		org.junit.jupiter.api.Assertions.assertEquals(expected, actual, 
			"Esperado: " + expected + ", Atual: " + actual);
	}
}
