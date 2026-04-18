# 🎬 Cinema Management System

A full-stack, feature-rich web application for modern cinema operations. The platform provides separate, seamless experiences for moviegoers, cinema staff, and administrators, all within a single system.

**Backend:** Java Spring Boot | **Frontend:** React (TypeScript) | **Database:** PostgreSQL

## ✨ Features

### 👤 User Features

#### 🔐 Authentication & Security

**Registration**

- Email validation (unique, no duplicate accounts)
- Password validation (length, complexity)
- Email confirmation via verification link
- Account locked until email is verified
- Welcome bonus automatically awarded after email verification
- Bonus card automatically created upon email verification
- Password hashed with BCrypt

**Login**

- JWT token generation
- Blocked for unverified accounts
- OAuth2 login via Google

**Password Recovery**

- Request reset via email
- Reset link sent to email
- New password validation (cannot reuse old password)
- Blocked for unverified accounts

---

#### 🏠 Homepage

- **Now Showing** — 6 recently released movies currently playing
- **Coming Soon** — 6 movies releasing in the near future
- **Last Chance** — 6 movies ending their run within the next 7 days
- **Special Offers** — active promotions available for claiming
  - User clicks "Claim" to receive bonus points on their bonus card
  - Each promotion can only be claimed once per user

---

#### 🎬 Movies

Two sections for movie discovery:

**Now Playing**

- All movies with `CURRENT` status
- Click on any movie to view details and session schedule

**Coming Soon**

- All movies with `UPCOMING` status
- Click on any movie to view details and session schedule (sessions can be available for advance booking)

**Movie Detail Page**

- Full movie information: title, description, duration, age rating, genre, cast (actors, directors)
- Session schedule organized by date
- Quick access to booking for selected session
- Advance booking available for upcoming movies

---

#### 📅 Schedule

Complete session listing with powerful filtering:

- All scheduled sessions across all movies and halls
- **Custom Calendar:**
  - Visual indicator showing which dates have available sessions
  - When searching for a specific movie, calendar updates to show only dates with sessions for that movie
- **Search:** Find sessions by movie title
- Click on any session to proceed to booking

---

#### 🎟️ Booking Process

Step-by-step ticket booking with seat reservation and secure payment.

**1. Seat Selection**

- Visual cinema hall layout with color-coded seats:
  - Available seats
  - Reserved/Booked seats
  - Selected seats
- Seat types visible with different colors/styles (Standard, VIP, Couple)
- **First-level reservation:** Clicking a seat locks it for **5 minutes** (prevents others from selecting it)

**2. Ticket Type Selection**

- Choose ticket type for each selected seat
- Available ticket types with their price multipliers
- Price updates automatically based on selected types

**3. Bonuses & Discounts**

- Apply bonus points to reduce total price
- Bonus usage limited by min/max rules configured by admin
- Final price calculated with all discounts applied

**4. Booking Confirmation**

- Click **"Book Now"** to confirm selection
- **Second-level reservation:** Seats are booked for **30 minutes** (time to complete payment)
- Redirect to booking summary page

**5. Booking Summary**

- Complete booking details displayed:
  - Movie title
  - Session date and time
  - Cinema hall
  - Selected seats
  - Ticket types per seat
  - Total price
  - Bonus points applied (if any)
- Options:
  - **Cancel** — release seats and cancel booking
  - **Proceed to Payment** — continue to payment page

**6. Payment**

- Select payment method (card via LiqPay)
- Redirect to LiqPay secure payment page
- After payment:
  - **Success** — confirmation page displayed
  - **Processing** — waiting page if payment status is pending
- Payment status automatically updates via scheduler

**7. Booking Completion**
Upon successful payment:

- Confirmation email sent to user with ticket details
- Tickets available in **My Tickets** section
- Each ticket includes QR code for cinema entry
- Bonus points awarded based on Payment Accrual rule

---

#### 💰 Refund

Ticket refund process available from **My Tickets** section:

**1. Initiate Refund**

- Navigate to **My Tickets** → **Active** tab
- Select ticket(s) to refund
- Click **"Refund"** button

**2. Refund Request**

- Select refund reason from dropdown
- System calculates refundable amount based on:
  - Time until session start (closer to session = lower refund)
  - Refund rules configured by admin
- Preview shows refundable amount

**3. Confirm Refund**

- User confirms refund request
- Request sent to payment provider (LiqPay)
- Refund processed back to original payment card

