# RideLink Microservices Platform - Comprehensive API Test Results Report

> **Execution Date & Time:** 2026-10-03 11:03:24  

> **Target Environment:** Local Spring Boot Microservices connected to MongoDB Atlas  

> **Total Execution Duration:** 43.23s  


## 1. Executive Test Summary

| Metric | Value |
| :--- | :--- |
| **Total Endpoints & Test Scenarios** | **51** |
| **Tests Passed** | <span style='color:green;font-weight:bold;'>51</span> |
| **Tests Failed** | <span style='color:gray;font-weight:bold;'>0</span> |
| **Pass Rate** | **100.0%** |
| **Microservices Covered** | 4 Services (Account, Driver, Ride, Payment) |

---

## 2. Tested Microservice Architecture & Port Mapping

| Service Name | Base URL | Primary Responsibility | Health / OpenAPI |
| :--- | :--- | :--- | :--- |
| **Account Service** | `http://localhost:8081` | User registration, authentication, JWT issuing, user & admin management | `/v3/api-docs` |
| **Driver & Vehicle Service** | `http://localhost:8082` | Driver operational profiles, vehicle registration, location, availability | `/v3/api-docs` |
| **Ride Management Service** | `http://localhost:8083` | Ride booking, lifecycle transitions, dispatch matching, cancellations | `/v3/api-docs` |
| **Fare & Payment Service** | `http://localhost:8084` | Fare estimation, final fare calculations, wallet payments, receipts | `/v3/api-docs` |

---

## 3. Comprehensive Test Results Table

