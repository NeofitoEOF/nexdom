package desafio.nexdom.desafio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Servidor de Desenvolvimento");

        Contact contact = new Contact()
                .name("Suporte Nexdom")
                .email("suporte@nexdom.com.br");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("API de Gerenciamento de Produtos e Estoque")
                .version("1.0.0")
                .contact(contact)
                .description("""
                    <h2>API RESTful para Gerenciamento de Produtos e Estoque</h2>
                    <p>Esta API permite o gerenciamento completo de produtos e suas respectivas movimentações de estoque.</p>
                    
                    <h3>Recursos Principais</h3>
                    <ul>
                        <li>Cadastro, consulta, atualização e remoção de produtos</li>
                        <li>Controle de movimentações de estoque (entrada e saída)</li>
                        <li>Consulta de produtos por tipo</li>
                        <li>Suporte a HATEOAS para navegação entre recursos</li>
                    </ul>
                    
                    <h3>Autenticação</h3>
                    <p>Esta API não requer autenticação para uso.</p>
                    
                    <h3>Formato das Respostas</h3>
                    <p>Todas as respostas seguem o formato JSON e incluem links HATEOAS para navegação.</p>
                    
                    <h3>Status de Resposta</h3>
                    <ul>
                        <li><strong>200 OK</strong>: Requisição bem-sucedida</li>
                        <li><strong>201 Created</strong>: Recurso criado com sucesso</li>
                        <li><strong>204 No Content</strong>: Exclusão bem-sucedida</li>
                        <li><strong>400 Bad Request</strong>: Dados inválidos fornecidos</li>
                        <li><strong>404 Not Found</strong>: Recurso não encontrado</li>
                        <li><strong>500 Internal Server Error</strong>: Erro interno do servidor</li>
                    </ul>
                    """)
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
