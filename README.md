# E-Commerce Order Management System

## Overview

A Spring Boot REST API backend for an E-Commerce Order Management System.

Features (built incrementally across commits):

- JWT Authentication
- Role Based Authorization (ADMIN, CUSTOMER, WAREHOUSE_STAFF)
- Product Catalog
- Shopping Cart
- Checkout with inventory reservation
- Inventory across Warehouses
- Payment Simulation
- Order Tracking (PLACED → CONFIRMED → PACKED → SHIPPED → DELIVERED → RETURNED)
- Returns & Refunds

---

## Tech Stack

- Java 21
- Spring Boot 3.4
- Spring Security + JWT
- Spring Data JPA
- H2 (default local) / MySQL (optional profile)
- Maven
- Lombok
- Validation
- JUnit + Mockito

---

## Project Status

**Commit 1 — Project setup** (current)

Foundation only: dependencies, package structure, database configuration.
Business APIs will be added in later commits.

---

## Roles

| Role | Capabilities |
|------|----------------|
| ADMIN | Manage products, warehouses, inventory, discounts |
| CUSTOMER | Browse, cart, checkout, track orders, returns |
| WAREHOUSE_STAFF | Update fulfillment / order status |

---

## Database

**Default (H2 in-memory)** — no install required; data resets on restart.

**MySQL** — create DB `ecommerce`, then run with profile `mysql`.

Tables (added in later commits):

- users, roles
- products, warehouses, inventory
- cart, cart_items
- orders, order_items, payments
- discounts, returns

---

## Assumptions

- Payment is simulated (no real payment gateway).
- Inventory is reserved atomically during checkout.
- Orders cannot be cancelled after shipping.
- Refunds are simulated.
- H2 is the default for easy local/Postman testing; MySQL profile is available for assignment parity.

---

## Prerequisites

- Java 21+
- Maven Wrapper included (`./mvnw`) — no global Maven install required
- (Optional) MySQL 8+ if using the `mysql` profile

---

## Running

```bash
# Default (H2)
./mvnw spring-boot:run

# MySQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

App URL: http://localhost:8080

---

## Testing

```bash
./mvnw test
```

---

## API Documentation

Postman collection will be added as APIs are implemented.
Public health check (Commit 1): `GET /api/health`

---

## Package Structure

```
com.example.ecommerce
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── util
```
