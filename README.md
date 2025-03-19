# TradeFlow
---
##  Arquitetura
O projeto é composto por 4 microsserviços que se comunicam utilizando Apache Kafka para implementar o padrão Saga Coreografado:

### 1. Order-Service
- Responsável por gerar o pedido inicial e receber notificações sobre o status da saga.  
- Banco de dados: **MongoDB**  
- Porta: **3000**  

### 2. Product-Validation-Service
- Valida se o produto informado no pedido existe e está disponível em estoque.  
- Banco de dados: **PostgreSQL**  
- Porta: **8090**  

### 3. Payment-Service
- Realiza o pagamento com base nos valores e quantidades informadas no pedido.  
- Banco de dados: **PostgreSQL**  
- Porta: **8091**  

### 4. Inventory-Service
- Realiza a baixa do estoque dos produtos de um pedido.  
- Banco de dados: **PostgreSQL**  
- Porta: **8092**  

---

## Tecnologias e Ferramentas  
- **[Java](https://www.java.com/pt-BR/):** Linguagem principal para implementação dos microsserviços  
- **[Spring Boot](https://spring.io/projects/spring-boot):** Framework para criação dos serviços REST e configuração simplificada  
- **[Apache Kafka](https://kafka.apache.org/):** Comunicação assíncrona entre microsserviços  
- **[PostgreSQL](https://www.postgresql.org/):** Banco de dados relacional para persistência de dados  
- **[MongoDB](https://www.mongodb.com/):** Banco de dados NoSQL para armazenar pedidos e eventos  
- **[Docker](https://www.docker.com/):** Contêinerização dos microsserviços e orquestração dos contêineres  
- **[Redpanda Console](https://www.redpanda.com/):** Interface para monitoramento de eventos Kafka  

---

## Execução do Projeto
### 1. Executando via Docker Compose
```bash
docker-compose up --build -d
```
### 2. Executando via Script Python  
```bash
python build.py
```
### 3. Executando Serviços de Banco de Dados e Kafka Separadamente  
```bash
docker-compose up --build -d order-db kafka product-db payment-db inventory-db
```
### 4. Suba os serviços de banco de dados e Kafka utilizando Docker Compose:  
```bash
docker-compose up -d
```
Para acessar as aplicações e realizar um pedido, basta acessar a URL: (http://localhost:3000/swagger-ui.html) 