**4. Refund Status**

- Ticket status changes to `REFUNDED`
- Refunded tickets moved to **Refunded** tab
- Bonus points used in booking are deducted from user's balance
- Refund transaction appears in bonus history (if applicable)

---

#### 👤 My Account

**Profile Information**

- View personal details: first name, last name, birth date, phone, email, city
- Edit first name, last name, birth date, phone, city
- **Birth Date Verification Warning:** If birth date is already verified and user attempts to change it, system warns that verification will be lost and Birthday Bonus eligibility will be removed

**My Tickets**

- List of all purchased tickets with status tabs:
  - **All** — all tickets
  - **Active** — upcoming valid tickets
  - **Used** — attended sessions
  - **Refunded** — returned tickets
- **Ticket Actions:**
  - View ticket details (movie, session time, hall, seat, ticket type, price)
  - Open QR code for cinema entry
  - Request refund (subject to refund rules)

**My Bonus**

- Bonus card with current point balance
- Display of min/max points allowed per booking
- Two tabs:
  - **Balance** — current bonus overview
  - **Transactions** — complete history of all bonus transactions (+ earned, - spent)

**Security**

- **Change Password:** Requires current password, new password (entered twice), validation that new ≠ old
- **Change Email:** Enter new email + current password, confirmation link sent to new email

---

### ⚙️ Admin Features

#### 📊 Dashboard

Overview of key metrics and recent system activity.

---

#### 🎬 Movies

Three tabs for complete movie content management:

**Movies Tab**

- Full CRUD operations
- Unique name validation — cannot create duplicate movie names
- Protected deletion — cannot delete a movie if it is linked to any session
- Auto-generated SEO-friendly slug from title
- Status automatically updates via scheduler:
  - `UPCOMING` — release date is in the future
  - `CURRENT` — release date has passed, end date not yet reached
  - `ARCHIVED` — end date has passed
- Search by movie title
- Pagination

**Genres Tab**

- Full CRUD operations
- Unique name validation — cannot create duplicate genre names
- Protected deletion — cannot delete a genre if it is linked to any movie
- Counter displays number of movies for each genre
- Sorting by movie count
- Search by genre name
- Pagination

**People Tab**

- Full CRUD operations
- Role selection: Actor, Director, Screenwriter
- Unique name validation
- Protected deletion — cannot delete a person if linked to any movie
- Counter displays number of movies for each person
- Sorting by movie count
- Search by person name
- Pagination

---

#### ⏰ Schedule

Full session management for movie screenings:

- Full CRUD operations
- **Smart Movie Filtering:** When creating a session, only movies available on the selected date are shown
- **Hall Conflict Validation:** Cannot schedule overlapping sessions in the same hall (considers movie duration)
- Session status automatically updates via scheduler:
  - `SCHEDULED` — session is in the future
  - `UPCOMING` — session is currently playing
  - `COMPLETED` — session has finished
  - `CANCELED` — manually canceled by admin
- Filter sessions by:
  - Date range (from — to)
  - Cinema hall
  - Status
- Search by movie title
- Pagination

---

#### 🎭 Halls

Cinema hall management with visual layout editor:

- Full CRUD operations
- Unique hall name validation
- **Auto-generation:** Admin specifies number of rows and seats per row, system generates the layout
- **Automatic VIP rows:** Last 2 rows are always VIP by default
- Option to make entire hall VIP
- Option to designate specific rows as couple seats (double seats)
- **Validation for couple rows:** Requires even number of seats per row
- **Price tiers based on seat type:**
  - `STANDARD` — base price
  - `VIP` — premium price
  - `COUPLE` — premium price
- **Interactive Layout Editor (Modal):**
  - Visual representation of the hall layout
  - **Left-click** — change seat type (Standard ↔ VIP ↔ Couple)
  - **Right-click** — deactivate/activate seat (e.g., for maintenance)
- Protected from edit/delete if hall has future scheduled sessions

---

#### 👥 Users

Complete user management interface:

- View all registered users
- **User information displayed:** Name, email, role, verification status, block status, ticket count, last activity
- **Actions:**
  - Change user role (`USER`, `CASHIER`, `Content Manager`, `ADMIN`)
  - Verify user's birth date (enables Birthday Bonus eligibility)
  - Block / unblock user account
- **Security validations:**
  - Admin cannot block themselves
  - Cannot remove `ADMIN` role from the last remaining administrator
