# 🎬 Cinema Management System

A full-stack, feature-rich web application for modern cinema operations. The platform provides separate, seamless experiences for moviegoers, cinema staff, and administrators, all within a single system.

**Backend:** Java Spring Boot | **Frontend:** React (TypeScript) | **Database:** PostgreSQL

## ✨ Features

### 👤 **User (Moviegoer)**
*   **Account Management:** Registration with email confirmation, secure login, password reset via email.
*   **Profile Control:** Edit personal information, change password or email.
*   **Movie Discovery:** Browse currently showing and upcoming movies with detailed views.
*   **Booking System:** Select a movie session, choose seats, and purchase tickets with full validation.
*   **Notifications:** Real-time feedback and error messages.

### ⚙️ **Admin (Cinema Manager)**
*   **Full Content Management (CRUD):** Create, read, update, and delete movies, genres, people (actors, directors), cinema halls, sessions, and promotions.
*   **User Management:** View all users, filter, and modify user roles (`USER`, `CASHIER`, `ADMIN`) and account status.
*   **Smart Automation:**
    *   Auto-generated SEO-friendly `slugs` for movies.
    *   Automatic movie status updates (`UPCOMING` → `CURRENT` → `ARCHIVED`) based on release dates.
    *   Automatic seat generation when creating a new cinema hall (by rows and seats).
*   **Advanced Validation:** Prevents session overlaps in the same hall and enforces business rules (e.g., cannot remove the last admin).

### 🎟️ **Cashier** *(Planned / In Development)*
*   **Staff Interface:** Dedicated view for fast ticket booking and reservation management.

### 🔧 **Technical Highlights**
*   **Role-Based Access Control (RBAC):** Secure API endpoints and UI elements for `USER`, `CASHIER`, and `ADMIN`.
*   **RESTful API:** Well-structured backend API built with Spring Boot.
*   **Modern Frontend:** Responsive and interactive UI built with React and TypeScript.