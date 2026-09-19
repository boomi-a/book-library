# BookLibrary

A full-stack book library management app. Each user manages their own personal library. An admin can manage all users.

**Stack:** React + Vite · Spring Boot 3.3 / Java 21 · PostgreSQL 16 · JWT · Docker

---

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java | 21+ | `java -version` |
| Node.js | 18+ | `node -v` |
| Docker Desktop | any | `docker -v` |
| Maven | 3.9+ (or use `./mvnw`) | `mvn -v` |

---

## Option A — Local Dev (recommended for development)

### Quick start

Run every development service with one command:

```bash
cd book-library
./scripts/start-dev.sh
```

This requires local PostgreSQL 15+, Java 21+, and Node.js 18+. It creates the `bookdb` database and `bookuser` role if needed, then seeds the local `admin@booklibrary.com` account. Press `Ctrl+C` to stop the API and frontend. Docker remains available as the production deployment option below.

### 1. Start PostgreSQL

```bash
cd book-library
docker compose up postgres -d
```

Wait a few seconds for the DB to initialize. The `init.sql` script creates tables and seeds the admin user automatically.

### 2. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**. Default profile is `dev` which connects to `localhost:5432/bookdb`.

### 3. Start the Frontend

```bash
cd frontend
npm install       # first time only
npm run dev
```

Open **http://localhost:5173** in your browser.

> The Vite dev server proxies all `/api` requests to `http://localhost:8080` — no CORS setup needed.

---

## Option B — Full Docker (all services in containers)

### 1. Create your `.env` file

```bash
cp .env.example .env
```

Edit `.env` and set a strong `JWT_SECRET` (at least 64 characters):

```env
POSTGRES_DB=bookdb
POSTGRES_USER=bookuser
POSTGRES_PASSWORD=change_me_strong_password
JWT_SECRET=replace_with_64_plus_random_characters_for_real_security_here_abc
```

### 2. Build and run all services

```bash
docker compose up --build
```

Open **http://localhost** in your browser.

> First startup takes 2-3 minutes (Maven downloads dependencies, React builds).
> Subsequent starts are fast because Docker layer-caches dependencies.

### 3. Stop and clean up

```bash
docker compose down          # stop containers, keep data
docker compose down -v       # stop containers + delete DB volume (wipes all data)
```

---

## Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@booklibrary.com` | `Admin@123` |

> **Change the admin password after first login** (currently no in-app password change — update directly in the DB or via the API).

Register new users at `/register` — they get the `USER` role by default.

---

## API Endpoints

### Auth (public)

| Method | Endpoint | Body |
|--------|----------|------|
| `POST` | `/api/auth/register` | `{ email, username, password }` |
| `POST` | `/api/auth/login` | `{ email, password }` |

### Books (requires JWT)

| Method | Endpoint | Notes |
|--------|----------|-------|
| `GET` | `/api/books` | Query params: `search`, `category`, `isRead` |
| `GET` | `/api/books/{id}` | Own books only |
| `POST` | `/api/books` | Create a book |
| `PUT` | `/api/books/{id}` | Update own book |
| `DELETE` | `/api/books/{id}` | Delete own book |

### Admin (requires ADMIN role)

| Method | Endpoint | Notes |
|--------|----------|-------|
| `GET` | `/api/admin/users` | List all users |
| `GET` | `/api/admin/users/{id}` | Get user by ID |
| `PATCH` | `/api/admin/users/{id}/activate` | Re-activate a user |
| `PATCH` | `/api/admin/users/{id}/deactivate` | Deactivate a user |

**All protected endpoints require:** `Authorization: Bearer <token>`

---

## Running Tests

```bash
cd backend
./mvnw test
```

8 integration tests run against an H2 in-memory database (no PostgreSQL needed).

---

## Project Structure

```
book-library/
├── backend/                        # Spring Boot (Maven)
│   ├── src/main/java/com/boomlibrary/
│   │   ├── config/SecurityConfig.java
│   │   ├── controller/             # AuthController, BookController, AdminController
│   │   ├── dto/                    # Request/response records
│   │   ├── entity/                 # User, Book JPA entities
│   │   ├── exception/              # GlobalExceptionHandler, ResourceNotFoundException
│   │   ├── repository/             # UserRepository, BookRepository
│   │   ├── security/               # JwtUtil, JwtFilter, UserDetailsServiceImpl
│   │   └── service/                # AuthService, BookService, UserService
│   └── src/main/resources/
│       ├── application.yml         # Active profile: dev
│       ├── application-dev.yml     # Local dev config
│       └── application-prod.yml    # Production (reads from env vars)
│
├── frontend/                       # React + Vite + TypeScript + Tailwind
│   └── src/
│       ├── api/                    # axios.ts, authApi, bookApi, adminApi
│       ├── components/             # BookCard, BookForm, BookFilter, Navbar, ProtectedRoute, UserTable
│       ├── pages/                  # Login, Register, Dashboard, AddBook, EditBook, Admin
│       ├── store/authStore.ts      # Zustand store with JWT persistence
│       └── types/                  # auth.types.ts, book.types.ts
│
├── docker/
│   └── postgres/init.sql           # Schema DDL + seed admin user
├── docker-compose.yml              # Local dev orchestration
├── docker-compose.prod.yml         # Cloud-ready (no DB port exposed, resource limits)
└── .env.example                    # Copy to .env and fill in secrets
```

---

## Cloud Deployment

The app is cloud-agnostic. Use `docker-compose.prod.yml` as the base.

**General steps for any cloud provider:**

1. Push images to a container registry (ECR / GCR / ACR / Docker Hub)
2. Provision a managed PostgreSQL database and run `docker/postgres/init.sql` on it
3. Deploy containers via your chosen orchestrator (ECS / Cloud Run / Azure Container Apps / Kubernetes)
4. Set all environment variables from a secrets manager (never hardcode)
5. Point a load balancer or CDN to the frontend container on port 80
6. Enable HTTPS at the load balancer level

**Key env vars for production:**

```env
DB_URL=jdbc:postgresql://<host>:5432/<db>
DB_USERNAME=<user>
DB_PASSWORD=<password>
JWT_SECRET=<64+ random chars>
```

---

## Book Fields

| Field | Type | Notes |
|-------|------|-------|
| Book Name | Text (required) | |
| Author | Text | |
| Publisher | Text | |
| Category | Text | e.g. Fiction, Technology, Science |
| Date Bought | Timestamp | |
| Read | Boolean | Toggle in form or card |
| Comments | Long text | Personal notes |
