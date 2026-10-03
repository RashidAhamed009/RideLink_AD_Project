"""
RideLink Microservices - Comprehensive All-API Test Suite
Tests every single API endpoint across all 4 microservices:
  - Account Service (Port 8081)
  - Driver & Vehicle Service (Port 8082)
  - Ride Management Service (Port 8083)
  - Fare & Payment Service (Port 8084)

Generates full execution results into Markdown file 'API_TEST_RESULTS.md'.
"""

import sys
import time
import json
import random
import datetime
import requests

# Base URLs
ACCOUNT_BASE = "http://localhost:8081"
DRIVER_BASE  = "http://localhost:8082"
RIDE_BASE    = "http://localhost:8083"
PAYMENT_BASE = "http://localhost:8084"

class TestRecorder:
    def __init__(self):
        self.results = []
        self.passed_count = 0
        self.failed_count = 0
        self.start_time = time.time()

    def record(self, test_num, service, method, url, description, expected_status, actual_status, duration_ms, req_body=None, resp_body=None, error=None):
        passed = (actual_status == expected_status) and (error is None)
        if passed:
            self.passed_count += 1
            status_tag = "PASS"
            color_pre = "\033[92m"
        else:
            self.failed_count += 1
            status_tag = "FAIL"
            color_pre = "\033[91m"
        color_reset = "\033[0m"

        print(f"[{color_pre}{status_tag}{color_reset}] #{test_num:02d} [{service}] {method:6} {url} -> Status: {actual_status} (Expected: {expected_status}) [{duration_ms:.1f}ms] - {description}")

        self.results.append({
            "test_num": test_num,
            "service": service,
            "method": method,
            "url": url,
            "description": description,
            "expected_status": expected_status,
            "actual_status": actual_status,
            "duration_ms": round(duration_ms, 2),
            "passed": passed,
            "req_body": req_body,
            "resp_body": resp_body,
            "error": error
        })

    def generate_markdown(self, filename="API_TEST_RESULTS.md"):
        total_time = round(time.time() - self.start_time, 2)
        total_tests = len(self.results)
        success_rate = (self.passed_count / total_tests * 100) if total_tests > 0 else 0

        now_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        md = []
        md.append("# RideLink Microservices Platform - Comprehensive API Test Results Report\n")
        md.append(f"> **Execution Date & Time:** {now_str}  \n")
        md.append(f"> **Target Environment:** Local Spring Boot Microservices connected to MongoDB Atlas  \n")
        md.append(f"> **Total Execution Duration:** {total_time}s  \n\n")

        md.append("## 1. Executive Test Summary\n")
        md.append("| Metric | Value |")
        md.append("| :--- | :--- |")
        md.append(f"| **Total Endpoints & Test Scenarios** | **{total_tests}** |")
        md.append(f"| **Tests Passed** | <span style='color:green;font-weight:bold;'>{self.passed_count}</span> |")
        md.append(f"| **Tests Failed** | <span style='color:{'red' if self.failed_count > 0 else 'gray'};font-weight:bold;'>{self.failed_count}</span> |")
        md.append(f"| **Pass Rate** | **{success_rate:.1f}%** |")
        md.append(f"| **Microservices Covered** | 4 Services (Account, Driver, Ride, Payment) |")
        md.append("\n---\n")

        md.append("## 2. Tested Microservice Architecture & Port Mapping\n")
        md.append("| Service Name | Base URL | Primary Responsibility | Health / OpenAPI |")
        md.append("| :--- | :--- | :--- | :--- |")
        md.append("| **Account Service** | `http://localhost:8081` | User registration, authentication, JWT issuing, user & admin management | `/v3/api-docs` |")
        md.append("| **Driver & Vehicle Service** | `http://localhost:8082` | Driver operational profiles, vehicle registration, location, availability | `/v3/api-docs` |")
        md.append("| **Ride Management Service** | `http://localhost:8083` | Ride booking, lifecycle transitions, dispatch matching, cancellations | `/v3/api-docs` |")
        md.append("| **Fare & Payment Service** | `http://localhost:8084` | Fare estimation, final fare calculations, wallet payments, receipts | `/v3/api-docs` |")
        md.append("\n---\n")

        md.append("## 3. Comprehensive Test Results Table\n")
        md.append("| # | Service | Method | Endpoint URL | Description | Expected | Actual | Latency | Result |")
        md.append("| :-: | :--- | :-: | :--- | :--- | :-: | :-: | :-: | :-: |")

        for r in self.results:
            status_badge = "✅ PASS" if r["passed"] else "❌ FAIL"
            method_badge = f"`{r['method']}`"
            url_code = f"`{r['url']}`"
            md.append(f"| {r['test_num']} | {r['service']} | {method_badge} | {url_code} | {r['description']} | `{r['expected_status']}` | `{r['actual_status']}` | {r['duration_ms']}ms | {status_badge} |")

        md.append("\n---\n")
        md.append("## 4. In-Depth Endpoint Execution Details\n")

        for r in self.results:
            status_icon = "✅" if r["passed"] else "❌"
            md.append(f"### {status_icon} Test #{r['test_num']:02d}: [{r['service']}] {r['method']} {r['url']}\n")
            md.append(f"- **Description:** {r['description']}")
            md.append(f"- **Expected Status:** `{r['expected_status']}` | **Actual Status:** `{r['actual_status']}`")
            md.append(f"- **Response Latency:** `{r['duration_ms']} ms`")

            if r["req_body"]:
                req_json = json.dumps(r["req_body"], indent=2) if isinstance(r["req_body"], (dict, list)) else str(r["req_body"])
                # Truncate if too long
                if len(req_json) > 1500:
                    req_json = req_json[:1500] + "\n... [truncated]"
                md.append("\n**Request Body:**")
                md.append(f"```json\n{req_json}\n```")

            if r["resp_body"]:
                resp_json = json.dumps(r["resp_body"], indent=2) if isinstance(r["resp_body"], (dict, list)) else str(r["resp_body"])
                if len(resp_json) > 1500:
                    resp_json = resp_json[:1500] + "\n... [truncated]"
                md.append("\n**Response Body:**")
                md.append(f"```json\n{resp_json}\n```")

            if r["error"]:
                md.append(f"\n> ⚠️ **Error / Exception:** `{r['error']}`")

            md.append("\n---\n")

        with open(filename, "w", encoding="utf-8") as f:
            f.write("\n".join(md))

        print(f"\nSuccessfully wrote Markdown test report to: {filename}")