- **Filters:**
  - By role
  - By verification status
  - By block status
- **Search:** By email or name
- Pagination

---

#### 🎁 Bonus

System-wide bonus program configuration:

- Configure four bonus rules:
  - **Welcome Bonus** — points awarded to user after successful email verification
  - **Birthday Bonus** — points awarded automatically on user's birthday (requires verified birth date). Applied via scheduler
  - **Booking Spend** — minimum and maximum points a user can redeem per booking
  - **Payment Accrual** — percentage of ticket purchase amount returned as bonus points after successful payment
- Admin can update any rule value
- **Reset button** — restores all rules to default values defined in backend

---

#### 📢 Promotion

Bonus point promotions management:

- Full CRUD operations
- Unique promotion title validation
- Status automatically updates via scheduler:
  - `UPCOMING` — promotion is scheduled (start date in future)
  - `ACTIVE` — promotion is currently active (current date between start and end)
  - `EXPIRED` — promotion has ended (end date passed)
- Expired promotions can be reactivated by updating dates (useful for recurring events like New Year)
- Search by promotion title
- Pagination

---

#### 🎫 Ticket Types

Flexible ticket type configuration:

- Full CRUD operations
- Unique name validation
- **Fields:**
  - Name (e.g., "Child", "Standard", "Military", "Student")
  - Category (grouping of similar types)
  - Price multiplier (e.g., 0.5 = 50% discount off base price)
  - Document required flag (indicates if verification is needed at cinema entrance)
  - Active status (can be toggled on/off)
- Deactivated ticket types are hidden during booking
- Sorting by category
- Pagination

---

#### 📋 Audit Logs

Complete history of all administrative actions:

- Tracks every change made by admins across the system
- **Log entry details:**
  - **Time** — timestamp when change occurred
  - **Changed By** — email of the admin who made the change
  - **Target** — name of the entity that was modified
  - **Action** — type of operation: `CREATE`, `UPDATE`, or `DELETE`
  - **Changes** — brief description of what was changed
- **Filters:**
  - By entity type (Movie, Genre, Person, Session, Hall, User, Promotion, Ticket Type, etc.)
  - By action type (CREATE, UPDATE, DELETE)
- **Search:** By admin email (shows all actions performed by specific admin)
- Pagination

---

### 🔧 Technical Highlights

- **Role-Based Access Control (RBAC):** Secure API endpoints and UI elements for `USER`, `CASHIER`, and `ADMIN`
- **Rate Limiting:** API protection against brute-force and DDoS attacks
- **RESTful API:** Well-structured backend API built with Spring Boot
- **Modern Frontend:** Responsive and interactive UI built with React and TypeScript

## 🛠 Tech Stack

### Backend

| Technology           | Version |
| :------------------- | :------ |
| Java                 | 17      |
| Spring Boot          | 3.5.13  |
| Spring Security      | 6.x     |
| Spring Data JPA      | 3.x     |
| Spring OAuth2 Client | 3.x     |
| Spring Mail          | 3.x     |
| Spring Cache         | 3.x     |
| Spring Actuator      | 3.x     |
| PostgreSQL           | 15+     |
| Flyway               | 11.5.0  |
| JWT (jjwt)           | 0.12.6  |
| MapStruct            | 1.5.5   |
| Lombok               | 1.18.36 |
| Bucket4j             | 8.10.1  |
| Caffeine Cache       | 3.x     |
| ZXing (QR Code)      | 3.5.3   |
| Gson                 | 2.x     |
| SpringDoc OpenAPI    | 2.7.0   |
| Dotenv               | 4.0.0   |

### Frontend

| Technology        | Version |
| :---------------- | :------ |
| React             | 19.1.1  |
| TypeScript        | 5.8.3   |
| Vite              | 7.3.2   |
| React Router DOM  | 7.8.1   |
| Axios             | 1.15.0  |
| Lucide React      | 0.563.0 |
| Styled Components | 6.1.19  |
| date-fns          | 4.1.0   |
| clsx              | 2.1.1   |

### DevOps & Tools

| Technology     | Description                   |
| :------------- | :---------------------------- |
| Docker         | Containerization              |
| Docker Compose | Multi-container orchestration |
| Flyway         | Database migrations           |
| Maven          | Build automation              |

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Node.js 20+** and npm
- **Docker** and **Docker Compose** (recommended)
- **Maven** (for backend builds)

---

### Option 1: Docker Setup (Recommended)

