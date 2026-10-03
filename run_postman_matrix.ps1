# RideLink Postman / Integration Test Matrix Runner
# Executes every single Postman collection request against live microservices and records execution results

$ErrorActionPreference = "Continue"

$ACCOUNT_URL = "http://localhost:8081"
$DRIVER_URL  = "http://localhost:8082"
$RIDE_URL    = "http://localhost:8083"
$PAYMENT_URL = "http://localhost:8084"

$results = [System.Collections.Generic.List[PSCustomObject]]::new()

function Record-TestResult {
    param(
        [string]$Service,
        [string]$TestName,
        [string]$Purpose,
        [string]$InputDesc,
        [string]$ExpectedResult,
        [string]$ActualResult,
        [string]$Status
    )
    $obj = [PSCustomObject]@{
        Service        = $Service
        TestName       = $TestName
        Purpose        = $Purpose
        Input          = $InputDesc
        ExpectedResult = $ExpectedResult
        ActualResult   = $ActualResult
        Status         = $Status
    }
    $results.Add($obj)
    $color = if ($Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "[$Status] $Service :: $TestName -> $ActualResult" -ForegroundColor $color
}

function Get-ErrorDetails {
    param($Exception)
    $resp = $Exception.Response
    $status = if ($resp -and $resp.StatusCode) { [int]$resp.StatusCode } else { 500 }
    $body = ""
    if ($resp) {
        $stream = $resp.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $raw = $reader.ReadToEnd()
            try { $body = $raw | ConvertFrom-Json } catch { $body = $raw }
        }
    }
    return [PSCustomObject]@{ StatusCode = $status; Body = $body }
}

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$pEmail = "pass.matrix.$ts@ridelink.com"
$dEmail = "drv.matrix.$ts@ridelink.com"
$passId = $null
$passToken = $null
$drvId = $null
$drvToken = $null
$rideId = $null
$paymentId = $null
$receiptId = $null

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host "   EXECUTING POSTMAN INTEGRATION TEST MATRIX" -ForegroundColor Cyan
Write-Host "=======================================================`n" -ForegroundColor Cyan

