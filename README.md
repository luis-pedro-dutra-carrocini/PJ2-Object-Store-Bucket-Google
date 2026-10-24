# PJ2-Object-Store-Bucket-Google

## 🍽️ Sistema de Restaurante — Web App + Object Store + Persistência

Documentação dos processos que compõem a solução:

1. **Backend (Spring Boot) e Frontend** — serve a interface Web (HTML/CSS/JS) e expõe a API REST para gerenciamento de Unidades, Administradores, Produtos, Preços e Imagens.
2. **Integração com Google Cloud Storage (GCS)** — armazena fisicamente as imagens dos produtos em um bucket público para leitura e protegido para escrita.

---

## 🏗️ Arquitetura geral

```
┌────────────────────┐      HTTP/Fetch      ┌──────────────────┐
│  Frontend (HTML/JS)│ ───────────────────► │  API REST (Java) │
└─────────┬──────────┘                      │  (Spring Boot)   │
          │                                 └─┬──────┬─────────┘
          │ (Exibe imagens)                   │      │ 
          ▼                                   │      │ Hibernate/JPA
┌────────────────────┐             ┌──────────┘      ▼
│  Google Cloud      │             │        ┌──────────────────┐
│  Storage (Bucket)  │ ◄───────────┘        │   PostgreSQL     │
│  (Imagens)         │   (Upload/Delete)    │   (Dados)        │
└────────────────────┘                      └──────────────────┘
```

- **Frontend:** HTML puro, CSS e JavaScript. Servido nativamente pelo Spring Boot na pasta `src/main/resources/static`.
- **Backend:** Java 17 + Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security para BCrypt).
- **Banco de Dados:** PostgreSQL.
- **Armazenamento de Objetos (Object Store):** Google Cloud Storage (GCS). O banco armazena apenas a URL do objeto no bucket.

---

## 🔧 Variáveis de ambiente (`application.properties`)

```properties
# Banco de dados PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/nome_bd
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update

# Google Cloud Storage
# Caminho para o arquivo JSON da chave de serviço com permissão de escrita
google.cloud.credentials.path=C:/caminho/para/sua-chave.json
google.cloud.bucket.name=nome-do-seu-bucket

# Porta do servidor
server.port=8080
```

---

# 🖥️ Aplicação Web — Telas e Frontend

A aplicação possui três frentes principais de interface (arquivos na pasta `static`).

## 1. Tela Inicial / Cardápio (`index.html`)
Pública. Acessada via URL com o parâmetro `unidade` (ID da unidade).
- **URL de Exemplo:** `http://localhost:8080/index.html?unidade=93317181-83da-431d-97d1-438c31626152`
- **Funcionalidades:**
  - Busca e exibe o nome do restaurante dinamicamente com base no ID da URL.
  - Lista apenas os produtos com status `ATIVO` daquela unidade.
  - Exibe Card com imagem principal, nome e preço atual.
  - **Modal de Detalhes:** Ao clicar no card, abre um popup exibindo a imagem ampliada, descrição, preço e uma galeria de miniaturas com todas as imagens cadastradas do produto.

## 2. Painel Adm Geral (`adm-geral.html`)
Protegido por login (Tabela `AdmGeralSistema`).
- **Funcionalidades:**
  - Cadastro e edição de Unidades (Nome, Descrição, Telefone, E-mail).
  - Cadastro e edição de Administradores de Unidade (Nome, Usuário, Senha criptografada com BCrypt).
  - Inativação de Unidades e Admins.
  - Validação de unicidade de nome de usuário entre os Admins.

## 3. Painel Adm Unidade (`adm-unidade.html`)
Protegido por login (Tabela `AdmUnidade`). Identifica a unidade do usuário logado e isola os dados.
- **Funcionalidades:**
  - **Cadastro de Produto:** Nome e Descrição.
  - **Gestão de Preços:** Histórico de preços. Ao cadastrar um novo valor, o anterior é inativado automaticamente e o novo passa a ser exibido na tela inicial.
  - **Gestão de Imagens (Integração com Bucket):**
    - Upload inline de imagens diretamente no card do produto.
    - Exibição de galeria de imagens cadastradas.
    - Opção de marcar uma imagem como "Principal" (regra de negócio garante que só exista uma principal por produto).
    - Exclusão de imagem (remove o arquivo físico do Google Cloud Storage e o registro do banco).

---

# 🌐 API REST — Controllers (Java)

## Formato de saída (Entidades/JSON)
Os endpoints que retornam dados usam o próprio mapeamento das entidades JPA convertidas em JSON pelo Jackson (evitando loop infinito usando `@JsonIgnore`).

## Endpoints Principais

### `GET /api/produtos?unidadeId={uuid}`
Lista os produtos ativos da unidade no formato consumido pela tela inicial.
**Resposta:**
```json
[
  {
    "produtoId": "uuid",
    "produtoNome": "Porção de Peixe",
    "produtoDescricao": "Porção 1Kg",
    "imagemPrincipalUrl": "https://storage.googleapis.com/...",
    "precoAtual": 45.90
  }
]
```

### `GET /api/produtos/{id}`
Retorna os detalhes de um produto para montar o modal (inclui lista de imagens e preço atual).

### `POST /api/adm-geral/login`
Valida credenciais do Administrador Geral.
### `POST /api/adm-unidade/login`
Valida credenciais do Administrador da Unidade e retorna os IDs necessários para o frontend.

