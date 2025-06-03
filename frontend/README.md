# Nexdom Frontend

## Descrição

Frontend para o sistema de gestão de estoque e produtos Nexdom, desenvolvido em Vue 3 + TypeScript, utilizando Vite para build rápido e Tailwind CSS para estilização.

- **Vue 3** com Composition API e `<script setup>`
- **TypeScript** para tipagem robusta
- **Tailwind CSS** para UI responsiva e moderna
- **Pinia** para gerenciamento de estado (stores)
- Organização modular: `components/`, `views/`, `stores/`, `interfaces/`, `services/`, `utils/`
- Integração com backend Java Spring Boot (API REST)

## Instalação e uso

```bash
cd frontend
npm install
```

### Ambiente de desenvolvimento

```bash
npm run dev
```
Acesse: http://localhost:5173

### Build para produção

```bash
npm run build
```

### Preview do build

```bash
npm run preview
```

## Estrutura do projeto

```
frontend/
├── public/
├── src/
│   ├── assets/        # Imagens e arquivos estáticos
│   ├── components/    # Componentes Vue reutilizáveis (UI e base)
│   ├── interfaces/    # Tipos e interfaces TypeScript compartilhadas
│   ├── services/      # Serviços de acesso à API, adapters e helpers
│   ├── stores/        # Stores Pinia para estado global (produtos, inventário, etc)
│   ├── utils/         # Funções utilitárias (formatação, validação)
│   └── views/         # Páginas principais e relatórios
├── index.html
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```

## Arquitetura do Projeto

O sistema Nexdom utiliza uma arquitetura moderna, desacoplada e escalável, composta por dois grandes blocos:

### 1. **Backend (Java Spring Boot)**
- Responsável por toda a lógica de negócio, persistência e exposição de uma API RESTful.
- Endpoints para CRUD de produtos, movimentações de estoque, e relatórios de lucro.
- Documentação automática via Swagger/OpenAPI.
- Banco de dados H2 em memória (ou facilmente adaptável para outros bancos).
- Containerização com Docker para fácil deploy.

### 2. **Frontend (Vue 3 + TypeScript + Vite)**
- Interface SPA moderna, responsiva e modular.
- **Vue 3** com Composition API e `<script setup>` para componentes reativos e concisos.
- **Pinia** para gerenciamento centralizado de estado (produtos, inventário, filtros, etc).
- **TypeScript** para tipagem forte, interfaces e validação estática.
- **Tailwind CSS** para estilização rápida, responsiva e consistente.
- **Services** centralizam o acesso à API backend, desacoplando regras de negócio do consumo HTTP.
- Organização modular: componentes base, componentes de domínio, views, stores, interfaces e utils separados.
- **Boas práticas**: documentação JSDoc, validação defensiva, tipagem explícita, reuso de componentes, helpers de formatação.

### Fluxo de Dados
- O frontend consome a API REST via serviços centralizados (`src/services/api.ts`).
- Stores Pinia reagem a mudanças e propagam estado para os componentes.
- Componentes de relatório e dashboard consomem dados agregados e expõem filtros e ações ao usuário.
- Toda a navegação e manipulação de dados é feita de forma reativa e tipada, garantindo robustez e UX fluida.

### Integração
- O frontend espera a API backend rodando em `http://localhost:8080` (ajustável).
- CORS e autenticação podem ser facilmente integrados.
- O deploy pode ser feito de forma independente ou integrada via Docker Compose.

---

## Integração com Backend
- O frontend consome a API REST em Java (Spring Boot) rodando normalmente em `http://localhost:8080`.
- Configure a URL base da API em `src/services/api.ts` se necessário.
- Para ambiente Docker, garanta que as portas estejam expostas e CORS habilitado no backend.

## Comandos úteis
- `npm run lint` — checagem de código
- `npm run format` — formatação automática (se configurado)

## Convenções e dicas
- Use componentes reutilizáveis (`BaseInput`, `BaseSelect`, etc)
- Tipagem explícita para stores, props e serviços
- Utilize os helpers de formatação de moeda/número
- Veja exemplos de documentação JSDoc em componentes principais

## Testando com Backend
1. Suba o backend conforme instruções do README principal.
2. Execute `npm run dev` no frontend.
3. Use a interface para cadastrar produtos, movimentar estoque, e visualizar relatórios.

---

Projeto Nexdom — Engenharia de Software
