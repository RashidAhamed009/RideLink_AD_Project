# RideLink Inter-Service Communication Design

## 1. Communication Protocol
RideLink employs **Synchronous REST over HTTP/1.1** with standard JSON request and response payloads.

## 2. Rationale & Alternatives
* **Decision**: Synchronous HTTP REST using Spring Framework 6 `RestClient`.
* **Why this is appropriate**: Matches developer workflows with Swagger UI and Postman; easy to debug, test, and trace during local execution and viva demonstration; eliminates external messaging broker infrastructure.
* **Alternative considered**: Asynchronous messaging via Apache Kafka or RabbitMQ.
* **Why rejected**: Introduces heavy operational complexity (broker clusters, outbox patterns, distributed saga rollback orchestration) and complicates direct Postman testing required for the academic rubric.

## 3. Communication Matrix

| Source Service | Target Service | HTTP Endpoint | HTTP Method | Business Goal |
|---|---|---|---|---|
| **Ride Management (8083)** | **Driver & Vehicle (8082)** | `/api/v1/drivers/available` | `GET` | Discover active, nearby drivers for a ride request |
| **Ride Management (8083)** | **Driver & Vehicle (8082)** | `/api/v1/drivers/{driverId}/availability` | `PATCH` | Lock driver on assignment; release driver on ride completion |
| **Ride Management (8083)** | **Fare & Payment (8084)** | `/api/v1/fares/calculate` + `/api/v1/payments/process` | `POST` | Calculate final dynamic fare, process simulated payment, generate receipt |

## 4. Resilience & Error Handling
1. **Timeouts** (configured in `RestClientConfig`):
   * Connect Timeout: 3000ms
   * Read Timeout: 5000ms
2. **Failure Fallbacks**:
   * If Driver Service is unavailable when matching, Ride Service returns `503 Service Unavailable` with message `"Driver Discovery Temporarily Unavailable"`.
   * If Payment Service fails during completion, Ride Service preserves trip metrics and returns `422 Unprocessable Entity` or `502 Bad Gateway` marking payment as `FAILED_PENDING_RETRY`.