| # | Service | Method | Endpoint URL | Description | Expected | Actual | Latency | Result |
| :-: | :--- | :-: | :--- | :--- | :-: | :-: | :-: | :-: |
| 1 | Account Service | `GET` | `/v3/api-docs` | Verify Account Service OpenAPI Spec | `200` | `200` | 156.01ms | ✅ PASS |
| 2 | Driver Service | `GET` | `/v3/api-docs` | Verify Driver & Vehicle Service OpenAPI Spec | `200` | `200` | 146.56ms | ✅ PASS |
| 3 | Ride Service | `GET` | `/v3/api-docs` | Verify Ride Management Service OpenAPI Spec | `200` | `200` | 104.96ms | ✅ PASS |
| 4 | Payment Service | `GET` | `/v3/api-docs` | Verify Fare & Payment Service OpenAPI Spec | `200` | `200` | 337.82ms | ✅ PASS |
| 5 | Account Service | `POST` | `/api/v1/auth/register/passenger` | Register new passenger account | `201` | `201` | 1546.46ms | ✅ PASS |
| 6 | Account Service | `POST` | `/api/v1/auth/login` | Authenticate passenger & acquire JWT token | `200` | `200` | 373.23ms | ✅ PASS |
| 7 | Account Service | `GET` | `/api/v1/users/me` | Retrieve own user profile (Passenger) | `200` | `200` | 856.22ms | ✅ PASS |
| 8 | Account Service | `PUT` | `/api/v1/users/me` | Update personal profile name (Passenger) | `200` | `200` | 478.72ms | ✅ PASS |
| 9 | Account Service | `POST` | `/api/v1/auth/register/driver` | Register new driver account | `201` | `201` | 402.58ms | ✅ PASS |
| 10 | Account Service | `POST` | `/api/v1/auth/login` | Authenticate driver & acquire JWT token | `200` | `200` | 269.82ms | ✅ PASS |
| 11 | Account Service | `POST` | `/api/v1/auth/login` | Authenticate system administrator & acquire Admin JWT | `200` | `200` | 291.65ms | ✅ PASS |
| 12 | Account Service | `GET` | `/api/v1/admin/users` | Admin queries list of all registered platform users | `200` | `200` | 4224.59ms | ✅ PASS |
| 13 | Account Service | `PATCH` | `/api/v1/admin/users/{userId}/status` | Admin updates user account status to ACTIVE | `200` | `200` | 510.2ms | ✅ PASS |
| 14 | Driver Service | `POST` | `/api/v1/drivers/profile` | Create or update driver operational profile | `200` | `200` | 3567.84ms | ✅ PASS |
| 15 | Driver Service | `POST` | `/api/v1/drivers/vehicles` | Register vehicle for authenticated driver | `201` | `201` | 376.3ms | ✅ PASS |
| 16 | Driver Service | `PUT` | `/api/v1/drivers/vehicles/{vehicleId}` | Update registered vehicle details | `200` | `200` | 375.43ms | ✅ PASS |
| 17 | Driver Service | `GET` | `/api/v1/drivers/me` | Driver views own profile and registered vehicles | `200` | `200` | 780.27ms | ✅ PASS |
| 18 | Driver Service | `GET` | `/api/v1/drivers/{driverId}` | Query driver details and vehicle status by driverId | `200` | `200` | 334.1ms | ✅ PASS |
| 19 | Driver Service | `PATCH` | `/api/v1/drivers/{driverId}/availability` | Update driver availability status via PATCH | `200` | `200` | 562.71ms | ✅ PASS |
| 20 | Driver Service | `PUT` | `/api/v1/drivers/{driverId}/availability` | Update driver availability status via PUT | `200` | `200` | 302.72ms | ✅ PASS |
| 21 | Driver Service | `PATCH` | `/api/v1/drivers/{driverId}/location` | Update driver simulated location coordinates via PATCH | `200` | `200` | 334.94ms | ✅ PASS |
| 22 | Driver Service | `PUT` | `/api/v1/drivers/{driverId}/location` | Update driver simulated location coordinates via PUT | `200` | `200` | 298.9ms | ✅ PASS |
| 23 | Driver Service | `GET` | `/api/v1/drivers/available` | Retrieve available drivers matching location proximity | `200` | `200` | 607.53ms | ✅ PASS |
| 24 | Payment Service | `POST` | `/api/v1/fares/estimate` | Calculate upfront ride fare estimate based on distance & duration | `200` | `200` | 3853.91ms | ✅ PASS |
| 25 | Payment Service | `POST` | `/api/v1/fares/calculate` | Calculate final itemized fare with surge pricing | `200` | `200` | 63.01ms | ✅ PASS |
| 26 | Payment Service | `POST` | `/api/v1/fares/calculate-final` | Calculate final itemized fare via alias endpoint | `200` | `200` | 45.66ms | ✅ PASS |
| 27 | Ride Service | `POST` | `/api/v1/rides` | Passenger creates new ride booking in REQUESTED state | `201` | `201` | 6460.47ms | ✅ PASS |
| 28 | Ride Service | `GET` | `/api/v1/rides/{rideId}` | Retrieve full ride details and status tracking by rideId | `200` | `200` | 327.42ms | ✅ PASS |
| 29 | Ride Service | `GET` | `/api/v1/rides/passenger/{passengerId}` | Get list of rides requested by specific passenger | `200` | `200` | 176.22ms | ✅ PASS |
| 30 | Ride Service | `GET` | `/api/v1/rides/{rideId}/eligible-drivers` | Find nearby eligible drivers for created ride | `200` | `200` | 422.56ms | ✅ PASS |
| 31 | Ride Service | `PATCH` | `/api/v1/rides/{rideId}/assign` | Assign driver to ride (Transitions to ASSIGNED) | `200` | `200` | 370.88ms | ✅ PASS |
| 32 | Ride Service | `PATCH` | `/api/v1/rides/{rideId}/accept` | Driver accepts assigned ride (Transitions to ACCEPTED) | `200` | `200` | 411.16ms | ✅ PASS |
| 33 | Ride Service | `PATCH` | `/api/v1/rides/{rideId}/start` | Driver starts ride trip (Transitions to IN_PROGRESS) | `200` | `200` | 1249.11ms | ✅ PASS |
| 34 | Ride Service | `GET` | `/api/v1/rides/driver/{driverId}` | Get list of rides assigned to specific driver | `200` | `200` | 190.49ms | ✅ PASS |
| 35 | Ride Service | `PATCH` | `/api/v1/rides/{rideId}/complete` | Complete ride, calculate fare, and process settlement (Transitions to COMPLETED) | `200` | `200` | 4242.96ms | ✅ PASS |
| 36 | Ride Service | `POST` | `/api/v1/rides` | Create a second ride specifically to test cancellation workflow | `201` | `201` | 587.41ms | ✅ PASS |
| 37 | Ride Service | `PATCH` | `/api/v1/rides/{rideId}/cancel` | Cancel ride request (Transitions to CANCELLED) | `200` | `200` | 298.15ms | ✅ PASS |
| 38 | Payment Service | `POST` | `/api/v1/payments/process` | Execute simulated wallet payment transaction | `201` | `201` | 434.24ms | ✅ PASS |
| 39 | Payment Service | `GET` | `/api/v1/payments/{paymentId}` | Retrieve payment details using unique payment ID | `200` | `200` | 372.36ms | ✅ PASS |
| 40 | Payment Service | `GET` | `/api/v1/payments/rides/{rideId}` | Retrieve payment transaction status for completed ride | `200` | `200` | 296.8ms | ✅ PASS |
| 41 | Payment Service | `GET` | `/api/v1/payments/receipts/{receiptId}` | Retrieve itemized invoice receipt by unique receipt ID | `200` | `200` | 181.93ms | ✅ PASS |
| 42 | Payment Service | `GET` | `/api/v1/payments/receipts/rides/{rideId}` | Retrieve itemized invoice receipt for completed ride | `200` | `200` | 157.79ms | ✅ PASS |
| 43 | Account Service | `POST` | `/api/v1/auth/register/passenger` | [Negative] Duplicate email registration rejection | `409` | `409` | 2330.85ms | ✅ PASS |
| 44 | Account Service | `POST` | `/api/v1/auth/login` | [Negative] Authentication failure on invalid password | `401` | `401` | 313.0ms | ✅ PASS |
| 45 | Driver Service | `PATCH` | `/api/v1/drivers/{driverId}/location` | [Negative] Bean validation failure on latitude > 90.0 | `400` | `400` | 1971.23ms | ✅ PASS |
| 46 | Driver Service | `POST` | `/api/v1/drivers/vehicles` | [Negative] Vehicle registration conflict on existing plate | `409` | `409` | 150.56ms | ✅ PASS |
| 47 | Ride Service | `POST` | `/api/v1/rides` | [Negative] Dispatch failure when zero drivers are within proximity | `404` | `404` | 316.1ms | ✅ PASS |
| 48 | Ride Service | `PATCH` | `/api/v1/rides/{rideId}/complete` | [Negative] Disallow completing an already completed ride | `409` | `409` | 172.65ms | ✅ PASS |
| 49 | Payment Service | `POST` | `/api/v1/payments/process` | [Negative] Deterministic payment decline simulation | `422` | `422` | 300.19ms | ✅ PASS |
| 50 | Account Service | `GET` | `/api/v1/admin/users` | [Security] Unauthenticated request to Admin endpoint rejected | `401` | `401` | 54.6ms | ✅ PASS |
| 51 | Account Service | `GET` | `/api/v1/admin/users` | [Security] Non-admin (Passenger) forbidden from Admin endpoint | `403` | `403` | 220.96ms | ✅ PASS |

---

## 4. In-Depth Endpoint Execution Details

### ✅ Test #01: [Account Service] GET /v3/api-docs

- **Description:** Verify Account Service OpenAPI Spec
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `156.01 ms`

**Response Body:**
```json
{
  "info": {
    "title": "RideLink - Account Service API",
    "description": "Member 1 - Authentication, Authorization, User Profile & Status Management API",
    "contact": {
      "name": "RideLink Team - Member 1"
    },
    "version": "1.0.0"
  }
}
```

---

### ✅ Test #02: [Driver Service] GET /v3/api-docs

- **Description:** Verify Driver & Vehicle Service OpenAPI Spec
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `146.56 ms`

**Response Body:**
```json
{
  "info": {
    "title": "RideLink - Driver & Vehicle Service API",
    "description": "Member 2 - Driver operational profiles, vehicles, availability & geospatial proximity API",
    "contact": {
      "name": "RideLink Team - Member 2"
    },
    "version": "1.0.0"
  }
}
```

---

