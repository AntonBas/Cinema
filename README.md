# Cinema Management System

> Designed as a backend-focused system to explore concurrency, asynchronous workflows, and real-world business constraints.

![Booking Demo](docs/images/booking.gif)

Production-like full-stack cinema booking platform focused on real-world backend challenges: concurrency control, asynchronous workflows, and complex business rules.

**Backend:** Java Spring Boot  
**Frontend:** React (TypeScript)  
**Database:** PostgreSQL

---

## Overview

Cinema Management System simulates real cinema operations with a focus on consistency, concurrency, and complex domain logic:

- Concurrency-safe seat booking
- Role-based access control (RBAC)
- Async payment & refund workflows
- Configurable bonus & loyalty system
- Admin panel with audit logging

---

## Key Engineering Features

- **Concurrency-safe booking**
  - Temporary seat locking (5 min)
  - Booking reservation window (30 min)
  - Prevents race conditions and overselling

- **RBAC (4 roles)**
  - USER, CASHIER, CONTENT_MANAGER, ADMIN
  - Secured API + UI

- **Async payment integration**
  - External provider (LiqPay)
  - Callback-based status updates
  - Idempotent handling

- **Scheduler-driven automation**
  - Session/movie status updates
  - Promotion lifecycle management

- **Flexible business rules**
  - Bonus system (welcome, birthday, accrual)
  - Dynamic refund calculation

- **Audit logging**
  - Tracks all admin actions

---

## Architecture

**Backend**

- Layered architecture (Controller → Service → Repository)
- DTO + MapStruct
- Stateless authentication (JWT)
- Scheduled jobs

**Frontend**

- Component-based architecture
- Feature-based structure
- Context API for state management

---

## System Architecture

```mermaid
C4Context
    title System Context Diagram for Cinema Management System

    Person(user, "User", "Cinema visitor")
    Person(admin, "Admin/Staff", "Cinema employee")

    System(cinema, "Cinema Management System", "Handles bookings, payments, and cinema operations")

    System_Ext(liqpay, "LiqPay", "Payment provider")
    System_Ext(google, "Google OAuth", "Authentication")
    System_Ext(smtp, "Email Service", "Notifications")

    Rel(user, cinema, "Browse movies, book tickets, make payments", "HTTPS")
    Rel(admin, cinema, "Manage content, process tickets", "HTTPS")
    Rel(cinema, liqpay, "Process payments and refunds", "REST API")
    Rel(cinema, google, "OAuth2 authentication", "OAuth2")
    Rel(cinema, smtp, "Send confirmation emails", "SMTP")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")

---

## Engineering Decisions

### Seat Booking Concurrency

To prevent double booking, a temporary seat lock mechanism is used:

- First-level lock (5 minutes) on seat selection
- Second-level reservation (30 minutes) before payment
- Expired locks are released via scheduler

### Payment Flow

- External payment handled via LiqPay
- System processes async callbacks
- Booking updates are idempotent to avoid duplicate state changes

### Bonus System

- Implemented as a configurable rule engine
- Allows dynamic adjustment without code changes

---

## Tech Stack

### Backend

- Java 21, Spring Boot 3
- Spring Security, JPA
- PostgreSQL
- Flyway
- Bucket4j (rate limiting)
- Caffeine (caching)

### Frontend

- React + TypeScript
- Vite
- Axios
- Styled Components

### DevOps

- Docker / Docker Compose

---

## Quick Start

```bash
git clone https://github.com/AntonBas/Cinema.git
cd Cinema
cp .env.docker.example .env.docker
docker-compose up -d
````

| Service     | URL                                   |
| ----------- | ------------------------------------- |
| Frontend    | http://localhost:5173                 |
| Backend API | http://localhost:8080/api             |
| Swagger UI  | http://localhost:8080/swagger-ui.html |

---

## Demo

Video walkthrough — coming soon

---

## Documentation

Full feature documentation, flows, and technical details:

[docs/DOCS.md](docs/DOCS.md)

---

## Test Accounts

| Email            | Password | Role            |
| ---------------- | -------- | --------------- |
| admin@test.com   | admin    | ADMIN           |
| manager@test.com | manager  | CONTENT_MANAGER |
| cashier@test.com | cashier  | CASHIER         |
| user@test.com    | user     | USER            |

---

## Highlights

- Designed as a real-world backend system, not just CRUD
- Focus on consistency, concurrency, and scalability
- Covers full lifecycle: booking → payment → refund → audit
