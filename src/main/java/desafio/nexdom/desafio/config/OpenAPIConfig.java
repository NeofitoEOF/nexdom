package desafio.nexdom.desafio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
    info = @Info(
        title = "Nexdom API",
        version = "1.0",
        description = "API para gerenciamento de produtos e movimentações de estoque.",
        contact = @Contact(
            name = "Equipe Nexdom",
            email = "contato@nexdom.com"
        ),
        license = @License(name = "Apache 2.0", url = "http://springdoc.org")
    ),
    servers = {
        @Server(url = "/api", description = "Servidor principal")
    },
    tags = {
        @Tag(name = "Produtos", description = "Operações relacionadas a produtos"),
        @Tag(name = "Movimentações de Estoque", description = "Operações de movimentação de estoque")
    }
)

/**
 * Configuração centralizada do OpenAPI/Swagger para documentação da API REST.
 * Define metadados, servidores e tags para organização dos endpoints.
 */
public class OpenAPIConfig {
  
}