### ✅ Test #03: [Ride Service] GET /v3/api-docs

- **Description:** Verify Ride Management Service OpenAPI Spec
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `104.96 ms`

**Response Body:**
```json
{
  "info": {
    "title": "RideLink - Ride Management Service API",
    "description": "Member 3 - Ride booking lifecycle, state machine & driver assignment API",
    "contact": {
      "name": "RideLink Team - Member 3"
    },
    "version": "1.0.0"
  }
}
```

---

### ✅ Test #04: [Payment Service] GET /v3/api-docs

- **Description:** Verify Fare & Payment Service OpenAPI Spec
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `337.82 ms`

**Response Body:**
```json
{
  "info": {
    "title": "RideLink - Fare & Payment Service API",
    "description": "Member 4 - Fare calculation engine, simulated wallet & receipt generation API",
    "contact": {
      "name": "RideLink Team - Member 4"
    },
    "version": "1.0.0"
  }
}
```

---

### ✅ Test #05: [Account Service] POST /api/v1/auth/register/passenger

- **Description:** Register new passenger account
- **Expected Status:** `201` | **Actual Status:** `201`
- **Response Latency:** `1546.46 ms`

**Request Body:**
```json
{
  "name": "Sarah Connor",
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "password": "SecurePass2026!"
}
```

**Response Body:**
```json
{
  "id": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "name": "Sarah Connor",
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "role": "PASSENGER",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-10-03T05:32:42.717150600Z",
  "updatedAt": "2026-10-03T05:32:42.718150300Z"
}
```

---

### ✅ Test #06: [Account Service] POST /api/v1/auth/login

- **Description:** Authenticate passenger & acquire JWT token
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `373.23 ms`

**Request Body:**
```json
{
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "password": "SecurePass2026!"
}
```

**Response Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJz...",
  "user": {
    "id": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
    "name": "Sarah Connor",
    "email": "sarah.connor.1791005561188_6105@ridelink.com",
    "role": "PASSENGER",
    "accountStatus": "ACTIVE",
    "createdAt": "2026-10-03T05:32:42.717Z",
    "updatedAt": "2026-10-03T05:32:42.718Z"
  }
}
```

---

### ✅ Test #07: [Account Service] GET /api/v1/users/me

- **Description:** Retrieve own user profile (Passenger)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `856.22 ms`

**Response Body:**
```json
{
  "id": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "name": "Sarah Connor",
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "role": "PASSENGER",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-10-03T05:32:42.717Z",
  "updatedAt": "2026-10-03T05:32:42.718Z"
}
```

---

### ✅ Test #08: [Account Service] PUT /api/v1/users/me

- **Description:** Update personal profile name (Passenger)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `478.72 ms`

**Request Body:**
```json
{
  "name": "Sarah Connor Updated"
}
```

**Response Body:**
```json
{
  "id": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "name": "Sarah Connor Updated",
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "role": "PASSENGER",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-10-03T05:32:42.717Z",
  "updatedAt": "2026-10-03T05:32:45.052244300Z"
}
```

---

### ✅ Test #09: [Account Service] POST /api/v1/auth/register/driver

- **Description:** Register new driver account
- **Expected Status:** `201` | **Actual Status:** `201`
- **Response Latency:** `402.58 ms`

**Request Body:**
```json
{
  "name": "Alex Mercer",
  "email": "alex.mercer.1791005561188_6105@ridelink.com",
  "password": "SecurePass2026!"
}
```

**Response Body:**
```json
{
  "id": "c61aec41-1800-4003-9348-876d5421b84a",
  "name": "Alex Mercer",
  "email": "alex.mercer.1791005561188_6105@ridelink.com",
  "role": "DRIVER",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-10-03T05:32:45.458382200Z",
  "updatedAt": "2026-10-03T05:32:45.458382200Z"
}
```

---

### ✅ Test #10: [Account Service] POST /api/v1/auth/login

- **Description:** Authenticate driver & acquire JWT token
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `269.82 ms`

**Request Body:**
```json
{
  "email": "alex.mercer.1791005561188_6105@ridelink.com",
  "password": "SecurePass2026!"
}
```

**Response Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJz...",
  "user": {
    "id": "c61aec41-1800-4003-9348-876d5421b84a",
    "name": "Alex Mercer",
    "email": "alex.mercer.1791005561188_6105@ridelink.com",
    "role": "DRIVER",
    "accountStatus": "ACTIVE",
    "createdAt": "2026-10-03T05:32:45.458Z",
    "updatedAt": "2026-10-03T05:32:45.458Z"
  }
}
```

---

### ✅ Test #11: [Account Service] POST /api/v1/auth/login

- **Description:** Authenticate system administrator & acquire Admin JWT
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `291.65 ms`

**Request Body:**
```json
{
  "email": "admin@ridelink.com",
  "password": "***"
}
```

