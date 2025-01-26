# Order Management System

## Descrição do Projeto
O **Order Management System** é um serviço desenvolvido em Java com Spring Boot para gerenciar pedidos. Ele realiza o cálculo do valor total dos produtos de um pedido e disponibiliza APIs para integração com sistemas externos. O serviço é projetado para lidar com alta volumetria de dados, garantindo escalabilidade, consistência e eficiência.

## Arquitetura do Sistema
A arquitetura do sistema segue o modelo de microsserviços com os seguintes componentes:

1. **Produto Externo A**: Sistema externo que envia pedidos para o serviço.
2. **Order (Serviço Principal)**:
    - Responsável por gerenciar e calcular os pedidos.
    - Salva as informações em um banco de dados MongoDB.
    - Disponibiliza APIs para consulta dos pedidos processados.
3. **Produto Externo B**: Sistema externo que consome os pedidos processados.
4. **Order Database**: Armazenamento persistente das informações dos pedidos.

### Fluxo de Dados
1. **Recepção de Pedidos**: O serviço recebe pedidos de um sistema externo (Produto Externo A).
2. **Processamento**:
    - Validação de duplicação via Redis.
    - Cálculo do valor total dos produtos.
3. **Persistência**: Os pedidos são salvos no MongoDB.
4. **Consulta de Pedidos**: Sistemas externos podem consultar os pedidos processados via API.

---

## Tecnologias Utilizadas
- **Linguagem**: Java 17
- **Framework**: Spring Boot 3
- **Banco de Dados**: MongoDB
- **Cache**: Redis
- **Ferramentas de Containerização**: Docker e Docker Compose
- **Gerenciamento de Dependências**: Maven

---

## Funcionalidades
- Recepção de pedidos via API REST.
- Validação de duplicação de pedidos usando Redis.
- Cálculo do valor total dos pedidos com base em:
    - `unitPrice` (preço unitário).
    - `quantity` (quantidade).
    - `totalPrice` (unitPrice * quantity).
- Paginação na consulta de pedidos.
- Endpoint para buscar pedidos por ID.
- Tratamento de erros e respostas adequadas com HTTP Status Codes.
- Suporte a alta volumetria (150k a 200k pedidos/dia).

---

## Endpoints da API

### 1. Receber Pedido
**URL**: `/orders`
**Método**: `POST`

**Body de Exemplo**:
```json
{
  "orderId": "12345",
  "products": [
    { "name": "Produto A", "unitPrice": 25.50, "quantity": 2 },
    { "name": "Produto B", "unitPrice": 15.00, "quantity": 1 }
  ]
}
```

**Resposta**:
- Status: `200 OK`
- Corpo:
```json
{
  "orderId": "12345"
}
```

---

### 2. Consultar Todos os Pedidos
**URL**: `/orders`
**Método**: `GET`

**Parâmetros de Query**:
- `page` (opcional): Número da página (default: 0).
- `size` (opcional): Tamanho da página (default: 10).

**Resposta de Exemplo**:
```json
{
  "content": [
    {
      "orderId": "12345",
      "products": [
        { "name": "Produto A", "unitPrice": 25.50, "quantity": 2, "totalPrice": 51.00 },
        { "name": "Produto B", "unitPrice": 15.00, "quantity": 1, "totalPrice": 15.00 }
      ],
      "total": 66.00,
      "status": "processed",
      "createdAt": "2025-01-26T20:30:00",
      "updatedAt": "2025-01-26T20:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 1,
  "totalElements": 1
}
```

---

### 3. Consultar Pedido por ID
**URL**: `/orders/{id}`
**Método**: `GET`

**Resposta de Exemplo**:
```json
{
  "orderId": "12345",
  "products": [
    { "name": "Produto A", "unitPrice": 25.50, "quantity": 2, "totalPrice": 51.00 },
    { "name": "Produto B", "unitPrice": 15.00, "quantity": 1, "totalPrice": 15.00 }
  ],
  "total": 66.00,
  "status": "processed",
  "createdAt": "2025-01-26T20:30:00",
  "updatedAt": "2025-01-26T20:30:00"
}
```

**Erro** (Pedido não encontrado):
```json
{
  "timestamp": "2025-01-26T20:34:52.312",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 123456",
  "path": "/orders/123456"
}
```

---

## Como Executar o Projeto

### 1. Pré-requisitos
- **Docker** e **Docker Compose** instalados.
- Maven configurado.

### 2. Executar com Docker Compose
1. Compile o projeto:
   ```bash
   mvn clean package
   ```
2. Suba os contêineres:
   ```bash
   docker-compose up --build
   ```
3. Acesse a aplicação em `http://localhost:8080`.

### 3. Limpar Volumes
Para limpar os dados persistidos no MongoDB e Redis:
```bash
docker-compose down -v
```

---

## Melhorias Futuras
- Adicionar suporte a filas (RabbitMQ/Kafka) para processamento assíncrono.
- Implementar autenticação com JWT.
- Adicionar mais métricas e observabilidade com Prometheus/Grafana.
- Criar testes de carga.

---

## Autor
Projeto desenvolvido para atender a um desafio técnico, incluindo soluções para escalabilidade, consistência e robustez do sistema.
