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

### 1. Start Infrastructure (Database, Broker, Email)
We use Docker to run the backing services: PostgreSQL, RabbitMQ, and MailHog.
```bash
docker-compose up -d
```
- **RabbitMQ Dashboard**: `http://localhost:15672` (guest/guest)
- **MailHog Inbox**: `http://localhost:8025` (Check this to see live emails!)

### 2. Run the Microservices
Ensure Java 21+ and Maven are installed. Start them in the following order:
```bash
# 1. Start Discovery Service
./mvnw spring-boot:run -pl discovery-service

# 2. Start User, Catalog, Booking, and Notification Services
./mvnw spring-boot:run -pl user-service
./mvnw spring-boot:run -pl catalog-service
./mvnw spring-boot:run -pl booking-service
./mvnw spring-boot:run -pl notification-service

# 3. Start API Gateway (Must run last so it registers with Eureka)
./mvnw spring-boot:run -pl api-gateway
```

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