**Response Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJz..."
}
```

---

### ✅ Test #12: [Account Service] GET /api/v1/admin/users

- **Description:** Admin queries list of all registered platform users
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `4224.59 ms`

**Response Body:**
```json
Retrieved 4 users
```

---

### ✅ Test #13: [Account Service] PATCH /api/v1/admin/users/{userId}/status

- **Description:** Admin updates user account status to ACTIVE
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `510.2 ms`

**Request Body:**
```json
{
  "accountStatus": "ACTIVE"
}
```

**Response Body:**
```json
{
  "id": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "name": "Sarah Connor Updated",
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "role": "PASSENGER",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-10-03T05:32:42.717Z",
  "updatedAt": "2026-10-03T05:32:50.738210500Z"
}
```

---

### ✅ Test #14: [Driver Service] POST /api/v1/drivers/profile

- **Description:** Create or update driver operational profile
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `3567.84 ms`

**Request Body:**
```json
{
  "licenseNumber": "LIC-6105",
  "serviceArea": "COLOMBO_CENTRAL",
  "latitude": 6.9344,
  "longitude": 79.8428,
  "locationName": "Colombo Fort Station"
}
```

**Response Body:**
```json
{
  "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "licenseNumber": "LIC-6105",
  "serviceArea": "COLOMBO_CENTRAL",
  "availabilityStatus": "UNAVAILABLE",
  "currentLocation": {
    "latitude": 6.9344,
    "longitude": 79.8428,
    "address": "Simulated Location"
  },
  "rating": 5.0,
  "createdAt": "2026-10-03T05:32:53.881843200Z",
  "updatedAt": "2026-10-03T05:32:53.881843200Z"
}
```

---

### ✅ Test #15: [Driver Service] POST /api/v1/drivers/vehicles

- **Description:** Register vehicle for authenticated driver
- **Expected Status:** `201` | **Actual Status:** `201`
- **Response Latency:** `376.3 ms`

**Request Body:**
```json
{
  "registrationNumber": "CAB-6105",
  "make": "Toyota",
  "model": "Prius Prime",
  "vehicleType": "SEDAN",
  "color": "Pearl White",
  "year": 2023
}
```

**Response Body:**
```json
{
  "id": "fec06c16-4263-4b6a-823b-4f6e512a44e5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "registrationNumber": "CAB-6105",
  "make": "Toyota",
  "model": "Prius Prime",
  "vehicleType": "SEDAN",
  "color": "Pearl White",
  "year": 2023,
  "createdAt": "2026-10-03T05:32:54.646434900Z",
  "updatedAt": "2026-10-03T05:32:54.646434900Z",
  "vehicleId": "fec06c16-4263-4b6a-823b-4f6e512a44e5"
}
```

---

### ✅ Test #16: [Driver Service] PUT /api/v1/drivers/vehicles/{vehicleId}

- **Description:** Update registered vehicle details
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `375.43 ms`

**Request Body:**
```json
{
  "registrationNumber": "CAB-6105",
  "make": "Toyota",
  "model": "Prius Prime 2024",
  "vehicleType": "SEDAN",
  "color": "Metallic Silver",
  "year": 2024
}
```

**Response Body:**
```json
{
  "id": "fec06c16-4263-4b6a-823b-4f6e512a44e5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "registrationNumber": "CAB-6105",
  "make": "Toyota",
  "model": "Prius Prime 2024",
  "vehicleType": "SEDAN",
  "color": "Metallic Silver",
  "year": 2024,
  "createdAt": "2026-10-03T05:32:54.646Z",
  "updatedAt": "2026-10-03T05:32:55.035290800Z",
  "vehicleId": "fec06c16-4263-4b6a-823b-4f6e512a44e5"
}
```

---

### ✅ Test #17: [Driver Service] GET /api/v1/drivers/me

- **Description:** Driver views own profile and registered vehicles
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `780.27 ms`

**Response Body:**
```json
{
  "profile": {
    "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
    "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
    "licenseNumber": "LIC-6105",
    "serviceArea": "COLOMBO_CENTRAL",
    "availabilityStatus": "UNAVAILABLE",
    "currentLocation": {
      "latitude": 6.9344,
      "longitude": 79.8428,
      "address": "Simulated Location"
    },
    "rating": 5.0,
    "createdAt": "2026-10-03T05:32:53.881Z",
    "updatedAt": "2026-10-03T05:32:53.881Z"
  },
  "vehicles": [
    {
      "id": "fec06c16-4263-4b6a-823b-4f6e512a44e5",
      "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
      "registrationNumber": "CAB-6105",
      "make": "Toyota",
      "model": "Prius Prime 2024",
      "vehicleType": "SEDAN",
      "color": "Metallic Silver",
      "year": 2024,
      "createdAt": "2026-10-03T05:32:54.646Z",
      "updatedAt": "2026-10-03T05:32:55.035Z",
      "vehicleId": "fec06c16-4263-4b6a-823b-4f6e512a44e5"
    }
  ]
}
```

---

### ✅ Test #18: [Driver Service] GET /api/v1/drivers/{driverId}

- **Description:** Query driver details and vehicle status by driverId
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `334.1 ms`

**Response Body:**
```json
{
  "profile": {
    "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
    "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
    "licenseNumber": "LIC-6105",
    "serviceArea": "COLOMBO_CENTRAL",
    "availabilityStatus": "UNAVAILABLE",
    "currentLocation": {
      "latitude": 6.9344,
      "longitude": 79.8428,
      "address": "Simulated Location"
    },
    "rating": 5.0,
    "createdAt": "2026-10-03T05:32:53.881Z",
    "updatedAt": "2026-10-03T05:32:53.881Z"
  },
  "vehicles": [
    {
      "id": "fec06c16-4263-4b6a-823b-4f6e512a44e5",
      "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
      "registrationNumber": "CAB-6105",
      "make": "Toyota",
      "model": "Prius Prime 2024",
      "vehicleType": "SEDAN",
      "color": "Metallic Silver",
      "year": 2024,
      "createdAt": "2026-10-03T05:32:54.646Z",
      "updatedAt": "2026-10-03T05:32:55.035Z",
      "vehicleId": "fec06c16-4263-4b6a-823b-4f6e512a44e5"
    }
  ]
}
```

---

### ✅ Test #19: [Driver Service] PATCH /api/v1/drivers/{driverId}/availability

- **Description:** Update driver availability status via PATCH
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `562.71 ms`

**Request Body:**
```json
{
  "status": "AVAILABLE"
}
```

**Response Body:**
```json
{
  "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "licenseNumber": "LIC-6105",
  "serviceArea": "COLOMBO_CENTRAL",
  "availabilityStatus": "AVAILABLE",
  "currentLocation": {
    "latitude": 6.9344,
    "longitude": 79.8428,
    "address": "Simulated Location"
  },
  "rating": 5.0,
  "createdAt": "2026-10-03T05:32:53.881Z",
  "updatedAt": "2026-10-03T05:32:56.724395400Z"
}
```

---

### ✅ Test #20: [Driver Service] PUT /api/v1/drivers/{driverId}/availability

- **Description:** Update driver availability status via PUT
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `302.72 ms`

**Request Body:**
```json
{
  "status": "AVAILABLE"
}
```

**Response Body:**
```json
{
  "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "licenseNumber": "LIC-6105",
  "serviceArea": "COLOMBO_CENTRAL",
  "availabilityStatus": "AVAILABLE",
  "currentLocation": {
    "latitude": 6.9344,
    "longitude": 79.8428,
    "address": "Simulated Location"
  },
  "rating": 5.0,
  "createdAt": "2026-10-03T05:32:53.881Z",
  "updatedAt": "2026-10-03T05:32:57.054412500Z"
}
```

---

### ✅ Test #21: [Driver Service] PATCH /api/v1/drivers/{driverId}/location

- **Description:** Update driver simulated location coordinates via PATCH
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `334.94 ms`

**Request Body:**
```json
{
  "latitude": 6.9344,
  "longitude": 79.8428,
  "address": "Colombo Fort Railway Station"
}
```

**Response Body:**
```json
{
  "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "licenseNumber": "LIC-6105",
  "serviceArea": "COLOMBO_CENTRAL",
  "availabilityStatus": "AVAILABLE",
  "currentLocation": {
    "latitude": 6.9344,
    "longitude": 79.8428,
    "address": "Colombo Fort Railway Station"
  },
  "rating": 5.0,
  "createdAt": "2026-10-03T05:32:53.881Z",
  "updatedAt": "2026-10-03T05:32:57.374022500Z"
}
```

---

### ✅ Test #22: [Driver Service] PUT /api/v1/drivers/{driverId}/location

- **Description:** Update driver simulated location coordinates via PUT
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `298.9 ms`

**Request Body:**
```json
{
  "latitude": 6.9344,
  "longitude": 79.8428,
  "address": "Colombo Fort Railway Station"
}
```

**Response Body:**
```json
{
  "id": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "licenseNumber": "LIC-6105",
  "serviceArea": "COLOMBO_CENTRAL",
  "availabilityStatus": "AVAILABLE",
  "currentLocation": {
    "latitude": 6.9344,
    "longitude": 79.8428,
    "address": "Colombo Fort Railway Station"
  },
  "rating": 5.0,
  "createdAt": "2026-10-03T05:32:53.881Z",
  "updatedAt": "2026-10-03T05:32:57.688517800Z"
}
```

---

### ✅ Test #23: [Driver Service] GET /api/v1/drivers/available

- **Description:** Retrieve available drivers matching location proximity
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `607.53 ms`

**Request Body:**
```json
{
  "latitude": 6.9344,
  "longitude": 79.8428,
  "radiusKm": 10.0,
  "serviceArea": "COLOMBO_CENTRAL"
}
```

**Response Body:**
```json
[
  {
    "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
    "driverProfileId": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
    "licenseNumber": "LIC-6105",
    "serviceArea": "COLOMBO_CENTRAL",
    "currentLocation": {
      "latitude": 6.9344,
      "longitude": 79.8428,
      "address": "Colombo Fort Railway Station"
    },
    "rating": 5.0,
    "distanceKm": 0.0,
    "vehicle": {
      "id": "fec06c16-4263-4b6a-823b-4f6e512a44e5",
      "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
      "registrationNumber": "CAB-6105",
      "make": "Toyota",
      "model": "Prius Prime 2024",
      "vehicleType": "SEDAN",
      "color": "Metallic Silver",
      "year": 2024,
      "createdAt": "2026-10-03T05:32:54.646Z",
      "updatedAt": "2026-10-03T05:32:55.035Z",
      "vehicleId": "fec06c16-4263-4b6a-823b-4f6e512a44e5"
    }
  }
]
```

---

### ✅ Test #24: [Payment Service] POST /api/v1/fares/estimate

- **Description:** Calculate upfront ride fare estimate based on distance & duration
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `3853.91 ms`

**Request Body:**
```json
{
  "rideId": "est-ride-1791005561188",
  "pickup": "Colombo Fort Station",
  "destination": "Bambalapitiya Junction",
  "distanceKm": 6.2,
  "durationMinutes": 18.0
}
```

**Response Body:**
```json
{
  "estimateId": "d3d24d07-7f2c-40d4-909f-6b0f1d5e367a",
  "rideId": "est-ride-1791005561188",
  "pickup": "Colombo Fort Station",
  "destination": "Bambalapitiya Junction",
  "distanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "baseFare": 3.0,
  "distanceCharge": 9.3,
  "timeCharge": 4.5,
  "estimatedTotal": 16.8,
  "currency": "USD",
  "createdAt": "2026-10-03T05:33:00.962834600Z"
}
```

---

### ✅ Test #25: [Payment Service] POST /api/v1/fares/calculate

- **Description:** Calculate final itemized fare with surge pricing
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `63.01 ms`

**Request Body:**
```json
{
  "rideId": "calc-ride-1791005561188",
  "distanceKm": 6.5,
  "durationMinutes": 20.0,
  "surgeMultiplier": 1.2
}
```

**Response Body:**
```json
{
  "rideId": "calc-ride-1791005561188",
  "baseFare": 3.0,
  "distanceKm": 6.5,
  "distanceCharge": 9.75,
  "durationMinutes": 20.0,
  "timeCharge": 5.0,
  "surgeMultiplier": 1.2,
  "totalAmount": 21.3,
  "currency": "USD"
}
```

---

### ✅ Test #26: [Payment Service] POST /api/v1/fares/calculate-final

- **Description:** Calculate final itemized fare via alias endpoint
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `45.66 ms`

**Request Body:**
```json
{
  "rideId": "calc-final-1791005561188",
  "distanceKm": 7.0,
  "durationMinutes": 22.0,
  "surgeMultiplier": 1.0
}
```

**Response Body:**
```json
{
  "rideId": "calc-final-1791005561188",
  "baseFare": 3.0,
  "distanceKm": 7.0,
  "distanceCharge": 10.5,
  "durationMinutes": 22.0,
  "timeCharge": 5.5,
  "surgeMultiplier": 1.0,
  "totalAmount": 19.0,
  "currency": "USD"
}
```

---

### ✅ Test #27: [Ride Service] POST /api/v1/rides

- **Description:** Passenger creates new ride booking in REQUESTED state
- **Expected Status:** `201` | **Actual Status:** `201`
- **Response Latency:** `6460.47 ms`

**Request Body:**
```json
{
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0
}
```

**Response Body:**
```json
{
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": null,
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedFare": 16.8,
  "finalFare": null,
  "status": "REQUESTED",
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:07.236555500Z",
  "updatedAt": "2026-10-03T05:33:07.236555500Z",
  "requestedAt": "2026-10-03T05:33:07.236555500Z",
  "assignedAt": null,
  "acceptedAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

---

### ✅ Test #28: [Ride Service] GET /api/v1/rides/{rideId}

- **Description:** Retrieve full ride details and status tracking by rideId
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `327.42 ms`

**Response Body:**
```json
{
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": null,
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedFare": 16.8,
  "finalFare": null,
  "status": "REQUESTED",
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:07.236Z",
  "updatedAt": "2026-10-03T05:33:07.236Z",
  "requestedAt": "2026-10-03T05:33:07.236Z",
  "assignedAt": null,
  "acceptedAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

---

### ✅ Test #29: [Ride Service] GET /api/v1/rides/passenger/{passengerId}

- **Description:** Get list of rides requested by specific passenger
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `176.22 ms`

**Response Body:**
```json
Found 1 ride(s)
```

---

### ✅ Test #30: [Ride Service] GET /api/v1/rides/{rideId}/eligible-drivers

- **Description:** Find nearby eligible drivers for created ride
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `422.56 ms`

**Response Body:**
```json
[
  {
    "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
    "driverProfileId": "e1710e64-66a7-4f69-ad87-e8bfc22446d5",
    "licenseNumber": "LIC-6105",
    "serviceArea": "COLOMBO_CENTRAL",
    "rating": 5.0,
    "distanceKm": 0.0
  }
]
```

---

### ✅ Test #31: [Ride Service] PATCH /api/v1/rides/{rideId}/assign

- **Description:** Assign driver to ride (Transitions to ASSIGNED)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `370.88 ms`

**Request Body:**
```json
{
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a"
}
```

**Response Body:**
```json
{
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedFare": 16.8,
  "finalFare": null,
  "status": "ASSIGNED",
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:07.236Z",
  "updatedAt": "2026-10-03T05:33:09.987383800Z",
  "requestedAt": "2026-10-03T05:33:07.236Z",
  "assignedAt": "2026-10-03T05:33:09.987383800Z",
  "acceptedAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

---

### ✅ Test #32: [Ride Service] PATCH /api/v1/rides/{rideId}/accept

- **Description:** Driver accepts assigned ride (Transitions to ACCEPTED)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `411.16 ms`

**Response Body:**
```json
{
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedFare": 16.8,
  "finalFare": null,
  "status": "ACCEPTED",
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:07.236Z",
  "updatedAt": "2026-10-03T05:33:10.315986700Z",
  "requestedAt": "2026-10-03T05:33:07.236Z",
  "assignedAt": "2026-10-03T05:33:09.987Z",
  "acceptedAt": "2026-10-03T05:33:10.315986700Z",
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

---

### ✅ Test #33: [Ride Service] PATCH /api/v1/rides/{rideId}/start

- **Description:** Driver starts ride trip (Transitions to IN_PROGRESS)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `1249.11 ms`

**Response Body:**
```json
{
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedFare": 16.8,
  "finalFare": null,
  "status": "IN_PROGRESS",
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:07.236Z",
  "updatedAt": "2026-10-03T05:33:10.847232300Z",
  "requestedAt": "2026-10-03T05:33:07.236Z",
  "assignedAt": "2026-10-03T05:33:09.987Z",
  "acceptedAt": "2026-10-03T05:33:10.315Z",
  "startedAt": "2026-10-03T05:33:10.847232300Z",
  "completedAt": null,
  "cancelledAt": null
}
```

---

### ✅ Test #34: [Ride Service] GET /api/v1/rides/driver/{driverId}

- **Description:** Get list of rides assigned to specific driver
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `190.49 ms`

**Response Body:**
```json
Found 1 ride(s)
```

---

### ✅ Test #35: [Ride Service] PATCH /api/v1/rides/{rideId}/complete

- **Description:** Complete ride, calculate fare, and process settlement (Transitions to COMPLETED)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `4242.96 ms`

**Request Body:**
```json
{
  "actualDistanceKm": 6.5,
  "actualDurationMinutes": 20.0,
  "paymentMethod": "SIMULATED_WALLET",
  "surgeMultiplier": 1.0
}
```

**Response Body:**
```json
{
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Bambalapitiya Junction",
    "latitude": 6.8918,
    "longitude": 79.8587
  },
  "estimatedFare": 16.8,
  "finalFare": 17.75,
  "status": "COMPLETED",
  "estimatedDistanceKm": 6.2,
  "estimatedDurationMinutes": 18.0,
  "actualDistanceKm": 6.5,
  "actualDurationMinutes": 20.0,
  "paymentReference": "TXN-SIM-2201CCE3",
  "receiptId": "6f3d0468-2de7-4c57-8e25-61d258ff8a76",
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:07.236Z",
  "updatedAt": "2026-10-03T05:33:15.832827500Z",
  "requestedAt": "2026-10-03T05:33:07.236Z",
  "assignedAt": "2026-10-03T05:33:09.987Z",
  "acceptedAt": "2026-10-03T05:33:10.315Z",
  "startedAt": "2026-10-03T05:33:10.847Z",
  "completedAt": "2026-10-03T05:33:15.832827500Z",
  "cancelledAt": null
}
```

---

### ✅ Test #36: [Ride Service] POST /api/v1/rides

- **Description:** Create a second ride specifically to test cancellation workflow
- **Expected Status:** `201` | **Actual Status:** `201`
- **Response Latency:** `587.41 ms`

**Request Body:**
```json
{
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Kollupitiya Station",
    "latitude": 6.9034,
    "longitude": 79.8512
  },
  "estimatedDistanceKm": 3.5,
  "estimatedDurationMinutes": 10.0
}
```

**Response Body:**
```json
{
  "rideId": "41bc90c8-44e9-4807-a523-8fd142225877",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": null,
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Kollupitiya Station",
    "latitude": 6.9034,
    "longitude": 79.8512
  },
  "estimatedFare": 10.75,
  "finalFare": null,
  "status": "REQUESTED",
  "estimatedDistanceKm": 3.5,
  "estimatedDurationMinutes": 10.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "createdAt": "2026-10-03T05:33:16.709603900Z",
  "updatedAt": "2026-10-03T05:33:16.709603900Z",
  "requestedAt": "2026-10-03T05:33:16.709603900Z",
  "assignedAt": null,
  "acceptedAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