def run_request(method, url, headers=None, json_data=None, params=None):
    start = time.time()
    try:
        r = requests.request(
            method=method,
            url=url,
            headers=headers,
            json=json_data,
            params=params,
            timeout=10
        )
        duration_ms = (time.time() - start) * 1000
        try:
            data = r.json()
        except Exception:
            data = r.text
        return r.status_code, duration_ms, data, None
    except Exception as e:
        duration_ms = (time.time() - start) * 1000
        return 0, duration_ms, None, str(e)


def main():
    print("=" * 80)
    print("      RIDELINK MICROSERVICES - FULL ALL-API TESTING SUITE")
    print("=" * 80)

    recorder = TestRecorder()
    test_id = 0

    ts = int(time.time() * 1000)
    rand_val = random.randint(1000, 9999)

    passenger_email = f"sarah.connor.{ts}_{rand_val}@ridelink.com"
    driver_email    = f"alex.mercer.{ts}_{rand_val}@ridelink.com"
    license_plate   = f"CAB-{rand_val}"
    license_no      = f"LIC-{rand_val}"
    password        = "SecurePass2026!"

    # Session variables
    passenger_id = None
    passenger_token = None
    driver_id = None
    driver_token = None
    admin_token = None
    vehicle_id = None
    ride_id = None
    ride_id_cancel = None
    payment_id = None
    receipt_id = None
    direct_payment_id = None
    direct_receipt_id = None

    # =========================================================================
    # STAGE 0: OpenAPI Docs & Specification Verification
    # =========================================================================
    print("\n--- [STAGE 0] OpenAPI Documentation & Specification Verification ---")

    test_id += 1
    status, dur, body, err = run_request("GET", f"{ACCOUNT_BASE}/v3/api-docs")
    recorder.record(test_id, "Account Service", "GET", "/v3/api-docs", "Verify Account Service OpenAPI Spec", 200, status, dur, None, {"info": body.get("info", {}) if isinstance(body, dict) else body}, err)

    test_id += 1
    status, dur, body, err = run_request("GET", f"{DRIVER_BASE}/v3/api-docs")
    recorder.record(test_id, "Driver Service", "GET", "/v3/api-docs", "Verify Driver & Vehicle Service OpenAPI Spec", 200, status, dur, None, {"info": body.get("info", {}) if isinstance(body, dict) else body}, err)

    test_id += 1
    status, dur, body, err = run_request("GET", f"{RIDE_BASE}/v3/api-docs")
    recorder.record(test_id, "Ride Service", "GET", "/v3/api-docs", "Verify Ride Management Service OpenAPI Spec", 200, status, dur, None, {"info": body.get("info", {}) if isinstance(body, dict) else body}, err)

    test_id += 1
    status, dur, body, err = run_request("GET", f"{PAYMENT_BASE}/v3/api-docs")
    recorder.record(test_id, "Payment Service", "GET", "/v3/api-docs", "Verify Fare & Payment Service OpenAPI Spec", 200, status, dur, None, {"info": body.get("info", {}) if isinstance(body, dict) else body}, err)

    # =========================================================================
    # STAGE 1: Account Service Endpoints
    # =========================================================================
    print("\n--- [STAGE 1] Account Service Endpoints (Port 8081) ---")

    # 1.1 Register Passenger
    test_id += 1
    req = {
        "name": "Sarah Connor",
        "email": passenger_email,
        "password": password
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/register/passenger", json_data=req)
    if isinstance(body, dict) and "id" in body:
        passenger_id = body["id"]
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/register/passenger", "Register new passenger account", 201, status, dur, req, body, err)

    # 1.2 Login Passenger
    test_id += 1
    req = {
        "email": passenger_email,
        "password": password
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/login", json_data=req)
    if isinstance(body, dict) and "token" in body:
        passenger_token = body["token"]
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/login", "Authenticate passenger & acquire JWT token", 200, status, dur, req, {"token": passenger_token[:25] + "..." if passenger_token else None, "user": body.get("user") if isinstance(body, dict) else None}, err)

    pass_headers = {"Authorization": f"Bearer {passenger_token}"}

    # 1.3 Get Own Profile (Passenger)
    test_id += 1
    status, dur, body, err = run_request("GET", f"{ACCOUNT_BASE}/api/v1/users/me", headers=pass_headers)
    recorder.record(test_id, "Account Service", "GET", "/api/v1/users/me", "Retrieve own user profile (Passenger)", 200, status, dur, None, body, err)

    # 1.4 Update Own Profile (Passenger)
    test_id += 1
    req = {"name": "Sarah Connor Updated"}
    status, dur, body, err = run_request("PUT", f"{ACCOUNT_BASE}/api/v1/users/me", headers=pass_headers, json_data=req)
    recorder.record(test_id, "Account Service", "PUT", "/api/v1/users/me", "Update personal profile name (Passenger)", 200, status, dur, req, body, err)

    # 1.5 Register Driver
    test_id += 1
    req = {
        "name": "Alex Mercer",
        "email": driver_email,
        "password": password
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/register/driver", json_data=req)
    if isinstance(body, dict) and "id" in body:
        driver_id = body["id"]
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/register/driver", "Register new driver account", 201, status, dur, req, body, err)

    # 1.6 Login Driver
    test_id += 1
    req = {
        "email": driver_email,
        "password": password
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/login", json_data=req)
    if isinstance(body, dict) and "token" in body:
        driver_token = body["token"]
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/login", "Authenticate driver & acquire JWT token", 200, status, dur, req, {"token": driver_token[:25] + "..." if driver_token else None, "user": body.get("user") if isinstance(body, dict) else None}, err)

    drv_headers = {"Authorization": f"Bearer {driver_token}"}

    # 1.7 Login Admin
    test_id += 1
    req = {
        "email": "admin@ridelink.com",
        "password": "AdminSecure2026!"
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/login", json_data=req)
    if isinstance(body, dict) and "token" in body:
        admin_token = body["token"]
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/login", "Authenticate system administrator & acquire Admin JWT", 200, status, dur, {"email": req["email"], "password": "***"}, {"token": admin_token[:25] + "..." if admin_token else None}, err)

    admin_headers = {"Authorization": f"Bearer {admin_token}"}

    # 1.8 Admin Get All Users
    test_id += 1
    status, dur, body, err = run_request("GET", f"{ACCOUNT_BASE}/api/v1/admin/users", headers=admin_headers)
    recorder.record(test_id, "Account Service", "GET", "/api/v1/admin/users", "Admin queries list of all registered platform users", 200, status, dur, None, f"Retrieved {len(body)} users" if isinstance(body, list) else body, err)

    # 1.9 Admin Update User Status
    test_id += 1
    req = {"accountStatus": "ACTIVE"}
    status, dur, body, err = run_request("PATCH", f"{ACCOUNT_BASE}/api/v1/admin/users/{passenger_id}/status", headers=admin_headers, json_data=req)
    recorder.record(test_id, "Account Service", "PATCH", f"/api/v1/admin/users/{{userId}}/status", "Admin updates user account status to ACTIVE", 200, status, dur, req, body, err)


    # =========================================================================
    # STAGE 2: Driver & Vehicle Service Endpoints
    # =========================================================================
    print("\n--- [STAGE 2] Driver & Vehicle Service Endpoints (Port 8082) ---")

    # 2.1 Create Driver Operational Profile
    test_id += 1
    req = {
        "licenseNumber": license_no,
        "serviceArea": "COLOMBO_CENTRAL",
        "latitude": 6.9344,
        "longitude": 79.8428,
        "locationName": "Colombo Fort Station"
    }
    status, dur, body, err = run_request("POST", f"{DRIVER_BASE}/api/v1/drivers/profile", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "POST", "/api/v1/drivers/profile", "Create or update driver operational profile", 200, status, dur, req, body, err)

    # 2.2 Register Vehicle for Driver
    test_id += 1
    req = {
        "registrationNumber": license_plate,
        "make": "Toyota",
        "model": "Prius Prime",
        "vehicleType": "SEDAN",
        "color": "Pearl White",
        "year": 2023
    }
    status, dur, body, err = run_request("POST", f"{DRIVER_BASE}/api/v1/drivers/vehicles", headers=drv_headers, json_data=req)
    if isinstance(body, dict):
        vehicle_id = body.get("vehicleId") or body.get("id")
    recorder.record(test_id, "Driver Service", "POST", "/api/v1/drivers/vehicles", "Register vehicle for authenticated driver", 201, status, dur, req, body, err)

    # 2.3 Update Vehicle Details
    test_id += 1
    req = {
        "registrationNumber": license_plate,
        "make": "Toyota",
        "model": "Prius Prime 2024",
        "vehicleType": "SEDAN",
        "color": "Metallic Silver",
        "year": 2024
    }
    status, dur, body, err = run_request("PUT", f"{DRIVER_BASE}/api/v1/drivers/vehicles/{vehicle_id}", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "PUT", f"/api/v1/drivers/vehicles/{{vehicleId}}", "Update registered vehicle details", 200, status, dur, req, body, err)

    # 2.4 View Own Driver Details
    test_id += 1
    status, dur, body, err = run_request("GET", f"{DRIVER_BASE}/api/v1/drivers/me", headers=drv_headers)
    recorder.record(test_id, "Driver Service", "GET", "/api/v1/drivers/me", "Driver views own profile and registered vehicles", 200, status, dur, None, body, err)

    # 2.5 View Driver Details by ID
    test_id += 1
    status, dur, body, err = run_request("GET", f"{DRIVER_BASE}/api/v1/drivers/{driver_id}", headers=drv_headers)
    recorder.record(test_id, "Driver Service", "GET", f"/api/v1/drivers/{{driverId}}", "Query driver details and vehicle status by driverId", 200, status, dur, None, body, err)

    # 2.6 Update Availability Status (PATCH)
    test_id += 1
    req = {"status": "AVAILABLE"}
    status, dur, body, err = run_request("PATCH", f"{DRIVER_BASE}/api/v1/drivers/{driver_id}/availability", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "PATCH", f"/api/v1/drivers/{{driverId}}/availability", "Update driver availability status via PATCH", 200, status, dur, req, body, err)

    # 2.7 Update Availability Status (PUT variant)
    test_id += 1
    req = {"status": "AVAILABLE"}
    status, dur, body, err = run_request("PUT", f"{DRIVER_BASE}/api/v1/drivers/{driver_id}/availability", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "PUT", f"/api/v1/drivers/{{driverId}}/availability", "Update driver availability status via PUT", 200, status, dur, req, body, err)

    # 2.8 Update Simulated Location (PATCH)
    test_id += 1
    req = {
        "latitude": 6.9344,
        "longitude": 79.8428,
        "address": "Colombo Fort Railway Station"
    }
    status, dur, body, err = run_request("PATCH", f"{DRIVER_BASE}/api/v1/drivers/{driver_id}/location", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "PATCH", f"/api/v1/drivers/{{driverId}}/location", "Update driver simulated location coordinates via PATCH", 200, status, dur, req, body, err)

    # 2.9 Update Simulated Location (PUT variant)
    test_id += 1
    req = {
        "latitude": 6.9344,
        "longitude": 79.8428,
        "address": "Colombo Fort Railway Station"
    }
    status, dur, body, err = run_request("PUT", f"{DRIVER_BASE}/api/v1/drivers/{driver_id}/location", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "PUT", f"/api/v1/drivers/{{driverId}}/location", "Update driver simulated location coordinates via PUT", 200, status, dur, req, body, err)

    # 2.10 Retrieve Eligible Available Drivers
    test_id += 1
    params = {
        "latitude": 6.9344,
        "longitude": 79.8428,
        "radiusKm": 10.0,
        "serviceArea": "COLOMBO_CENTRAL"
    }
    status, dur, body, err = run_request("GET", f"{DRIVER_BASE}/api/v1/drivers/available", headers=pass_headers, params=params)
    recorder.record(test_id, "Driver Service", "GET", "/api/v1/drivers/available", "Retrieve available drivers matching location proximity", 200, status, dur, params, body, err)


    # =========================================================================
    # STAGE 3: Fare & Payment Service - Fares Engine
    # =========================================================================
    print("\n--- [STAGE 3] Fare Engine Endpoints (Port 8084) ---")

    # 3.1 Calculate Upfront Fare Estimate
    test_id += 1
    req = {
        "rideId": f"est-ride-{ts}",
        "pickup": "Colombo Fort Station",
        "destination": "Bambalapitiya Junction",
        "distanceKm": 6.2,
        "durationMinutes": 18.0
    }
    status, dur, body, err = run_request("POST", f"{PAYMENT_BASE}/api/v1/fares/estimate", headers=pass_headers, json_data=req)
    recorder.record(test_id, "Payment Service", "POST", "/api/v1/fares/estimate", "Calculate upfront ride fare estimate based on distance & duration", 200, status, dur, req, body, err)

    # 3.2 Calculate Final Ride Fare
    test_id += 1
    req = {
        "rideId": f"calc-ride-{ts}",
        "distanceKm": 6.5,
        "durationMinutes": 20.0,
        "surgeMultiplier": 1.2
    }
    status, dur, body, err = run_request("POST", f"{PAYMENT_BASE}/api/v1/fares/calculate", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Payment Service", "POST", "/api/v1/fares/calculate", "Calculate final itemized fare with surge pricing", 200, status, dur, req, body, err)

    # 3.3 Calculate Final Ride Fare (calculate-final endpoint alias)
    test_id += 1
    req = {
        "rideId": f"calc-final-{ts}",
        "distanceKm": 7.0,
        "durationMinutes": 22.0,
        "surgeMultiplier": 1.0
    }
    status, dur, body, err = run_request("POST", f"{PAYMENT_BASE}/api/v1/fares/calculate-final", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Payment Service", "POST", "/api/v1/fares/calculate-final", "Calculate final itemized fare via alias endpoint", 200, status, dur, req, body, err)


    # =========================================================================
    # STAGE 4: Ride Management Service - Complete Lifecycle Flow
    # =========================================================================
    print("\n--- [STAGE 4] Ride Lifecycle & Dispatch Endpoints (Port 8083) ---")

    # 4.1 Create Ride Request
    test_id += 1
    req = {
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
    status, dur, body, err = run_request("POST", f"{RIDE_BASE}/api/v1/rides", headers=pass_headers, json_data=req)
    if isinstance(body, dict) and "rideId" in body:
        ride_id = body["rideId"]
    recorder.record(test_id, "Ride Service", "POST", "/api/v1/rides", "Passenger creates new ride booking in REQUESTED state", 201, status, dur, req, body, err)

    # 4.2 Retrieve Ride by ID
    test_id += 1
    status, dur, body, err = run_request("GET", f"{RIDE_BASE}/api/v1/rides/{ride_id}", headers=pass_headers)
    recorder.record(test_id, "Ride Service", "GET", f"/api/v1/rides/{{rideId}}", "Retrieve full ride details and status tracking by rideId", 200, status, dur, None, body, err)

    # 4.3 Get Rides Requested by Passenger
    test_id += 1
    status, dur, body, err = run_request("GET", f"{RIDE_BASE}/api/v1/rides/passenger/{passenger_id}", headers=pass_headers)
    recorder.record(test_id, "Ride Service", "GET", f"/api/v1/rides/passenger/{{passengerId}}", "Get list of rides requested by specific passenger", 200, status, dur, None, f"Found {len(body)} ride(s)" if isinstance(body, list) else body, err)

    # 4.4 Find Nearby Eligible Drivers for Ride
    test_id += 1
    status, dur, body, err = run_request("GET", f"{RIDE_BASE}/api/v1/rides/{ride_id}/eligible-drivers", headers=pass_headers)
    recorder.record(test_id, "Ride Service", "GET", f"/api/v1/rides/{{rideId}}/eligible-drivers", "Find nearby eligible drivers for created ride", 200, status, dur, None, body, err)

    # 4.5 Assign Driver to Ride
    test_id += 1
    req = {"driverId": driver_id}
    status, dur, body, err = run_request("PATCH", f"{RIDE_BASE}/api/v1/rides/{ride_id}/assign", headers=pass_headers, json_data=req)
    recorder.record(test_id, "Ride Service", "PATCH", f"/api/v1/rides/{{rideId}}/assign", "Assign driver to ride (Transitions to ASSIGNED)", 200, status, dur, req, body, err)

    # 4.6 Driver Accepts Assigned Ride
    test_id += 1
    status, dur, body, err = run_request("PATCH", f"{RIDE_BASE}/api/v1/rides/{ride_id}/accept", headers=drv_headers)
    recorder.record(test_id, "Ride Service", "PATCH", f"/api/v1/rides/{{rideId}}/accept", "Driver accepts assigned ride (Transitions to ACCEPTED)", 200, status, dur, None, body, err)

    # 4.7 Driver Starts Ride
    test_id += 1
    status, dur, body, err = run_request("PATCH", f"{RIDE_BASE}/api/v1/rides/{ride_id}/start", headers=drv_headers)
    recorder.record(test_id, "Ride Service", "PATCH", f"/api/v1/rides/{{rideId}}/start", "Driver starts ride trip (Transitions to IN_PROGRESS)", 200, status, dur, None, body, err)

    # 4.8 Get Rides Assigned to Driver
    test_id += 1
    status, dur, body, err = run_request("GET", f"{RIDE_BASE}/api/v1/rides/driver/{driver_id}", headers=drv_headers)
    recorder.record(test_id, "Ride Service", "GET", f"/api/v1/rides/driver/{{driverId}}", "Get list of rides assigned to specific driver", 200, status, dur, None, f"Found {len(body)} ride(s)" if isinstance(body, list) else body, err)

    # 4.9 Driver Completes Ride and Processes Payment
    test_id += 1
    req = {
        "actualDistanceKm": 6.5,
        "actualDurationMinutes": 20.0,
        "paymentMethod": "SIMULATED_WALLET",
        "surgeMultiplier": 1.0
    }
    status, dur, body, err = run_request("PATCH", f"{RIDE_BASE}/api/v1/rides/{ride_id}/complete", headers=drv_headers, json_data=req)
    if isinstance(body, dict):
        receipt_id = body.get("receiptId")
        payment_id = body.get("paymentReference")
    recorder.record(test_id, "Ride Service", "PATCH", f"/api/v1/rides/{{rideId}}/complete", "Complete ride, calculate fare, and process settlement (Transitions to COMPLETED)", 200, status, dur, req, body, err)


    # =========================================================================
    # STAGE 5: Ride Management Service - Ride Cancellation Flow
    # =========================================================================
    print("\n--- [STAGE 5] Ride Cancellation Flow (Port 8083) ---")

    # 5.1 Create Second Ride for Cancellation
    test_id += 1
    req = {
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
    status, dur, body, err = run_request("POST", f"{RIDE_BASE}/api/v1/rides", headers=pass_headers, json_data=req)
    if isinstance(body, dict) and "rideId" in body:
        ride_id_cancel = body["rideId"]
    recorder.record(test_id, "Ride Service", "POST", "/api/v1/rides", "Create a second ride specifically to test cancellation workflow", 201, status, dur, req, body, err)

    # 5.2 Cancel Ride
    test_id += 1
    req = {"reason": "Passenger cancelled: change of meeting plans"}
    status, dur, body, err = run_request("PATCH", f"{RIDE_BASE}/api/v1/rides/{ride_id_cancel}/cancel", headers=pass_headers, json_data=req)
    recorder.record(test_id, "Ride Service", "PATCH", f"/api/v1/rides/{{rideId}}/cancel", "Cancel ride request (Transitions to CANCELLED)", 200, status, dur, req, body, err)


    # =========================================================================
    # STAGE 6: Fare & Payment Service - Payment Processing & Receipts
    # =========================================================================
    print("\n--- [STAGE 6] Payment Transactions & Itemized Receipts (Port 8084) ---")

    # 6.1 Direct Payment Processing (Simulate Wallet Transaction)
    test_id += 1
    req = {
        "rideId": f"standalone-ride-{ts}",
        "passengerId": passenger_id,
        "driverId": driver_id,
        "amount": 25.50,
        "paymentMethod": "SIMULATED_WALLET",
        "simulateFailure": False
    }
    status, dur, body, err = run_request("POST", f"{PAYMENT_BASE}/api/v1/payments/process", headers=pass_headers, json_data=req)
    if isinstance(body, dict):
        direct_payment_id = body.get("paymentId")
        direct_receipt_id = body.get("receiptId")
    recorder.record(test_id, "Payment Service", "POST", "/api/v1/payments/process", "Execute simulated wallet payment transaction", 201, status, dur, req, body, err)

    # 6.2 Retrieve Payment by Payment ID
    test_id += 1
    target_payment = direct_payment_id or payment_id
    status, dur, body, err = run_request("GET", f"{PAYMENT_BASE}/api/v1/payments/{target_payment}", headers=pass_headers)
    recorder.record(test_id, "Payment Service", "GET", f"/api/v1/payments/{{paymentId}}", "Retrieve payment details using unique payment ID", 200, status, dur, None, body, err)

    # 6.3 Retrieve Payment Status by Ride ID
    test_id += 1
    status, dur, body, err = run_request("GET", f"{PAYMENT_BASE}/api/v1/payments/rides/{ride_id}", headers=pass_headers)
    recorder.record(test_id, "Payment Service", "GET", f"/api/v1/payments/rides/{{rideId}}", "Retrieve payment transaction status for completed ride", 200, status, dur, None, body, err)

    # 6.4 Retrieve Itemized Receipt by Receipt ID
    test_id += 1
    target_receipt = receipt_id or direct_receipt_id
    status, dur, body, err = run_request("GET", f"{PAYMENT_BASE}/api/v1/payments/receipts/{target_receipt}", headers=pass_headers)
    recorder.record(test_id, "Payment Service", "GET", f"/api/v1/payments/receipts/{{receiptId}}", "Retrieve itemized invoice receipt by unique receipt ID", 200, status, dur, None, body, err)

    # 6.5 Retrieve Itemized Receipt by Ride ID
    test_id += 1
    status, dur, body, err = run_request("GET", f"{PAYMENT_BASE}/api/v1/payments/receipts/rides/{ride_id}", headers=pass_headers)
    recorder.record(test_id, "Payment Service", "GET", f"/api/v1/payments/receipts/rides/{{rideId}}", "Retrieve itemized invoice receipt for completed ride", 200, status, dur, None, body, err)


    # =========================================================================
    # STAGE 7: Negative Flows, Validation & Role-Based Security
    # =========================================================================
    print("\n--- [STAGE 7] Negative Scenarios, Edge Cases & RBAC Security ---")

    # 7.1 Duplicate Passenger Registration (409 Conflict)
    test_id += 1
    req = {
        "name": "Sarah Connor Duplicate",
        "email": passenger_email,
        "password": password
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/register/passenger", json_data=req)
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/register/passenger", "[Negative] Duplicate email registration rejection", 409, status, dur, req, body, err)

    # 7.2 Invalid Login Credentials (401 Unauthorized)
    test_id += 1
    req = {
        "email": passenger_email,
        "password": "IncorrectPassword123!"
    }
    status, dur, body, err = run_request("POST", f"{ACCOUNT_BASE}/api/v1/auth/login", json_data=req)
    recorder.record(test_id, "Account Service", "POST", "/api/v1/auth/login", "[Negative] Authentication failure on invalid password", 401, status, dur, req, body, err)

    # 7.3 Invalid Driver Location Coordinates (400 Bad Request)
    test_id += 1
    req = {
        "latitude": 195.0,  # Invalid: > 90.0
        "longitude": 79.8428,
        "address": "Invalid Latitude Point"
    }
    status, dur, body, err = run_request("PATCH", f"{DRIVER_BASE}/api/v1/drivers/{driver_id}/location", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "PATCH", f"/api/v1/drivers/{{driverId}}/location", "[Negative] Bean validation failure on latitude > 90.0", 400, status, dur, req, body, err)

    # 7.4 Duplicate Vehicle Registration Number (409 Conflict)
    test_id += 1
    req = {
        "registrationNumber": license_plate,
        "make": "Toyota",
        "model": "Prius",
        "vehicleType": "SEDAN",
        "color": "White",
        "year": 2022
    }
    status, dur, body, err = run_request("POST", f"{DRIVER_BASE}/api/v1/drivers/vehicles", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Driver Service", "POST", "/api/v1/drivers/vehicles", "[Negative] Vehicle registration conflict on existing plate", 409, status, dur, req, body, err)

    # 7.5 Ride Request with No Eligible Drivers (404 Not Found)
    test_id += 1
    req = {
        "pickup": {
            "address": "Deep Remote Jungle Outpost, Wilpattu",
            "latitude": 8.4500,
            "longitude": 79.9800
        },
        "destination": {
            "address": "Anuradhapura Clock Tower",
            "latitude": 8.3114,
            "longitude": 80.4037
        },
        "estimatedDistanceKm": 45.0,
        "estimatedDurationMinutes": 60.0
    }
    status, dur, body, err = run_request("POST", f"{RIDE_BASE}/api/v1/rides", headers=pass_headers, json_data=req)
    recorder.record(test_id, "Ride Service", "POST", "/api/v1/rides", "[Negative] Dispatch failure when zero drivers are within proximity", 404, status, dur, req, body, err)

    # 7.6 Invalid Ride State Transition (409 Conflict)
    test_id += 1
    req = {
        "actualDistanceKm": 5.0,
        "actualDurationMinutes": 15.0
    }
    # Attempting to complete already COMPLETED ride
    status, dur, body, err = run_request("PATCH", f"{RIDE_BASE}/api/v1/rides/{ride_id}/complete", headers=drv_headers, json_data=req)
    recorder.record(test_id, "Ride Service", "PATCH", f"/api/v1/rides/{{rideId}}/complete", "[Negative] Disallow completing an already completed ride", 409, status, dur, req, body, err)

    # 7.7 Simulated Payment Decline (422 Unprocessable Entity)
    test_id += 1
    req = {
        "rideId": f"declined-ride-{ts}",
        "passengerId": passenger_id,
        "driverId": driver_id,
        "amount": 100.00,
        "paymentMethod": "CREDIT_CARD",
        "simulateFailure": True,
        "failureReason": "INSUFFICIENT_FUNDS"
    }
    status, dur, body, err = run_request("POST", f"{PAYMENT_BASE}/api/v1/payments/process", headers=pass_headers, json_data=req)
    recorder.record(test_id, "Payment Service", "POST", "/api/v1/payments/process", "[Negative] Deterministic payment decline simulation", 422, status, dur, req, body, err)

    # 7.8 RBAC: Access Admin Endpoint with No Token (401 Unauthorized)
    test_id += 1
    status, dur, body, err = run_request("GET", f"{ACCOUNT_BASE}/api/v1/admin/users")
    recorder.record(test_id, "Account Service", "GET", "/api/v1/admin/users", "[Security] Unauthenticated request to Admin endpoint rejected", 401, status, dur, None, body, err)

    # 7.9 RBAC: Access Admin Endpoint with Passenger Token (403 Forbidden)
    test_id += 1
    status, dur, body, err = run_request("GET", f"{ACCOUNT_BASE}/api/v1/admin/users", headers=pass_headers)
    recorder.record(test_id, "Account Service", "GET", "/api/v1/admin/users", "[Security] Non-admin (Passenger) forbidden from Admin endpoint", 403, status, dur, None, body, err)


    # =========================================================================
    # SUMMARY & MARKDOWN GENERATION
    # =========================================================================
    print("\n" + "=" * 80)
    print(f"TEST EXECUTION SUMMARY: {recorder.passed_count} PASSED, {recorder.failed_count} FAILED OUT OF {len(recorder.results)} TESTS")
    print("=" * 80)

    recorder.generate_markdown("API_TEST_RESULTS.md")

    if recorder.failed_count > 0:
        sys.exit(1)
    else:
        sys.exit(0)


if __name__ == "__main__":
    main()
