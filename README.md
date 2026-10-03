# RideLink — Microservices Ride-Sharing Backend System

[![Build & Test](https://github.com/ridelink-team/ridelink-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/ridelink-team/ridelink-backend/actions/workflows/ci.yml)
[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0%2B-green.svg)](https://www.mongodb.com/)

RideLink is a decentralized, microservices-based ride-sharing backend engineered for the **IT3130 Application Development** group assignment. It features four independent Spring Boot services, each owning its dedicated MongoDB database, communicating synchronously over REST, and secured with stateless JSON Web Tokens (JWT).

---

## 1. Team Ownership & Responsibilities

| Service Name | Port | Database Name | Module Owner | Primary Responsibilities |
|---|---|---|---|---|
| **`account-service`** | `8081` | `ridelink_account_db` | Member 1 | Registration (Passenger/Driver), JWT issuance, Profile management, Admin status management |
| **`driver-vehicle-service`** | `8082` | `ridelink_driver_db` | Member 2 | Driver operational profiles, Vehicle records, Availability, Geospatial location matching |
| **`ride-management-service`** | `8083` | `ridelink_ride_db` | Member 3 | Ride booking lifecycle, State machine (REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED), Driver assignment |
| **`fare-payment-service`** | `8084` | `ridelink_payment_db` | Member 4 | Upfront fare estimation, Dynamic completion fare calculation, Simulated wallet settlement, Receipt generation |

---

## 2. Repository Layout

```text
AD_project/
├── .github/
│   └── workflows/
│       └── ci.yml                          # GitHub Actions CI matrix build
├── docs/
│   ├── architecture.md                     # Architecture specification
│   ├── architecture-diagram.md             # Mermaid architecture diagrams
│   ├── sequence-diagram.md                 # End-to-end sequence flows
│   └── communication-design.md             # REST communication matrix
├── postman/
│   ├── RideLink.postman_collection.json    # Complete API test suite
│   └── RideLink.postman_environment.json   # Environment variables
├── pom.xml                                 # Root Maven parent aggregator
├── account-service/                        # Service 1 (Port 8081)
├── driver-vehicle-service/                 # Service 2 (Port 8082)
├── ride-management-service/                # Service 3 (Port 8083)
├── fare-payment-service/                   # Service 4 (Port 8084)
├── README.md
└── .gitignore
```

---

## 3. Technology Stack

* **Language**: Java 25 LTS
* **Framework**: Spring Boot 3.5.16
* **Persistence**: Spring Data MongoDB (Strict Database-per-Service)
* **Security**: Spring Security 6 with Stateless JWT (HMAC-SHA256)
* **Validation**: Jakarta Bean Validation (Hibernate Validator)
* **Documentation**: SpringDoc OpenAPI 3.0 (Swagger UI)
* **Testing**: JUnit 5, Mockito, Spring Boot Test
* **Build Tool**: Apache Maven 3.9+
* **Continuous Integration**: GitHub Actions

---

## 4. Quick Start & Execution

### Prerequisites
* JDK 25+ installed and configured on `PATH`
* Apache Maven 3.9+ installed
* MongoDB instance:
  * **Option A (Local)**: Running on `localhost:27017` (default fallback)
  * **Option B (MongoDB Atlas Cloud)**: Configured via environment variables / `.env` file

### Database Configuration (Database-per-Service Pattern)
Each microservice strictly owns its isolated MongoDB database.

To connect to **MongoDB Atlas**:
1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Fill in your MongoDB Atlas cluster hostname, username, and password in `.env`.
3. Verify Atlas network connectivity and DNS SRV resolution:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\test_atlas_connection.ps1
   ```

### Run All Services with MongoDB Atlas
```powershell
# Launches all 4 microservices in separate windows with their isolated Atlas DB URIs:
powershell -ExecutionPolicy Bypass -File .\start_services_atlas.ps1
```

### Run Each Service Independently (Manual)
In four separate terminal windows (with either local MongoDB or your custom `MONGODB_URI` environment variable):

```powershell
# Terminal 1: Account Service (Port 8081 -> ridelink_account_db)
$env:MONGODB_URI = "mongodb+srv://<USER>:<PASS>@<CLUSTER>/ridelink_account_db?retryWrites=true&w=majority&appName=Cluster0"
mvn spring-boot:run -pl account-service

# Terminal 2: Driver & Vehicle Service (Port 8082 -> ridelink_driver_db)
$env:MONGODB_URI = "mongodb+srv://<USER>:<PASS>@<CLUSTER>/ridelink_driver_db?retryWrites=true&w=majority&appName=Cluster0"
mvn spring-boot:run -pl driver-vehicle-service

# Terminal 3: Ride Management Service (Port 8083 -> ridelink_ride_db)
$env:MONGODB_URI = "mongodb+srv://<USER>:<PASS>@<CLUSTER>/ridelink_ride_db?retryWrites=true&w=majority&appName=Cluster0"
mvn spring-boot:run -pl ride-management-service

# Terminal 4: Fare & Payment Service (Port 8084 -> ridelink_payment_db)
$env:MONGODB_URI = "mongodb+srv://<USER>:<PASS>@<CLUSTER>/ridelink_payment_db?retryWrites=true&w=majority&appName=Cluster0"
mvn spring-boot:run -pl fare-payment-service
```

### Stop Running Services
```powershell
powershell -ExecutionPolicy Bypass -File .\stop_services.ps1
```

### Interactive Swagger UI Endpoints
* **Account Service**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
* **Driver & Vehicle Service**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
* **Ride Management Service**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
* **Fare & Payment Service**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)

---

## 5. Running Automated Tests

```bash
# Run unit tests across all 4 services
mvn test

# Run tests for Account Service only
mvn test -pl account-service
```