---

### ✅ Test #37: [Ride Service] PATCH /api/v1/rides/{rideId}/cancel

- **Description:** Cancel ride request (Transitions to CANCELLED)
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `298.15 ms`

**Request Body:**
```json
{
  "reason": "Passenger cancelled: change of meeting plans"
}
```

**Response Body:**
```json
{
  "rideId": "41bc90c8-44e9-4807-a523-8fd142225877",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": null,
  "pickup": {
    "address": "Colombo Fort Station",
    "latitude": 6.9344,
    "longitude": 79.8428
  },
  "destination": {
    "address": "Kollupitiya Station",
    "latitude": 6.9034,
    "longitude": 79.8512
  },
  "estimatedFare": 10.75,
  "finalFare": null,
  "status": "CANCELLED",
  "estimatedDistanceKm": 3.5,
  "estimatedDurationMinutes": 10.0,
  "actualDistanceKm": null,
  "actualDurationMinutes": null,
  "paymentReference": null,
  "receiptId": null,
  "cancellationReason": "Passenger cancelled: change of meeting plans",
  "cancelledBy": "sarah.connor.1791005561188_6105@ridelink.com",
  "createdAt": "2026-10-03T05:33:16.709Z",
  "updatedAt": "2026-10-03T05:33:17.004708200Z",
  "requestedAt": "2026-10-03T05:33:16.709Z",
  "assignedAt": null,
  "acceptedAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": "2026-10-03T05:33:17.004708200Z"
}
```

