# 🎬 Cinema Management System

A full-stack, feature-rich web application for modern cinema operations. The platform provides separate, seamless experiences for moviegoers, cinema staff, and administrators, all within a single system.

**Backend:** Java Spring Boot | **Frontend:** React (TypeScript) | **Database:** PostgreSQL

## ✨ Features

### 👤 **User (Moviegoer)**

- **Account Management:** Registration with email confirmation, secure login, password reset via email.
- **Profile Control:** Edit personal information, change password or email.
- **Movie Discovery:** Browse currently showing and upcoming movies with detailed views.
- **Booking System:** Select a movie session, choose seats, and purchase tickets with full validation.
- **Notifications:** Real-time feedback and error messages.

### ⚙️ **Admin (Cinema Manager)**

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
  - **Welcome Bonus** — points awarded to user after successful email verification. Bonus card is created automatically upon verification.
  - **Birthday Bonus** — points awarded automatically on user's birthday (requires verified birth date). Applied via scheduler.
  - **Booking Spend** — minimum and maximum points a user can redeem per booking
  - **Payment Accrual** — percentage of ticket purchase amount returned as bonus points after successful payment
- Admin can update any rule value
- **Reset button** — restores all rules to default values defined in backend
- Changes persist in database (survive server restart)

---

#### 📢 Promotion

Bonus point promotions management:

- Full CRUD operations
- Unique promotion title validation
- **Fields:** Title, description, start date, end date, bonus points amount
- Status automatically updates via scheduler:
  - `UPCOMING` — promotion is scheduled (start date in future)
  - `ACTIVE` — promotion is currently active (current date between start and end)
  - `EXPIRED` — promotion has ended (end date passed)
- Only `ACTIVE` promotions are visible to users on homepage
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

### 🎟️ **Cashier** _(Planned / In Development)_

- **Staff Interface:** Dedicated view for fast ticket booking and reservation management.

### 🔧 **Technical Highlights**

- **Role-Based Access Control (RBAC):** Secure API endpoints and UI elements for `USER`, `CASHIER`, and `ADMIN`.
- **RESTful API:** Well-structured backend API built with Spring Boot.
- **Modern Frontend:** Responsive and interactive UI built with React and TypeScript.
