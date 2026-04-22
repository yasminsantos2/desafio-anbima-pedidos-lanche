# 🍔 Desafio Técnico: Sistema de Pedidos Posicionais

Este projeto é uma solução completa para um sistema de delivery de lanches que processa pedidos via **strings posicionais de 40 caracteres**. A arquitetura é baseada em microsserviços (Módulo A e Módulo B) que se comunicam de forma assíncrona via **RabbitMQ**.

---

## 📐 Layout da String Posicional (40 Caracteres)

O sistema segue rigorosamente o layout abaixo para todas as transações:

| Campo | Posição | Tamanho | Tipo | Regra |
| :--- | :--- | :--- | :--- | :--- |
| **tipoLanche** | 1–10 | 10 | Alfanumérico (A) | Preencher com espaços à direita |
| **proteina** | 11–20 | 10 | Alfanumérico (A) | Preencher com espaços à direita |
| **acompanhamento** | 21–30 | 10 | Alfanumérico (A) | Preencher com espaços à direita |
| **quantidade** | 31–32 | 2 | Numérico (N) | 01 a 99 (Zeros à esquerda) |
| **bebida** | 33–40 | 8 | Alfanumérico (A) | Preencher com espaços à direita |

---

## 🚀 Como Executar

### 1. Pré-requisitos
- **Docker** e **Docker Compose** instalados.
- **Node.js** (v18+) - Necessário apenas para rodar o frontend localmente.

### 2. Rodando o Backend (Docker)
Este é o método recomendado, pois sobe o Banco de Dados (Postgres), a Fila (RabbitMQ) e a Aplicação Java automaticamente.

No diretório raiz do projeto, execute:
```bash
docker-compose down -v  # Limpa o ambiente anterior (opcional)
docker-compose up --build -d
```

### 3. Rodando o Frontend (Angular)
O frontend fornece um **Assistente de Mapeamento** e uma **Régua Visual** para facilitar a criação de pedidos sem erros de contagem.

```bash
cd frontend/project-anbima-frontend
npm install
npm start
```
Acesse: [http://localhost:4200](http://localhost:4200)

---

## 🛠️ Tecnologias Utilizadas

- **Backend**: Java 11, Spring Boot, Spring Data JPA, RabbitMQ.
- **Frontend**: Angular 18+, Signals, CSS Vanilla.
- **Banco de Dados**: PostgreSQL.
- **Documentação**: Swagger/OpenAPI.

---

## 📖 Endpoints e Monitoramento

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) (usuário: `guest` / senha: `guest`)
- **API Pedidos**: `GET /pedidos`, `POST /pedidos/posicional`

---

## ✨ Funcionalidades Destacadas

1.  **Assistente de Mapeamento**: O frontend converte inputs simples em strings de 40 caracteres com padding automático.
2.  **Régua Visual**: Barra colorida que indica fisicamente a posição (1-40) de cada campo no payload.
3.  **Processamento Granular**: Pedidos são consumidos da fila um a um através da interface de "Pedidos".
4.  **Cálculo de Desconto**: 10% de desconto automático para o combo `HAMBURGUER + CARNE + SALADA`.
