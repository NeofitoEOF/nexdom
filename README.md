# Desafio Nexdom

## Descrição

Este projeto é uma API RESTful para controle de estoque de produtos, desenvolvida com Spring Boot. A aplicação permite o cadastro de produtos de diferentes tipos, controle de entrada e saída de estoque, e cálculo de lucro por produto. O sistema utiliza padrões modernos de desenvolvimento como DTO, HATEOAS e tratamento global de exceções.

## Funcionalidades

- CRUD completo de produtos (Create, Read, Update, Delete)
- Entrada e saída de produtos no estoque com validação de saldo
- Consulta de produtos por tipo
- Cálculo de lucro por produto (valor de venda - valor do fornecedor)
- Paginação e ordenação de resultados
- Respostas enriquecidas com HATEOAS para melhor navegabilidade
- Validação de dados de entrada
- Tratamento global de exceções com mensagens amigáveis

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.2.3
- Spring Data JPA
- Spring HATEOAS
- H2 Database (banco de dados em memória)
- Swagger/OpenAPI para documentação da API
- Docker para containerização
- JUnit 5 e Mockito para testes automatizados

## Como executar

### Usando Docker

```bash
docker-compose up --build
```

A aplicação estará disponível em http://localhost:8081

### Sem Docker

```bash
./mvnw spring-boot:run
```

## Testando a API

### Usando o Swagger UI

Acesse a documentação interativa da API em http://localhost:8081/swagger-ui/index.html

### Usando a Collection do Postman

Este projeto inclui uma collection do Postman para facilitar os testes da API. Para utilizá-la:

1. Importe o arquivo `Desafio_Nexdom_API_Collection.postman_collection.json` no Postman
2. Execute as requisições na ordem sugerida para testar o fluxo completo da aplicação

### Fluxo de teste recomendado

1. Criar produtos de diferentes tipos (ELECTRONIC, APPLIANCE, FURNITURE, BOOK)
2. Registrar entradas de estoque para os produtos
3. Consultar produtos por tipo
4. Registrar saídas de estoque (vendas)
5. Testar a validação de estoque insuficiente
6. Calcular o lucro por produto

## Estrutura do projeto

```
src
├── main
│   ├── java
│   │   └── desafio
│   │       └── nexdom
│   │           └── desafio
│   │               ├── config            # Configurações da aplicação
│   │               ├── controller        # Controladores REST
│   │               ├── dto               # Data Transfer Objects
│   │               ├── exception         # Exceções personalizadas e handler global
│   │               ├── hateoas           # Classes relacionadas ao HATEOAS
│   │               ├── interfaces        # Interfaces de serviço
│   │               ├── model             # Entidades JPA
│   │               ├── repository        # Repositórios Spring Data
│   │               └── service           # Implementações de serviço
│   └── resources
└── test
    └── java
        └── desafio
            └── nexdom
                └── desafio
                    ├── controller        # Testes de controladores
                    └── service           # Testes de serviços
```

Este é um projeto Spring Boot com Java 21, dockerizado para facilitar o desenvolvimento e deploy. O projeto segue boas práticas de desenvolvimento como Clean Code, SOLID e testes automatizados.

## Requisitos

- Docker (versão 20.10.0 ou superior)
- Docker Compose (versão 2.0.0 ou superior)
- Pelo menos 1GB de RAM disponível
- Portas 8081 e 3000 disponíveis no host

## Executando o projeto

Para executar o projeto usando Docker, siga os passos abaixo:

```bash
# Clone o repositório (se ainda não tiver feito)
git clone git@github.com:NeofitoEOF/nexdom.git
cd desafio

# Construir a imagem Docker
docker-compose build

# Iniciar o contêiner
docker-compose up -d

# Verificar os logs
docker logs -f desafio-app-1
```

A aplicação estará disponível em:
- API: http://localhost:8081
- Console H2 (banco de dados): http://localhost:8081/h2-console
  - JDBC URL: jdbc:h2:mem:testdb
  - Usuário: sa
  - Senha: password

## Documentação da API

A API é documentada usando o SpringDoc OpenAPI (Swagger). Você pode acessar a documentação interativa em:

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- Especificação OpenAPI (JSON): http://localhost:8081/v3/api-docs
- Especificação OpenAPI (YAML): http://localhost:8081/v3/api-docs.yaml

A documentação permite visualizar todos os endpoints disponíveis, testar as requisições diretamente pela interface e verificar os modelos de dados utilizados.

## Endpoints da API

### Produtos
- `GET /api/products` - Listar todos os produtos (com paginação)
- `GET /api/products/{id}` - Obter um produto específico
- `GET /api/products/type/{type}` - Listar produtos por tipo (com paginação)
- `POST /api/products` - Criar um novo produto
- `PUT /api/products/{id}` - Atualizar um produto existente
- `DELETE /api/products/{id}` - Excluir um produto

### Movimentações de Estoque
- `GET /api/stock-movements` - Listar todas as movimentações de estoque (com paginação)
- `GET /api/stock-movements/{id}` - Obter uma movimentação específica
- `GET /api/stock-movements/product/{productId}` - Listar movimentações por produto (com paginação)
- `GET /api/stock-movements/profit/{productId}` - Calcular lucro por produto
- `POST /api/stock-movements` - Criar uma nova movimentação
- `DELETE /api/stock-movements/{id}` - Excluir uma movimentação

## Arquitetura e Padrões de Projeto

O projeto segue uma arquitetura em camadas com as seguintes características:

### Padrões de Projeto
- **DTO (Data Transfer Object)**: Separa a representação externa da representação interna dos dados
- **Repository Pattern**: Abstrai o acesso a dados através de interfaces Spring Data JPA
- **Service Layer**: Encapsula a lógica de negócio
- **HATEOAS**: Enriquece as respostas REST com links para navegação
- **Command Query Separation**: Interfaces separadas para comandos e consultas
- **Global Exception Handler**: Tratamento centralizado de exceções

### Tecnologias
- Java 21
- Spring Boot 3.2.3
- Spring Data JPA
- Spring HATEOAS
- H2 Database
- Docker
- Lombok
- JUnit 5 e Mockito para testes

## Solução de Problemas

### Problemas de CORS

A aplicação está configurada com uma política CORS centralizada para permitir requisições de qualquer origem. Se você encontrar problemas de CORS ao acessar o Swagger UI ou fazer requisições para a API, verifique:

1. Se o contêiner Docker está em execução (`docker ps`)
2. Se a aplicação está acessível na porta 8081 (`curl -I http://localhost:8081`)
3. Se os logs mostram algum erro (`docker logs -f desafio-app-1`)

### Parando a aplicação

Para parar a aplicação, execute:

```bash
docker-compose down
```

### Reiniciando a aplicação

Para reiniciar a aplicação após fazer alterações no código:

```bash
docker-compose down
docker-compose up -d
```

## Melhorias Recentes

### Refatoração e Limpeza de Código
- Remoção de classes não utilizadas
- Consolidação das configurações CORS em uma única classe
- Unificação da configuração OpenAPI/Swagger
- Correção e atualização de testes para usar DTOs e HATEOAS

### Testes
- Testes unitários para serviços
- Testes de integração para controladores
- Testes de fluxo para validar cenários completos

### Documentação
- Documentação detalhada via Swagger/OpenAPI
- Comentários Javadoc em classes e métodos importantes
- README atualizado com informações detalhadas sobre o projeto

## Próximos Passos
- Implementação de autenticação e autorização
- Migração para um banco de dados persistente
- Implementação de cache para melhorar performance
- Monitoramento e logging avançados