---

### ✅ Test #38: [Payment Service] POST /api/v1/payments/process

- **Description:** Execute simulated wallet payment transaction
- **Expected Status:** `201` | **Actual Status:** `201`
- **Response Latency:** `434.24 ms`

**Request Body:**
```json
{
  "rideId": "standalone-ride-1791005561188",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "amount": 25.5,
  "paymentMethod": "SIMULATED_WALLET",
  "simulateFailure": false
}
```

**Response Body:**
```json
{
  "paymentId": "17dbe270-6dd1-4e93-9d97-fd86cdaa877a",
  "rideId": "standalone-ride-1791005561188",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "amount": 25.5,
  "paymentMethod": "SIMULATED_WALLET",
  "paymentStatus": "SUCCESS",
  "transactionReference": "TXN-SIM-EE1DF85C",
  "failureReason": null,
  "receiptId": "b3015ae5-9400-4ebd-9456-e4e9356baf0f",
  "createdAt": "2026-10-03T05:33:17.285415600Z"
}
```

---

### ✅ Test #39: [Payment Service] GET /api/v1/payments/{paymentId}

- **Description:** Retrieve payment details using unique payment ID
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `372.36 ms`

**Response Body:**
```json
{
  "paymentId": "17dbe270-6dd1-4e93-9d97-fd86cdaa877a",
  "rideId": "standalone-ride-1791005561188",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "amount": 25.5,
  "paymentMethod": "SIMULATED_WALLET",
  "paymentStatus": "SUCCESS",
  "transactionReference": "TXN-SIM-EE1DF85C",
  "failureReason": null,
  "receiptId": "b3015ae5-9400-4ebd-9456-e4e9356baf0f",
  "createdAt": "2026-10-03T05:33:17.285Z"
}
```