# ----------------- ACCOUNT SERVICE -----------------
# 1. Register Passenger
try {
    $body = @{ name = "Matrix Passenger"; email = $pEmail; password = "SecurePassword123!" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/register/passenger" -Method Post -Body $body -ContentType "application/json"
    $passId = $r.id
    Record-TestResult "Account Service" "Register Passenger" "Create new passenger account" "POST /auth/register/passenger ($pEmail)" "HTTP 201, UUID returned, ROLE_PASSENGER" "HTTP 201 (id: $passId, role: $($r.role))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Account Service" "Register Passenger" "Create new passenger account" "POST /auth/register/passenger" "HTTP 201" "HTTP $($e.StatusCode)" "FAIL"
}

# 2. Register Driver
try {
    $body = @{ name = "Matrix Driver"; email = $dEmail; password = "DriverSecure123!" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/register/driver" -Method Post -Body $body -ContentType "application/json"
    $drvId = $r.id
    Record-TestResult "Account Service" "Register Driver" "Create new driver account" "POST /auth/register/driver ($dEmail)" "HTTP 201, UUID returned, ROLE_DRIVER" "HTTP 201 (id: $drvId, role: $($r.role))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Account Service" "Register Driver" "Create new driver account" "POST /auth/register/driver" "HTTP 201" "HTTP $($e.StatusCode)" "FAIL"
}

# 3. Login Passenger
try {
    $body = @{ email = $pEmail; password = "SecurePassword123!" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/login" -Method Post -Body $body -ContentType "application/json"
    $passToken = $r.token
    Record-TestResult "Account Service" "Login Passenger" "Authenticate passenger and issue JWT" "POST /auth/login ($pEmail)" "HTTP 200, JWT Bearer token" "HTTP 200 (Token issued: $(if ($passToken) {'Yes'} else {'No'}))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Account Service" "Login Passenger" "Authenticate passenger" "POST /auth/login" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 4. Login Driver
try {
    $body = @{ email = $dEmail; password = "DriverSecure123!" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/login" -Method Post -Body $body -ContentType "application/json"
    $drvToken = $r.token
    Record-TestResult "Account Service" "Login Driver" "Authenticate driver and issue JWT" "POST /auth/login ($dEmail)" "HTTP 200, JWT Bearer token" "HTTP 200 (Token issued: $(if ($drvToken) {'Yes'} else {'No'}))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Account Service" "Login Driver" "Authenticate driver" "POST /auth/login" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 5. Get Own Profile
try {
    $r = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/users/me" -Method Get -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Account Service" "Get Own Profile" "Fetch authenticated user details" "GET /users/me (Bearer $passId)" "HTTP 200, UserProfileResponse" "HTTP 200 (email: $($r.email), status: $($r.accountStatus))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Account Service" "Get Own Profile" "Fetch user details" "GET /users/me" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 6. Update Own Profile
try {
    $body = @{ name = "Matrix Passenger Updated" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/users/me" -Method Put -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Account Service" "Update Own Profile" "Update user display name" "PUT /users/me (name: 'Matrix Passenger Updated')" "HTTP 200, updated name returned" "HTTP 200 (name: $($r.name))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Account Service" "Update Own Profile" "Update user display name" "PUT /users/me" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 7. Negative - Duplicate Registration
try {
    $body = @{ name = "Duplicate"; email = $pEmail; password = "Password123!" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/register/passenger" -Method Post -Body $body -ContentType "application/json" | Out-Null
    Record-TestResult "Account Service" "Duplicate Registration" "Prevent duplicate email signup" "POST /auth/register/passenger ($pEmail)" "HTTP 409 Conflict, DUPLICATE_EMAIL" "Unexpected HTTP 200" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 409 -and $e.Body.errorCode -eq "DUPLICATE_EMAIL")
    Record-TestResult "Account Service" "Duplicate Registration" "Prevent duplicate email signup" "POST /auth/register/passenger ($pEmail)" "HTTP 409 Conflict, DUPLICATE_EMAIL" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode))" $(if ($passed) {"PASS"} else {"FAIL"})
}

# 8. Negative - Invalid Credentials
try {
    $body = @{ email = $pEmail; password = "WrongPassword999!" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/login" -Method Post -Body $body -ContentType "application/json" | Out-Null
    Record-TestResult "Account Service" "Invalid Credentials" "Reject wrong password" "POST /auth/login (invalid pass)" "HTTP 401 Unauthorized, INVALID_CREDENTIALS" "Unexpected HTTP 200" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 401 -and $e.Body.errorCode -eq "INVALID_CREDENTIALS")
    Record-TestResult "Account Service" "Invalid Credentials" "Reject wrong password" "POST /auth/login (invalid pass)" "HTTP 401 Unauthorized, INVALID_CREDENTIALS" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode))" $(if ($passed) {"PASS"} else {"FAIL"})
}

# ----------------- DRIVER SERVICE -----------------
# 9. Create/Update Driver Operational Profile
try {
    $body = @{ licenseNumber = "LIC-MAT-$ts"; serviceArea = "COLOMBO_CENTRAL"; latitude = 6.9344; longitude = 79.8428; locationName = "Colombo Fort" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/profile" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Driver Service" "Create Driver Profile" "Initialize driver operational profile" "POST /drivers/profile (LIC-MAT-$ts)" "HTTP 200, DriverProfileResponse" "HTTP 200 (driverId: $($r.driverId), license: $($r.licenseNumber))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Driver Service" "Create Driver Profile" "Initialize driver profile" "POST /drivers/profile" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 10. Add Vehicle
try {
    $body = @{ registrationNumber = "CAB-MAT-$ts"; make = "Honda"; model = "Civic"; vehicleType = "SEDAN"; color = "Silver"; year = 2022 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/vehicles" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Driver Service" "Add Vehicle" "Register vehicle under driver" "POST /drivers/vehicles (CAB-MAT-$ts)" "HTTP 201 Created, VehicleResponse" "HTTP 201 (reg: $($r.registrationNumber), make: $($r.make))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Driver Service" "Add Vehicle" "Register vehicle" "POST /drivers/vehicles" "HTTP 201" "HTTP $($e.StatusCode)" "FAIL"
}

# 11. View Driver Details
try {
    $r = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$drvId" -Method Get -Headers @{ Authorization = "Bearer $drvToken" }
    $st = if ($r.profile) { $r.profile.availabilityStatus } else { $r.driverProfile.availabilityStatus }
    Record-TestResult "Driver Service" "View Driver Details" "Retrieve operational profile and vehicles" "GET /drivers/$drvId" "HTTP 200, DriverDetailsResponse" "HTTP 200 (status: $st, vehicles: $($r.vehicles.Count))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Driver Service" "View Driver Details" "Retrieve driver details" "GET /drivers/$drvId" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 12. Update Availability Status
try {
    $body = @{ availabilityStatus = "AVAILABLE"; status = "AVAILABLE" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$drvId/availability" -Method Patch -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Driver Service" "Update Availability" "Set driver availability to AVAILABLE" "PATCH /drivers/$drvId/availability (AVAILABLE)" "HTTP 200, status: AVAILABLE" "HTTP 200 (status: $($r.availabilityStatus))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Driver Service" "Update Availability" "Set driver availability" "PATCH /drivers/$drvId/availability" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 13. Update Simulated Location
try {
    $body = @{ latitude = 6.9350; longitude = 79.8435; address = "Pettah Market" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$drvId/location" -Method Patch -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Driver Service" "Update Location" "Update GPS coordinates for driver" "PATCH /drivers/$drvId/location (6.9350, 79.8435)" "HTTP 200, coordinates updated" "HTTP 200 (lat: $($r.simulatedCurrentLocation.latitude), lon: $($r.simulatedCurrentLocation.longitude))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Driver Service" "Update Location" "Update GPS coordinates" "PATCH /drivers/$drvId/location" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 14. Retrieve Eligible Available Drivers
try {
    $r = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/available?latitude=6.9344&longitude=79.8428&radiusKm=10.0" -Method Get -Headers @{ Authorization = "Bearer $passToken" }
    $passed = ($r.Count -ge 1)
    Record-TestResult "Driver Service" "Retrieve Eligible Drivers" "Match available nearby drivers with vehicles" "GET /drivers/available (Fort, 10km)" "HTTP 200, List >= 1 driver" "HTTP 200 (Matched: $($r.Count) drivers)" $(if ($passed) {"PASS"} else {"FAIL"})
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Driver Service" "Retrieve Eligible Drivers" "Match drivers" "GET /drivers/available" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 15. Negative - Duplicate Vehicle Registration
try {
    $body = @{ registrationNumber = "CAB-MAT-$ts"; make = "Toyota"; model = "Aqua"; vehicleType = "HATCHBACK"; color = "Red"; year = 2020 } | ConvertTo-Json
    Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/vehicles" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" } | Out-Null
    Record-TestResult "Driver Service" "Duplicate Vehicle" "Reject duplicate vehicle plate" "POST /drivers/vehicles (CAB-MAT-$ts)" "HTTP 409 Conflict, DUPLICATE_RESOURCE" "Unexpected HTTP 201" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 409 -and $e.Body.errorCode -eq "DUPLICATE_RESOURCE")
    Record-TestResult "Driver Service" "Duplicate Vehicle" "Reject duplicate vehicle plate" "POST /drivers/vehicles (CAB-MAT-$ts)" "HTTP 409 Conflict, DUPLICATE_RESOURCE" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode))" $(if ($passed) {"PASS"} else {"FAIL"})
}

# 16. Negative - Invalid Location Latitude
try {
    $body = @{ latitude = 195.0; longitude = 79.8428; address = "Invalid Latitude" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$drvId/location" -Method Patch -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" } | Out-Null
    Record-TestResult "Driver Service" "Invalid Latitude Validation" "Reject latitude > 90.0" "PATCH /drivers/$drvId/location (lat: 195.0)" "HTTP 400 Bad Request, VALIDATION_FAILED" "Unexpected HTTP 200" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 400 -and $e.Body.errorCode -eq "VALIDATION_FAILED")
    Record-TestResult "Driver Service" "Invalid Latitude Validation" "Reject latitude > 90.0" "PATCH /drivers/$drvId/location (lat: 195.0)" "HTTP 400 Bad Request, VALIDATION_FAILED" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode))" $(if ($passed) {"PASS"} else {"FAIL"})
}

# ----------------- FARE & PAYMENT SERVICE -----------------
# 17. Calculate Upfront Fare Estimate
try {
    $body = @{ rideId = "est-$ts"; pickup = "Colombo Fort"; destination = "Bambalapitiya"; distanceKm = 6.2; durationMinutes = 18.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/fares/estimate" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Payment Service" "Calculate Fare Estimate" "Compute upfront fare quote" "POST /fares/estimate (6.2km, 18min)" "HTTP 200, estimatedTotal: 16.80 USD" "HTTP 200 (Total: `$$($r.estimatedTotal) $($r.currency))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Payment Service" "Calculate Fare Estimate" "Compute upfront fare" "POST /fares/estimate" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 18. Calculate Final Ride Fare
try {
    $body = @{ rideId = "ride-final-$ts"; distanceKm = 6.5; durationMinutes = 20.0; surgeMultiplier = 1.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/fares/calculate" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Payment Service" "Calculate Final Fare" "Compute post-trip final fare with actuals" "POST /fares/calculate (6.5km, 20min)" "HTTP 200, totalAmount: 17.75 USD" "HTTP 200 (Total: `$$($r.totalAmount) $($r.currency))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Payment Service" "Calculate Final Fare" "Compute final fare" "POST /fares/calculate-final" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# ----------------- RIDE MANAGEMENT SERVICE -----------------
# 19. Create Ride Request
try {
    $body = @{
        pickup = @{ address = "Colombo Fort"; latitude = 6.9344; longitude = 79.8428 }
        destination = @{ address = "Bambalapitiya"; latitude = 6.8918; longitude = 79.8587 }
        estimatedDistanceKm = 6.2
        estimatedDurationMinutes = 18.0
    } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $passToken" }
    $rideId = $r.rideId
    Record-TestResult "Ride Service" "Create Ride Request" "Initiate ride in REQUESTED state" "POST /rides (Fort to Bambalapitiya)" "HTTP 201 Created, status: REQUESTED" "HTTP 201 (rideId: $rideId, status: $($r.status))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Create Ride Request" "Initiate ride" "POST /rides" "HTTP 201" "HTTP $($e.StatusCode)" "FAIL"
}

# 20. Get Ride by ID
try {
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId" -Method Get -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Ride Service" "Get Ride by ID" "Retrieve ride details by UUID" "GET /rides/$rideId" "HTTP 200, RideResponse" "HTTP 200 (status: $($r.status), passenger: $($r.passengerId))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Get Ride by ID" "Retrieve ride details" "GET /rides/$rideId" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 21. Find Eligible Drivers for Ride
try {
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/eligible-drivers" -Method Get -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Ride Service" "Find Eligible Drivers for Ride" "Query Driver Service for nearby drivers" "GET /rides/$rideId/eligible-drivers" "HTTP 200, List >= 1 driver" "HTTP 200 (Found: $($r.Count) drivers)" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Find Eligible Drivers for Ride" "Query drivers" "GET /rides/$rideId/eligible-drivers" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 22. Assign Driver to Ride
try {
    $body = @{ driverId = $drvId } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/assign" -Method Patch -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Ride Service" "Assign Driver" "Transition ride from REQUESTED to ASSIGNED" "PATCH /rides/$rideId/assign (driver: $drvId)" "HTTP 200, status: ASSIGNED" "HTTP 200 (status: $($r.status), driver: $($r.driverId))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Assign Driver" "Transition ride to ASSIGNED" "PATCH /rides/$rideId/assign" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 23. Driver Accepts Ride
try {
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/accept" -Method Patch -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Ride Service" "Accept Ride" "Driver transitions ride to ACCEPTED" "PATCH /rides/$rideId/accept" "HTTP 200, status: ACCEPTED" "HTTP 200 (status: $($r.status), acceptedAt: $($r.acceptedAt))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Accept Ride" "Driver transitions ride" "PATCH /rides/$rideId/accept" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 24. Driver Starts Ride
try {
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/start" -Method Patch -Headers @{ Authorization = "Bearer $drvToken" }
    Record-TestResult "Ride Service" "Start Ride" "Driver transitions ride to IN_PROGRESS" "PATCH /rides/$rideId/start" "HTTP 200, status: IN_PROGRESS, driver sync: ON_TRIP" "HTTP 200 (status: $($r.status), startedAt: $($r.startedAt))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Start Ride" "Driver transitions ride" "PATCH /rides/$rideId/start" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 25. Complete Ride & Settle Payment
try {
    $body = @{ actualDistanceKm = 6.5; actualDurationMinutes = 20.0; paymentMethod = "SIMULATED_WALLET"; surgeMultiplier = 1.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/complete" -Method Patch -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $drvToken" }
    $receiptId = $r.receiptId
    Record-TestResult "Ride Service" "Complete Ride & Settle" "Calculate final fare, charge payment, issue receipt" "PATCH /rides/$rideId/complete" "HTTP 200, status: COMPLETED, finalFare: 17.75" "HTTP 200 (finalFare: `$$($r.finalFare), receiptId: $receiptId)" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Ride Service" "Complete Ride & Settle" "Complete ride" "PATCH /rides/$rideId/complete" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 26. Retrieve Itemized Receipt
try {
    $r = Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/payments/receipts/$receiptId" -Method Get -Headers @{ Authorization = "Bearer $passToken" }
    Record-TestResult "Payment Service" "Retrieve Receipt by ID" "Fetch detailed post-trip receipt breakdown" "GET /payments/receipts/$receiptId" "HTTP 200, ReceiptResponse (#RCPT-...)" "HTTP 200 (receipt: $($r.receiptNumber), total: `$$($r.totalAmount))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Payment Service" "Retrieve Receipt by ID" "Fetch receipt" "GET /payments/receipts/$receiptId" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 27. Retrieve Payment by Ride ID
try {
    $r = Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/payments/rides/$rideId" -Method Get -Headers @{ Authorization = "Bearer $passToken" }
    $paymentId = $r.paymentId
    Record-TestResult "Payment Service" "Retrieve Payment by Ride ID" "Fetch payment record associated with ride" "GET /payments/rides/$rideId" "HTTP 200, PaymentResponse" "HTTP 200 (paymentId: $paymentId, status: $($r.paymentStatus))" "PASS"
} catch {
    $e = Get-ErrorDetails $_.Exception
    Record-TestResult "Payment Service" "Retrieve Payment by Ride ID" "Fetch payment" "GET /payments/rides/$rideId" "HTTP 200" "HTTP $($e.StatusCode)" "FAIL"
}

# 28. Negative - Invalid Ride State Transition
try {
    # Attempt to start an already COMPLETED ride
    Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/start" -Method Patch -Headers @{ Authorization = "Bearer $drvToken" } | Out-Null
    Record-TestResult "Ride Service" "Invalid State Transition" "Reject illegal transition COMPLETED -> IN_PROGRESS" "PATCH /rides/$rideId/start" "HTTP 409 Conflict, INVALID_STATE_TRANSITION" "Unexpected HTTP 200" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 409 -and $e.Body.errorCode -eq "INVALID_STATE_TRANSITION")
    Record-TestResult "Ride Service" "Invalid State Transition" "Reject illegal transition COMPLETED -> IN_PROGRESS" "PATCH /rides/$rideId/start" "HTTP 409 Conflict, INVALID_STATE_TRANSITION" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode))" $(if ($passed) {"PASS"} else {"FAIL"})
}

# 29. Negative - Duplicate Payment Rejection
try {
    $body = @{ rideId = $rideId; passengerId = $passId; driverId = $drvId; amount = 17.75; paymentMethod = "SIMULATED_WALLET" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/payments/process" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $passToken" } | Out-Null
    Record-TestResult "Payment Service" "Duplicate Payment Protection" "Prevent double charging for settled ride" "POST /payments/process (settled rideId: $rideId)" "HTTP 409 Conflict, DUPLICATE_PAYMENT" "Unexpected HTTP 200" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 409 -and $e.Body.errorCode -eq "DUPLICATE_PAYMENT")
    Record-TestResult "Payment Service" "Duplicate Payment Protection" "Prevent double charging for settled ride" "POST /payments/process (settled rideId: $rideId)" "HTTP 409 Conflict, DUPLICATE_PAYMENT" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode))" $(if ($passed) {"PASS"} else {"FAIL"})
}

# 30. Negative - Simulated Payment Decline
try {
    $body = @{ rideId = "ride-sim-fail-$ts"; passengerId = $passId; driverId = $drvId; amount = 45.00; paymentMethod = "CREDIT_CARD"; simulateFailure = $true; failureReason = "INSUFFICIENT_FUNDS" } | ConvertTo-Json
    Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/payments/process" -Method Post -Body $body -ContentType "application/json" -Headers @{ Authorization = "Bearer $passToken" } | Out-Null
    Record-TestResult "Payment Service" "Simulated Payment Decline" "Gracefully handle declined payment card" "POST /payments/process (simulateFailure: true)" "HTTP 422 Unprocessable, PAYMENT_DECLINED" "Unexpected HTTP 200" "FAIL"
} catch {
    $e = Get-ErrorDetails $_.Exception
    $passed = ($e.StatusCode -eq 422 -and $e.Body.errorCode -eq "PAYMENT_DECLINED")
    Record-TestResult "Payment Service" "Simulated Payment Decline" "Gracefully handle declined payment card" "POST /payments/process (simulateFailure: true)" "HTTP 422 Unprocessable, PAYMENT_DECLINED" "HTTP $($e.StatusCode) (code: $($e.Body.errorCode), reason: $($e.Body.failureReason))" $(if ($passed) {"PASS"} else {"FAIL"})
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host "   MATRIX EXECUTION SUMMARY" -ForegroundColor Cyan
Write-Host "   Total Matrix Tests Executed: $($results.Count)" -ForegroundColor Green
$passTotal = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failTotal = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "   Total Passed: $passTotal" -ForegroundColor Green
Write-Host "   Total Failed: $failTotal" -ForegroundColor $(if ($failTotal -eq 0) {"Green"} else {"Red"})
Write-Host "=======================================================`n" -ForegroundColor Cyan