### `POST /api/adm/produtos/{unidadeId}/{admUnidadeId}`
Cria um novo produto vinculado à unidade.
### `PUT /api/adm-unidade/produtos/{produtoId}`
Edita os dados do produto (nome, descrição, status ATIVO/INATIVO).
### `POST /api/adm-unidade/produtos/{produtoId}/preco`
Cadastra um novo preço e inativa o preço anterior daquele produto.

### `POST /api/adm/produtos/{produtoId}/imagens`
Upload de imagem via `MultipartFile`.
- **Fluxo:** Salva o registro no banco para obter o ID -> faz o upload no GCS usando o padrão de pastas -> atualiza o banco com a URL gerada.
### `PUT /api/adm-unidade/imagens/{imagemId}/principal`
Altera a imagem principal do produto (desmarca as outras e marca esta).
### `DELETE /api/adm/produtos/imagens/{imagemId}`
Exclui a imagem do banco e do bucket GCS.

---

# ☁️ Google Cloud Storage — Object Store

## Estrutura de pastas no Bucket
As imagens não são salvas na raiz do bucket, elas seguem uma hierarquia lógica para organização e segurança:
`produtos/{unidadeId}/{produtoId}/{imagemId}.extensao`

**Exemplo de URL:**
`https://storage.googleapis.com/repositorio/produtos/187b8867-c7e5-430d-b12a-76c6a67db35a/beb60261-100a-4baa-bc77-6c1ad830f52c.jfif`

## Serviço (`StorageService.java`)
- `uploadFile`: Recebe o arquivo e o caminho montado pelo Controller, configura o `BlobInfo` com o tipo de conteúdo (MIME type) e envia para o GCS. Retorna a URL pública.
- `deleteFile`: Extrai o caminho do arquivo da URL pública e solicita a exclusão do objeto no GCS.

---

## 🗄️ Banco de Dados — Schema PostgreSQL (JPA/Hibernate)

Tabelas mapeadas via Entidades Java:

| Tabela | PK | Observações |
|---|---|---|
| `adm_geral_sistema` | `UUID` | Gerenciamento global de unidades e credenciais. Senha com BCrypt. |
| `unidade` | `UUID` | Restaurantes/Estabelecimentos. Possui status (ATIVO/INATIVO). |
| `adm_unidade` | `UUID` | Usuários administradores atrelados a uma `unidade`. |
| `produto` | `UUID` | Itens do cardápio, atrelados a `unidade` e `adm_unidade`. |
| `imagem_produto` | `UUID` | Referências às URLs do GCS, atreladas ao `produto`. Flag `imagemProdutoPrincipal` ("SIM" ou "NAO"). |
| `preco_produto` | `UUID` | Histórico de preços. Possui flag de status ("ATIVO" ou "INATIVO"). |


---

## 🧪 Fluxo completo (ponta a ponta)

1. **Admin Geral:** Acessa `adm-geral.html`, faz login e cadastra uma Unidade e um Administrador de Unidade.
2. **Admin Unidade:** Acessa `adm-unidade.html`, faz login com as credenciais criadas.
3. **Cadastro de Produto:** Cria o produto, que é salvo no PostgreSQL com status `ATIVO`.
4. **Upload de Imagem:** Seleciona um arquivo, o Spring Boot recebe via Multipart, salva no banco e envia ao GCS. A URL pública é gravada no banco. O Admin marca a imagem como Principal.
5. **Definição de Preço:** O Admin insere o valor de venda. O preço é salvo com status `ATIVO`.
6. **Cliente Final:** Acessa `index.html?unidade={id}`, vê o produto, o preço atual e a imagem vinda diretamente do bucket. Ao clicar, visualiza o modal com todas as imagens.

---

## ✅ Checklist de execução

```bash
# 1. Verifique se o PostgreSQL está rodando e o banco foi criado
# 2. Verifique se o arquivo .json da Service Account do GCP está no caminho correto
# 3. Ajuste as credenciais no application.properties

# 4. Compile e rode o projeto via VS Code (Spring Boot Dashboard)
# ou via terminal Maven:
mvn clean spring-boot:run
```

---

## 📁 Estrutura de arquivos

```
.
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/restaurante/app/
│   │   │   ├── AppApplication.java
│   │   │   ├── config/
│   │   │   │   └── StorageConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── dto/
│   │   │   ├── controller/
│   │   │   │   ├── AdmGeralController.java
│   │   │   │   ├── AdmUnidadeController.java
│   │   │   │   ├── AdmProdutoController.java
│   │   │   │   └── ProdutoController.java
│   │   │   ├── entity/
│   │   │   │   ├── AdmGeralSistema.java
│   │   │   │   ├── AdmUnidade.java
│   │   │   │   ├── Unidade.java
│   │   │   │   ├── Produto.java
│   │   │   │   ├── PrecoProduto.java
│   │   │   │   └── ImagemProduto.java
│   │   │   ├── repository/
│   │   │   └── service/
│   │   │       └── StorageService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── chavebucket/
│   │       └── static/
│   │           ├── css/
│   │           ├── js/
│   │           ├── index.html
│   │           ├── adm-geral.html
│   │           └── adm-unidade.html
└── target/                # Gerado automaticamente pelo Maven
```

## ✒️ Desenvolvido por:

* **[Cláudio de Melo Júnior](https://github.com/Claudio-Fatec)**
* **[João Vitor Nicolau](https://github.com/Joao-Vitor-Nicolau-dos-Santos)**
* **[Luís Pedro Dutra Carrocini](https://github.com/luis-pedro-dutra-carrocini)**
