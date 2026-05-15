# Cinema Management System

> A concurrency-safe cinema booking system designed to handle real-world race conditions, asynchronous payment workflows, and complex business rules such as refunds and loyalty systems.

![Booking Demo](docs/images/booking.gif)

## Overview

This project simulates a real-world cinema platform with a focus on backend engineering challenges rather than simple CRUD operations.

It models the full lifecycle of a booking system:
**seat selection → reservation → payment → refund → audit logging**

---

## Engineering Focus

The system is designed to solve non-trivial backend problems:

- Preventing **double booking under concurrency**
- Handling **async payment callbacks** safely (idempotency)
- Managing **time-based reservations and expirations**
- Designing a **flexible bonus/loyalty rule system**
- Ensuring **data consistency across distributed flows**

---

## Key Features

- **Concurrency-safe booking**
  - Two-phase reservation system (5-min lock + 30-min booking window)
  - Prevents race conditions and overselling

- **Async payment integration**
  - External provider (LiqPay)
  - Callback-based updates
  - Idempotent state handling

- **RBAC (4 roles)**
  - USER, CASHIER, CONTENT_MANAGER, ADMIN
  - Secured API and UI

- **Scheduler-driven automation**
  - Expired lock cleanup
  - Session & promotion lifecycle management

- **Bonus system**
  - Configurable rule engine (no code changes required)

- **Audit logging**
  - Full trace of admin actions

---

## Architecture

### High-Level System Overview

```mermaid
flowchart TD

%% ========== CLIENT ==========
A[React Frontend]

%% ========== BACKEND ==========
A --> B[Spring Boot API]

B --> C[Auth / Security Layer]
B --> D[Booking Service]
B --> E[Payment Service]
B --> F[Bonus Service]
B --> G[Refund Service]
B --> H[Admin Services]

%% ========== DATABASE ==========
C --> DB[(PostgreSQL)]
D --> DB
E --> DB
F --> DB
G --> DB
H --> DB

%% ========== CONCURRENCY FLOW ==========
D --> L[Seat Lock Manager<br/>5 min temporary lock]
L --> R[Reservation Window<br/>30 min hold]

%% ========== PAYMENT FLOW ==========
E --> P[LiqPay API]
P --> E

E --> CB[Async Callback Handler]
CB --> E

%% ========== SCHEDULER ==========
S[Scheduler Jobs]

S --> SL[Release expired locks]
S --> SB[Cancel unpaid bookings]
S --> SS[Update session status]

SL --> DB
SB --> DB
SS --> DB

%% ========== CROSS-CUTTING ==========
C --> AUDIT[Audit Logging]
D --> AUDIT
E --> AUDIT
H --> AUDIT
```

**Backend**

- Layered architecture (Controller → Service → Repository)
- DTO + MapStruct
- Stateless authentication (JWT)

**Frontend**

- React (TypeScript)
- Feature-based structure

**Infrastructure**

- PostgreSQL
- Docker / Docker Compose

---

## Engineering Decisions

### Concurrency Control

To prevent double booking, a two-phase locking mechanism is used:

- **Phase 1:** Temporary seat lock (5 minutes) during selection
- **Phase 2:** Reservation window (30 minutes) before payment
- **Cleanup:** Expired locks are released via scheduled jobs

This approach prevents race conditions during concurrent seat selection while balancing consistency guarantees with user experience.

---

### Payment Flow

- External payments handled via LiqPay
- System processes **async callbacks**
- Updates are **idempotent** to prevent duplicate state transitions
- Scheduler acts as a fallback for missed callbacks

---

### Bonus System

Implemented as a **configurable rule engine**, allowing dynamic updates without code changes.

---

## Tech Stack

**Backend**

- Java 21, Spring Boot 3
- Spring Security, JPA
- PostgreSQL, Flyway
- Bucket4j, Caffeine

**Frontend**

- React + TypeScript
- Vite, Axios

**DevOps**

- Docker / Docker Compose

---

## Quick Start

```bash
git clone https://github.com/AntonBas/Cinema.git
cd Cinema
cp .env.docker.example .env.docker
docker-compose up -d
```

| Service     | URL                                   |
| ----------- | ------------------------------------- |
| Frontend    | http://localhost:5173                 |
| Backend API | http://localhost:8080/api             |
| Swagger UI  | http://localhost:8080/swagger-ui.html |

---

## Documentation

Detailed flows, UI behavior, and full feature descriptions:

[docs/DOCS.md](docs/DOCS.md)

---

## Highlights

- Designed as a real-world backend system, not just CRUD
- Focus on consistency, concurrency, and scalability
- Covers full lifecycle: booking → payment → refund → audit
