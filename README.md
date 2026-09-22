# Event & Movie Booking System

A highly concurrent, microservices-based backend system for managing events, users, and bookings. Designed to handle high loads, it features strict concurrency controls (to prevent double-booking) and asynchronous messaging (for notifications). 

## 🏗️ Architecture & Design Decisions

### Microservices Breakdown
1. **API Gateway (Port 8080)**: Single entry point. Centralizes routing, JWT validation, and RBAC (Role-Based Access Control) to strictly separate Customers and Vendors/Admins. Strips spoofed `X-User-*` headers.
2. **Discovery Service (Port 8084)**: Eureka-based service registry. All services register here to allow the API Gateway to route traffic dynamically without hardcoded IPs.
3. **User Service (Port 8081)**: Manages authentication, BCrypt password hashing, and JWT Access/Refresh tokens (with rotation) for ultimate security.
4. **Catalog Service (Port 8082)**: Manages inventory (Theatres, Screens, Shows, and Seats). Access is tightly scoped: Vendors can only modify their *own* theatres.
5. **Booking Service (Port 8083)**: The core transaction engine. Features **Pessimistic Locking** (`SELECT ... FOR UPDATE`) ordered numerically by Seat ID to explicitly prevent database deadlocks under extreme concurrency when hundreds of users target the exact same seat.
6. **Notification Service (Background/Async)**: Listens to RabbitMQ events (`BOOKING_CONFIRMED`, `BOOKING_CANCELLED`) and delegates email-sending via SMTP (MailHog).

### Data & Async Processing
- **PostgreSQL**: Used for all persistent data across Catalog, User, and Booking services. Schemas are separated logically.
- **RabbitMQ**: Message broker utilized to decouple the heavy booking process from the slow email-sending process. Ensures zero-data-loss for notifications.
- **Flyway**: Handles all database schema migrations automatically on startup.

## 🚀 Getting Started

### 1. Start the Full Stack
Each service has its own `Dockerfile` and Compose file — those stay independently deployable (see the multi-host note below). The root `docker-compose.yml` includes all of them into one project, in dependency order, for local same-host development. User, catalog, and booking each provision their own PostgreSQL database. Java 21 is supplied by each service image, so Java and Maven do not need to be installed on the host. Copy `.env.example` to `.env` and replace the placeholder secrets before deploying.
```bash
cp -n .env.example .env
./start_services.sh
```
`start_services.sh` just creates the shared `movie-booking` Docker network (if missing) and runs `docker compose up -d --build` — one command builds and starts everything: RabbitMQ, MailHog, discovery, user, catalog, booking, notification, and the gateway, in the right order.

- **API Gateway**: `http://localhost:8080`
- **Eureka Dashboard**: `http://localhost:8084`
- **RabbitMQ Dashboard**: `http://localhost:15672` (use `RABBITMQ_USERNAME`/`RABBITMQ_PASSWORD` from `.env`)
- **MailHog Inbox**: `http://localhost:8025` (Check this to see live emails!)

For separate-host production deployments, each service's own `<service>/docker-compose.yml` can still be run independently (e.g. `docker compose -f booking-service/docker-compose.yml up -d --build` on its own host) — RabbitMQ and MailHog likewise deploy from their own directories. Point services at each other over the network instead of Docker's local DNS: set `EUREKA_URL`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `MAIL_HOST`, and `MAIL_PORT` to reachable deployed addresses. Set `EUREKA_INSTANCE_PREFER_IP_ADDRESS=false` plus each service's `*_EUREKA_INSTANCE_HOSTNAME` when Eureka should advertise hostnames instead of container IPs.

## 🧪 Testing & Stress Scenarios

We have prepared automated testing scripts in the `scripts/` directory specifically to fulfill the assignment requirements.

### Quick Setup for Tests
```bash
cd scripts
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt  # Or manually: pip install pytest requests locust
```

### 1. Functional Endpoint Tests (Pytest)
Aggressively test registration, login, and dummy booking endpoints.
```bash
pytest test_endpoints.py -s
```

### 2. High Concurrency Race-Condition Test
Proves that the system safely rejects double-booking attempts via proper locking.
Spawns 20 identical requests for the exact same seat at the exact same millisecond.
*Expected Result*: 1 request gets `200 OK`, 19 requests get `409 Conflict`.
```bash
python test_concurrency.py
```

### 3. Heavy Load Testing (Locust)
Simulates massive traffic representing real user journeys (Register -> Login -> Browse -> Book) to find system breaking points.
```bash
locust -f locustfile.py
```
*Open `http://localhost:8089` to start the test and view real-time performance graphs.*
