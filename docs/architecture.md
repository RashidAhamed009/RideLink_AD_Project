# RideLink Architecture Overview

## 1. System Overview
RideLink is an enterprise-grade ride-sharing backend system developed for IT3130 Application Development. The system is designed following the **Decentralized Microservices Architecture Pattern**, enforcing strict domain boundaries, dedicated persistence stores per service, and stateless communication.

## 2. Four Core Microservices

| Service Name | Port | Database Name | Team Member Ownership | Primary Domain |
|---|---|---|---|---|
| **account-service** | `8081` | `ridelink_account_db` | Member 1 | User registration, authentication, JWT issuance, profile & account status management |
| **driver-vehicle-service** | `8082` | `ridelink_driver_db` | Member 2 | Driver profile, vehicle registration, availability toggle, simulated location & geospatial proximity matching |
| **ride-management-service** | `8083` | `ridelink_ride_db` | Member 3 | Ride request lifecycle, driver matching & assignment, trip states (REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED) |
| **fare-payment-service** | `8084` | `ridelink_payment_db` | Member 4 | Upfront fare estimation, final dynamic fare computation, simulated wallet payments, receipt generation & retrieval |

## 3. Core Architectural Rules
1. **Database-Per-Service**: Each microservice exclusively owns and manages its own MongoDB database. Cross-database queries or shared collections are strictly forbidden.
2. **Stateless Security**: Authentication is managed via JSON Web Tokens (JWT) signed with HMAC-SHA256 (`HS256`). Services validate tokens locally without querying Account Service.
3. **Synchronous REST Communication**: Microservices communicate over HTTP REST using standardized JSON payloads and status codes.
4. **Decoupled Identifiers**: All entities use canonical String UUIDv4 (`java.util.UUID`) identifiers. Cross-service entity references use raw UUID strings.
5. **Universal Error Handling**: Errors follow an RFC 7807 compliant structure across all services.
