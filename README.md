# Desafio Nexdom

## Descrição

Este projeto é uma API RESTful para controle de estoque de produtos, desenvolvida com Spring Boot. A aplicação permite o cadastro de produtos de diferentes tipos, controle de entrada e saída de estoque, e cálculo de lucro por produto.

## Funcionalidades

- CRUD completo de produtos (Create, Read, Update, Delete)
- Entrada e saída de produtos no estoque com validação de saldo
- Consulta de produtos por tipo
- Cálculo de lucro por produto (valor de venda - valor do fornecedor)

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.2.3
- Spring Data JPA
- H2 Database (banco de dados em memória)
- Swagger/OpenAPI para documentação da API
- Docker para containerização

## Como executar

### Usando Docker

```bash
docker-compose up --build
```

A aplicação estará disponível em http://localhost:8080

### Sem Docker

```bash
./mvnw spring-boot:run
```

## Testando a API

### Usando o Swagger UI

Acesse a documentação interativa da API em http://localhost:8080/swagger-ui/index.html

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
│   │               ├── config
│   │               ├── controller
│   │               ├── exception
│   │               ├── model
│   │               ├── repository
│   │               └── service
│   └── resources
└── test
    └── java
        └── desafio
            └── nexdom
                └── desafio
                    ├── controller
                    └── service
```

Este é um projeto Spring Boot com Java 21, dockerizado para facilitar o desenvolvimento e deploy.

## Requisitos

- Docker (versão 20.10.0 ou superior)
- Docker Compose (versão 2.0.0 ou superior)
- Pelo menos 1GB de RAM disponível
- Portas 8082 disponíveis no host

## Executando o projeto

Para executar o projeto usando Docker, siga os passos abaixo:

```bash
# Clone o repositório (se ainda não tiver feito)
git clone [URL_DO_REPOSITÓRIO]
cd desafio

# Construir a imagem Docker
docker-compose build

# Iniciar o contêiner
docker-compose up -d

# Verificar os logs
docker logs -f desafio-app-1
```

A aplicação estará disponível em:
- API: http://localhost:8082
- Console H2 (banco de dados): http://localhost:8082/h2-console
  - JDBC URL: jdbc:h2:mem:testdb
  - Usuário: sa
  - Senha: password

## Documentação da API

A API é documentada usando o SpringDoc OpenAPI (Swagger). Você pode acessar a documentação interativa em:

- Swagger UI: http://localhost:8082/swagger-ui/index.html
- Especificação OpenAPI (JSON): http://localhost:8082/v3/api-docs
- Especificação OpenAPI (YAML): http://localhost:8082/v3/api-docs.yaml

A documentação permite visualizar todos os endpoints disponíveis, testar as requisições diretamente pela interface e verificar os modelos de dados utilizados.

## Endpoints da API

### Produtos
- GET /api/products - Listar todos os produtos
- GET /api/products/{id} - Obter um produto específico
- POST /api/products - Criar um novo produto
- PUT /api/products/{id} - Atualizar um produto existente
- DELETE /api/products/{id} - Excluir um produto

### Movimentações de Estoque
- GET /api/stock-movements - Listar todas as movimentações de estoque
- GET /api/stock-movements/{id} - Obter uma movimentação específica
- POST /api/stock-movements - Criar uma nova movimentação
- PUT /api/stock-movements/{id} - Atualizar uma movimentação existente
- DELETE /api/stock-movements/{id} - Excluir uma movimentação

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.2.3
- Spring Data JPA
- H2 Database
- Docker
- Lombok
- Mockito (para testes)
- TestContainers (para testes)

## Solução de Problemas

### Problemas de CORS

A aplicação já está configurada com filtros CORS para permitir requisições de qualquer origem. Se você encontrar problemas de CORS ao acessar o Swagger UI ou fazer requisições para a API, verifique:

1. Se o contêiner Docker está em execução (`docker ps`)
2. Se a aplicação está acessível na porta 8082 (`curl -I http://localhost:8082`)
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
