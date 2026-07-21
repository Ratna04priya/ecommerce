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

**Commit 10 — Returns, refunds, and discounts** (current)

Customers can return delivered orders (restock + simulated refund). Admins can
manage discount codes. Validation polish and tests come next.

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
- Inventory is reserved atomically during checkout (pessimistic locks + multi-warehouse allocation).
- Tax is a flat 10% on (subtotal − discount).
- Orders cannot be cancelled after shipping.
- Refunds are simulated.
- H2 is the default for easy local/Postman testing; MySQL profile is available for assignment parity.
- Downstream notification/audit/fulfillment routing runs asynchronously after checkout.

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

### Auth (Commit 4)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Register user (optional `role`) |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| GET | `/api/health` | Public | Health check |

Use header: `Authorization: Bearer <token>`

### Seeded demo users

| Email | Password | Role |
|-------|----------|------|
| admin@shop.com | admin123 | ADMIN |
| customer@shop.com | customer123 | CUSTOMER |
| warehouse@shop.com | warehouse123 | WAREHOUSE_STAFF |

### Products (Commit 5)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/products` | Public | List products (`category`, `activeOnly`) |
| GET | `/api/products/{id}` | Public | Get product by id |
| POST | `/api/products` | ADMIN | Create product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Soft-delete (sets `active=false`) |

Postman: **Authentication** + **Products** folders.

### Warehouses & Inventory (Commit 6)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/warehouses` | ADMIN | Create warehouse |
| GET | `/api/warehouses` | ADMIN, WAREHOUSE_STAFF | List warehouses |
| GET | `/api/warehouses/{id}` | ADMIN, WAREHOUSE_STAFF | Get warehouse |
| PUT | `/api/warehouses/{id}` | ADMIN | Update warehouse |
| GET | `/api/inventory` | ADMIN, WAREHOUSE_STAFF | List stock (`productId`, `warehouseId`) |
| PUT | `/api/inventory` | ADMIN | Upsert stock for product+warehouse |

### Cart (Commit 7)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/cart` | CUSTOMER | View cart |
| POST | `/api/cart/add` | CUSTOMER | Add/update item quantity |
| DELETE | `/api/cart/remove/{itemId}` | CUSTOMER | Remove cart item |

### Checkout (Commit 8)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/checkout` | CUSTOMER | Reserve stock, pay, create order, clear cart |

Tax = 10% after discount. Payment simulated as SUCCESS. Demo code: `SAVE10`.

### Orders (Commit 9)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/orders` | ADMIN / WAREHOUSE_STAFF / CUSTOMER | List orders (customer sees own) |
| GET | `/api/orders/{id}` | ADMIN / WAREHOUSE_STAFF / CUSTOMER | Get order details |
| PUT | `/api/orders/{id}/status` | roles vary | Advance/cancel status |

Lifecycle: `CONFIRMED → PACKED → SHIPPED → DELIVERED` (warehouse).  
Cancel allowed only before shipping. Shipping consumes reserved inventory.  
Returns use `POST /api/returns` (not status update).

### Returns & Discounts (Commit 10)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/returns` | CUSTOMER | Return delivered order + refund |
| GET | `/api/returns/order/{orderId}` | CUSTOMER / ADMIN / WAREHOUSE_STAFF | Get return |
| GET | `/api/discounts` | ADMIN | List discount codes |
| POST | `/api/discounts` | ADMIN | Create discount code |

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
