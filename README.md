# 🎬 Cinema Management System

Full-stack cinema booking platform with real-world business logic — concurrency-safe seat reservation, bonus system, refund processing, and admin panel.

**Backend:** Java Spring Boot | **Frontend:** React (TypeScript) | **Database:** PostgreSQL

---

## 🚀 Overview

Cinema Management System is designed as a production-like application that simulates real cinema operations:

- Real-time seat reservation with locking mechanism
- Multi-role system
- Bonus & loyalty program
- Payment and refund workflow
- Full admin panel for system management

---

## 🧠 Key Engineering Features

- **Concurrency-safe seat booking**
  - 5-minute temporary seat lock
  - 30-minute booking window before payment

- **Role-Based Access Control (RBAC)**
  - USER, CASHIER, CONTENT_MANAGER, ADMIN
  - Secured API and UI

- **Scheduler-driven state management**
  - Movies, sessions, promotions auto-update statuses

- **Bonus & loyalty system**
  - Configurable rules (welcome, birthday, accrual, spending limits)

- **Refund processing system**
  - Dynamic refund calculation based on time before session

- **Audit logging**
  - Full tracking of admin actions

- **Rate limiting**
  - API protection against brute-force/DDoS

- **Payment integration**
  - External provider (LiqPay)
  - Async status updates

---

## 👥 User Features

- Authentication (JWT + OAuth2 Google)
- Movie browsing (Now Showing / Coming Soon)
- Session search with filters and calendar
- Seat selection with visual layout
- Ticket booking with multiple ticket types
- Bonus usage for discounts
- Secure payment flow
- Ticket management (QR codes, status tracking)
- Refund requests with calculated return amount
- Profile & account management

---

## 👔 Staff Features

### Cashier

- User verification (email, birth date)
- Ticket scanning via QR code
- Ticket status management (mark as used)

### Content Manager

- Full access to Movies, Genres, Persons (CRUD)
- Schedule management with conflict detection
- Cinema Halls management with interactive seat editor
- Promotions — time-based bonus campaigns

---

## ⚙️ Admin Features

Full system access, including all of the above plus:

- **User Management**
  - Role management (assign/change roles)
  - Account blocking / unblocking
  - Birth date verification

- **Bonus Configuration**
  - Flexible rule system (welcome, birthday, spending limits, accrual %)

- **Ticket Types**
  - Dynamic pricing via multipliers
  - Categories and active/inactive toggling

- **Audit Logs**
  - Full system change history — who did what and when

---

## 🏗 Architecture

### Backend

- Layered architecture: Controller → Service → Repository
- DTO pattern with MapStruct
- Domain-oriented package structure
- Stateless authentication (JWT)
- Scheduled jobs for automation
- Centralized exception handling

### Frontend

- Component-based architecture
- Feature-based folder structure
- Global state via Context API
- API layer abstraction (Axios)
- Reusable UI components

---

## 🛠 Tech Stack

### Backend

| Technology      | Version |
| :-------------- | :------ |
| Java            | 21      |
| Spring Boot     | 3.4.7   |
| Spring Security | 6.4.7   |
| Spring Data JPA | 3.4.7   |
| PostgreSQL      | 15+     |
| Flyway          | 11.7.0+ |
| JWT (jjwt)      | 0.12.6  |
| MapStruct       | 1.6.3   |
| Bucket4j        | 8.10.1  |
| Caffeine Cache  | 3.1.8   |
| ZXing (QR Code) | 3.5.3   |

### Frontend

| Technology        | Version |
| :---------------- | :------ |
| React             | 19.1.1  |
| TypeScript        | 5.8.3   |
| Vite              | 7.3.2   |
| React Router      | 7.8.1   |
| Axios             | 1.15.0  |
| Styled Components | 6.1.19  |

### DevOps

- Docker / Docker Compose
- Maven

---

## 🚀 Quick Start

**Prerequisites:** Docker and Docker Compose

**1. Clone the repository**

```bash
git clone https://github.com/AntonBas/Cinema.git
cd Cinema
```

2. Configure environment variables

```bash
cp .env.docker.example .env.docker
```

Edit .env.docker with your actual values. See [.env.docker.example](.env.docker.example) for all available variables.

3. Start the application

```bash
docker-compose up -d
```

| Service     | URL                                   |
| :---------- | :------------------------------------ |
| Frontend    | http://localhost:5173                 |
| Backend API | http://localhost:8080/api             |
| Swagger UI  | http://localhost:8080/swagger-ui.html |

## 🧪 Test Accounts

| Email            | Password | Role            |
| :--------------- | :------- | :-------------- |
| admin@test.com   | admin    | ADMIN           |
| manager@test.com | manager  | CONTENT_MANAGER |
| cashier@test.com | cashier  | CASHIER         |
| user@test.com    | user     | USER            |

---

## 📚 Full Documentation

Complete feature descriptions, screenshots, and technical details — [docs/DOCS.md](docs/DOCS.md)

---

## 📈 What I Learned

- Designing concurrency-safe booking systems
- Implementing flexible business rules (bonus/refund)
- Building scalable RBAC systems
- Structuring full-stack applications for maintainability
- Handling async payment workflows
- Managing complex domain logic in real-world scenarios

---

## 🎥 Demo

[Watch on YouTube](in-progress)

## 📸 Screenshots

in progress

```

```
