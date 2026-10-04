# Fare & Payment Service (Part 4)

Owns: fare estimation, final fare calculation, simulated payment recording, and receipts.

## Run
```
export MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>/ridelink_fare"
export JWT_SECRET="a-long-random-string-at-least-32-characters"   # MUST match Account Service
export RABBITMQ_HOST=localhost
mvn spring-boot:run
```
Runs on port **8084**. Swagger UI: http://localhost:8084/swagger-ui.html

## Endpoints
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | /api/fares/estimate | Bearer (any role) | body: `{ "distanceKm": 8.5 }` -> pre-ride quote |
| GET | /api/payments/{id} | passenger or driver on the payment | payment record |
| GET | /api/payments/ride/{rideId} | passenger or driver on the ride | look up payment by ride |
| GET | /api/payments/{id}/receipt | passenger or driver on the payment | formatted receipt |
| — | (internal) RabbitMQ consumer | — | listens for `ride.completed`, records the final payment |

## Documented fare rule (brief section 5, workflows 3 and 6)
```
fare = max(MINIMUM_FARE, BASE_FARE + PER_KM_RATE * distanceKm)
BASE_FARE = 100 LKR, PER_KM_RATE = 80 LKR/km, MINIMUM_FARE = 150 LKR
```
The same rule (`util/FareCalculator.java`) is used for both the pre-ride estimate and the final fare —
only the distance figure differs (a straight-line estimate vs. the ride's actually recorded distance).

## How a payment gets created
This service never receives a direct "charge this ride" REST call. Instead it consumes the `ride.completed`
event Ride Management publishes to RabbitMQ (`event/RideCompletedListener.java`), so payment processing
happens independently of the driver's "complete ride" request succeeding. The listener is idempotent — a
redelivered event for a ride that already has a payment is ignored rather than double-charging.

## Negative scenarios covered
- **Failed simulated payment**: if the ride's recorded distance is `0` (e.g. identical pickup/destination),
  the payment is saved with `status: FAILED` and no receipt number, instead of charging a bogus amount.
- Invalid estimate request (`distanceKm <= 0`) -> 400 Bad Request
- Payment not found -> 404 Not Found
- A user who isn't a participant in the payment requests it -> 403 Forbidden

## Tests
`mvn test` runs `FareServiceTest`: the fare rule's normal case and its minimum-fare floor, not-found and
access-denied failures, and a successful lookup by the owning passenger.