---

### ✅ Test #40: [Payment Service] GET /api/v1/payments/rides/{rideId}

- **Description:** Retrieve payment transaction status for completed ride
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `296.8 ms`

**Response Body:**
```json
{
  "paymentId": "3c02a4a3-e0fb-4dfc-8281-01d653d20673",
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "amount": 17.75,
  "paymentMethod": "SIMULATED_WALLET",
  "paymentStatus": "SUCCESS",
  "transactionReference": "TXN-SIM-2201CCE3",
  "failureReason": null,
  "receiptId": "6f3d0468-2de7-4c57-8e25-61d258ff8a76",
  "createdAt": "2026-10-03T05:33:15.518Z"
}
```

---

### ✅ Test #41: [Payment Service] GET /api/v1/payments/receipts/{receiptId}

- **Description:** Retrieve itemized invoice receipt by unique receipt ID
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `181.93 ms`

**Response Body:**
```json
{
  "receiptId": "6f3d0468-2de7-4c57-8e25-61d258ff8a76",
  "paymentId": "3c02a4a3-e0fb-4dfc-8281-01d653d20673",
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "totalAmount": 17.75,
  "currency": "USD",
  "baseFare": 3.0,
  "distanceCharge": 10.33,
  "timeCharge": 4.43,
  "receiptNumber": "RCPT-9EF699E3",
  "issuedAt": "2026-10-03T05:33:15.665Z"
}
```

---

### ✅ Test #42: [Payment Service] GET /api/v1/payments/receipts/rides/{rideId}

- **Description:** Retrieve itemized invoice receipt for completed ride
- **Expected Status:** `200` | **Actual Status:** `200`
- **Response Latency:** `157.79 ms`

**Response Body:**
```json
{
  "receiptId": "6f3d0468-2de7-4c57-8e25-61d258ff8a76",
  "paymentId": "3c02a4a3-e0fb-4dfc-8281-01d653d20673",
  "rideId": "af6aa950-7ca3-457d-b235-502f293178ba",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "totalAmount": 17.75,
  "currency": "USD",
  "baseFare": 3.0,
  "distanceCharge": 10.33,
  "timeCharge": 4.43,
  "receiptNumber": "RCPT-9EF699E3",
  "issuedAt": "2026-10-03T05:33:15.665Z"
}
```

---

### ✅ Test #43: [Account Service] POST /api/v1/auth/register/passenger

- **Description:** [Negative] Duplicate email registration rejection
- **Expected Status:** `409` | **Actual Status:** `409`
- **Response Latency:** `2330.85 ms`