The easiest way to run the entire stack with a single command.

**1. Clone the repository**
`git clone https://github.com/AntonBas/Cinema.git`
`cd Cinema`

**2. Configure environment variables**

Copy `.env.docker.example` to `.env.docker` and fill in the required values.  
See [`.env.docker.example`](.env.docker.example) for all available variables.

**3. Start all services**
`docker-compose up -d`

**4. Access the application**

| Service         | URL                                   |
| :-------------- | :------------------------------------ |
| Frontend        | http://localhost:5173                 |
| Backend API     | http://localhost:8080/api             |
| Swagger UI      | http://localhost:8080/swagger-ui.html |
| Ngrok Inspector | http://localhost:4040                 |

**5. Stop services**
`docker-compose down`

---

### Option 2: Local Development Setup

Run backend and frontend separately for faster development.

#### Backend Setup

`cd backend`

Copy environment configuration:
`cp .env.example .env`

Edit `.env` with your local values.
See [`backend/.env.example`](backend/.env.example) for all available variables.

Start PostgreSQL via Docker:
`cd ..`
`docker-compose up -d postgres`

Run backend:
`cd backend`
`./mvnw spring-boot:run`

Backend will be available at: http://localhost:8080

#### Frontend Setup

`cd frontend`

Install dependencies:
`npm install`

Create environment file:
`echo "VITE_API_URL=http://localhost:8080" > .env`

Start development server:
`npm run dev`

Frontend will be available at: http://localhost:5173

---

### Database Migrations

Flyway migrations run automatically on application startup. Migration files are located at:
`backend/src/main/resources/db/migration/`

To reset the database:
`docker-compose down -v postgres`
`docker-compose up -d postgres`

## 📚 API Documentation

Interactive API documentation is available via Swagger UI:
`http://localhost:8080/swagger-ui.html`

## 📁 Project Structure

### Backend

backend/src/main/java/ua/lviv/bas/cinema/
├── config/
│ ├── api/
│ ├── audit/
│ ├── cache/
│ ├── jackson/
│ ├── properties/
│ ├── ratelimit/
│ ├── scheduling/
│ └── security/
│ ├── jwt/
│ ├── oauth2/
│ └── user/
├── controller/
│ ├── admin/
│ └── api/
├── domain/
│ ├── audit/
│ ├── bonus/
│ ├── booking/
│ ├── cinema/
│ ├── promotion/
│ ├── ticket/
│ ├── token/
│ └── user/
├── dto/
│ ├── audit/
│ ├── bonus/
│ ├── booking/
│ ├── hall/
│ ├── movie/
│ ├── payment/
│ ├── promotion/
│ ├── refund/
│ ├── session/
│ ├── ticket/
│ ├── ticketType/
│ └── user/
├── exception/
│ ├── api/
│ ├── core/
│ ├── domain/
│ └── infrastructure/
├── mapper/
├── repository/
├── scheduler/
└── service/

### Frontend

frontend/src/
├── api/ # API service functions
├── components/
│ ├── account/ # User account components
│ ├── admin/ # Admin panel components
│ │ ├── AdminLayout/
│ │ ├── SectionAuditLogs/
│ │ ├── SectionBonus/
│ │ ├── SectionDashboard/
│ │ ├── SectionHalls/
│ │ ├── SectionMovies/
│ │ ├── SectionPromotion/
│ │ ├── SectionSchedule/
│ │ ├── SectionTicketType/
│ │ └── SectionUsers/
│ ├── auth/ # Authentication forms
│ ├── booking/ # Booking flow components
│ ├── home/ # Homepage sections
│ ├── layout/ # Header, Footer, Layout
│ ├── movies/ # Movie cards and lists
│ ├── sessions/ # Session filters and calendar
│ └── ui/ # Reusable UI components
├── context/ # React Context providers
├── hooks/
│ ├── common/ # Generic hooks
│ └── features/ # Feature-specific hooks
├── pages/
│ ├── account/ # Profile, Tickets, Bonus, Security
│ ├── auth/ # Login, Register, Password reset
│ ├── booking/ # Booking, Payment, Success
│ ├── home/ # Homepage
│ ├── movies/ # Current, Upcoming, Detail
│ └── sessions/ # Schedule page
├── routes/ # Route guards (Admin, Protected, Public)
├── services/ # Axios instance with interceptors
├── types/ # TypeScript type definitions
└── utils/ # Utility functions