**Request Body:**
```json
{
  "name": "Sarah Connor Duplicate",
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "password": "SecurePass2026!"
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:20.612876900Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "DUPLICATE_EMAIL",
  "message": "Email address is already registered: sarah.connor.1791005561188_6105@ridelink.com",
  "path": "/api/v1/auth/register/passenger",
  "serviceName": "account-service",
  "validationErrors": null
}
```

---

### ✅ Test #44: [Account Service] POST /api/v1/auth/login

- **Description:** [Negative] Authentication failure on invalid password
- **Expected Status:** `401` | **Actual Status:** `401`
- **Response Latency:** `313.0 ms`

**Request Body:**
```json
{
  "email": "sarah.connor.1791005561188_6105@ridelink.com",
  "password": "IncorrectPassword123!"
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:21.225537900Z",
  "status": 401,
  "error": "Unauthorized",
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login",
  "serviceName": "account-service",
  "validationErrors": null
}
```

---

### ✅ Test #45: [Driver Service] PATCH /api/v1/drivers/{driverId}/location

- **Description:** [Negative] Bean validation failure on latitude > 90.0
- **Expected Status:** `400` | **Actual Status:** `400`
- **Response Latency:** `1971.23 ms`

**Request Body:**
```json
{
  "latitude": 195.0,
  "longitude": 79.8428,
  "address": "Invalid Latitude Point"
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:23.188237200Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed on 1 field(s)",
  "path": "/api/v1/drivers/c61aec41-1800-4003-9348-876d5421b84a/location",
  "serviceName": "driver-vehicle-service",
  "validationErrors": [
    {
      "field": "latitude",
      "rejectedValue": 195.0,
      "message": "Latitude must be <= 90.0"
    }
  ]
}
```

---

### ✅ Test #46: [Driver Service] POST /api/v1/drivers/vehicles

- **Description:** [Negative] Vehicle registration conflict on existing plate
- **Expected Status:** `409` | **Actual Status:** `409`
- **Response Latency:** `150.56 ms`

**Request Body:**
```json
{
  "registrationNumber": "CAB-6105",
  "make": "Toyota",
  "model": "Prius",
  "vehicleType": "SEDAN",
  "color": "White",
  "year": 2022
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:23.347086200Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "DUPLICATE_RESOURCE",
  "message": "Vehicle with registration number is already registered: CAB-6105",
  "path": "/api/v1/drivers/vehicles",
  "serviceName": "driver-vehicle-service",
  "validationErrors": null
}
```

---

### ✅ Test #47: [Ride Service] POST /api/v1/rides

- **Description:** [Negative] Dispatch failure when zero drivers are within proximity
- **Expected Status:** `404` | **Actual Status:** `404`
- **Response Latency:** `316.1 ms`

**Request Body:**
```json
{
  "pickup": {
    "address": "Deep Remote Jungle Outpost, Wilpattu",
    "latitude": 8.45,
    "longitude": 79.98
  },
  "destination": {
    "address": "Anuradhapura Clock Tower",
    "latitude": 8.3114,
    "longitude": 80.4037
  },
  "estimatedDistanceKm": 45.0,
  "estimatedDurationMinutes": 60.0
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:23.663555900Z",
  "status": 404,
  "error": "Not Found",
  "errorCode": "NO_DRIVERS_AVAILABLE",
  "message": "No eligible drivers are currently available near your pickup location.",
  "path": "/api/v1/rides",
  "serviceName": "ride-management-service",
  "validationErrors": null
}
```

---

### ✅ Test #48: [Ride Service] PATCH /api/v1/rides/{rideId}/complete

- **Description:** [Negative] Disallow completing an already completed ride
- **Expected Status:** `409` | **Actual Status:** `409`
- **Response Latency:** `172.65 ms`

**Request Body:**
```json
{
  "actualDistanceKm": 5.0,
  "actualDurationMinutes": 15.0
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:23.836618800Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "INVALID_STATE_TRANSITION",
  "message": "Invalid ride status transition from 'COMPLETED' to 'COMPLETED'.",
  "path": "/api/v1/rides/af6aa950-7ca3-457d-b235-502f293178ba/complete",
  "serviceName": "ride-management-service",
  "validationErrors": null
}
```

---

### ✅ Test #49: [Payment Service] POST /api/v1/payments/process

- **Description:** [Negative] Deterministic payment decline simulation
- **Expected Status:** `422` | **Actual Status:** `422`
- **Response Latency:** `300.19 ms`

**Request Body:**
```json
{
  "rideId": "declined-ride-1791005561188",
  "passengerId": "110e6a51-9c2c-42a8-af5d-32c7e470ec08",
  "driverId": "c61aec41-1800-4003-9348-876d5421b84a",
  "amount": 100.0,
  "paymentMethod": "CREDIT_CARD",
  "simulateFailure": true,
  "failureReason": "INSUFFICIENT_FUNDS"
}
```

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:24.136125600Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "errorCode": "PAYMENT_DECLINED",
  "message": "Simulated payment transaction failed. Reason: INSUFFICIENT_FUNDS",
  "path": "/api/v1/payments/process",
  "serviceName": "fare-payment-service",
  "validationErrors": null
}
```

---

### ✅ Test #50: [Account Service] GET /api/v1/admin/users

- **Description:** [Security] Unauthenticated request to Admin endpoint rejected
- **Expected Status:** `401` | **Actual Status:** `401`
- **Response Latency:** `54.6 ms`

**Response Body:**
```json
{
  "timestamp": 1791005604.1675804,
  "status": 401,
  "error": "Unauthorized",
  "errorCode": "UNAUTHORIZED",
  "message": "Full authentication is required to access this resource: Full authentication is required to access this resource",
  "path": "/api/v1/admin/users",
  "serviceName": "account-service",
  "validationErrors": null
}
```

---

### ✅ Test #51: [Account Service] GET /api/v1/admin/users

- **Description:** [Security] Non-admin (Passenger) forbidden from Admin endpoint
- **Expected Status:** `403` | **Actual Status:** `403`
- **Response Latency:** `220.96 ms`

**Response Body:**
```json
{
  "timestamp": "2026-10-03T05:33:24.398+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/v1/admin/users"
}
```

---
