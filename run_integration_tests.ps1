$ErrorActionPreference = "Stop"
$global:passedCount = 0
$global:failedCount = 0

function Assert-Result($condition, $message) {
    if ($condition) {
        Write-Host "  [PASS] $message" -ForegroundColor Green
        $global:passedCount++
    } else {
        Write-Host "  [FAIL] $message" -ForegroundColor Red
        $global:failedCount++
        throw "Assertion failed: $message"
    }
}

function Get-HttpError($ex) {
    $code = 0
    $body = $null
    if ($ex.Response) {
        $code = [int]$ex.Response.StatusCode
        $stream = $ex.Response.GetResponseStream()
        if ($stream) {
            $reader = New-Object System.IO.StreamReader($stream)
            $text = $reader.ReadToEnd()
            if ($text) {
                try {
                    $body = $text | ConvertFrom-Json
                } catch {
                    $body = $text
                }
            }
        }
    }
    return @{ StatusCode = $code; Body = $body }
}

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "   RIDELINK MICROSERVICES PLATFORM - END-TO-END INTEGRATION TEST" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# Service Endpoints
$ACCOUNT_URL = "http://localhost:8081"
$DRIVER_URL  = "http://localhost:8082"
$RIDE_URL    = "http://localhost:8083"
$PAYMENT_URL = "http://localhost:8084"

# -----------------------------------------------------------------------------
# 1. HAPPY PATH: Complete Passenger Ride Flow
# -----------------------------------------------------------------------------
Write-Host "`n>>> [FLOW 1] HAPPY PATH: Complete Ride Lifecycle & Settlement" -ForegroundColor Yellow

$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$randNum   = Get-Random -Minimum 1000 -Maximum 9999
$passengerEmail = "passenger.$timestamp@ridelink.com"
$driverEmail    = "driver.$timestamp@ridelink.com"

# 1.1 Register Passenger
Write-Host "1. Register Passenger..."
$regPassBody = @{
    name = "Sarah Connor"
    email = $passengerEmail
    password = "SecurePassword123!"
} | ConvertTo-Json

$passRegResp = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/register/passenger" -Method Post -Body $regPassBody -ContentType "application/json"
Assert-Result ($passRegResp.id -ne $null) "Passenger registered with ID: $($passRegResp.id)"
$passengerId = $passRegResp.id

# 1.2 Login Passenger
Write-Host "2. Login Passenger & Receive JWT..."
$loginPassBody = @{
    email = $passengerEmail
    password = "SecurePassword123!"
} | ConvertTo-Json

$passLoginResp = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/login" -Method Post -Body $loginPassBody -ContentType "application/json"
Assert-Result ($passLoginResp.token -ne $null) "Passenger received valid JWT token"
$passengerToken = $passLoginResp.token

# 1.3 Register & Setup Driver
Write-Host "3. Register & Setup Driver..."
$regDrvBody = @{
    name = "Alex Mercer"
    email = $driverEmail
    password = "DriverSecure123!"
} | ConvertTo-Json

$drvRegResp = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/register/driver" -Method Post -Body $regDrvBody -ContentType "application/json"
$driverId = $drvRegResp.id

$drvLoginResp = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/login" -Method Post -Body $regDrvBody -ContentType "application/json"
$driverToken = $drvLoginResp.token
Assert-Result ($driverToken -ne $null) "Driver registered and received JWT token"

# Setup Driver Profile in Driver Service
$drvProfileBody = @{
    licenseNumber = "LIC-$randNum"
    serviceArea   = "COLOMBO_CENTRAL"
    latitude      = 6.9344
    longitude     = 79.8428
    locationName  = "Colombo Fort Station"
} | ConvertTo-Json

$drvProfileResp = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/profile" -Method Post -Body $drvProfileBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $driverToken" }
Assert-Result ($drvProfileResp.driverId -eq $driverId) "Driver operational profile created in Driver Service"

# Add Vehicle
$vehicleBody = @{
    registrationNumber = "CAB-$randNum"
    make               = "Toyota"
    model              = "Prius Prime"
    vehicleType        = "SEDAN"
    color              = "Pearl White"
    year               = 2023
} | ConvertTo-Json

$vehicleResp = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/vehicles" -Method Post -Body $vehicleBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $driverToken" }
Assert-Result ($vehicleResp.id -ne $null -or $vehicleResp.vehicleId -ne $null) "Vehicle registered for driver: $($vehicleResp.registrationNumber)"

# Set Driver Available
$availBody = @{ availabilityStatus = "AVAILABLE"; status = "AVAILABLE" } | ConvertTo-Json
$availResp = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$driverId/availability" -Method Patch -Body $availBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $driverToken" }
Assert-Result ($availResp.availabilityStatus -eq "AVAILABLE") "Driver availability set to AVAILABLE"

# 1.4 Fare Estimate
Write-Host "4. Create Upfront Fare Estimate via Fare & Payment Service..."
$estimateBody = @{
    rideId          = "est-ride-$timestamp"
    pickup          = "Colombo Fort Station"
    destination     = "Bambalapitiya Junction"
    distanceKm      = 6.2
    durationMinutes = 18.0
} | ConvertTo-Json

$fareEstResp = Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/fares/estimate" -Method Post -Body $estimateBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
$estTotal = $fareEstResp.estimatedTotal
$estCurr = $fareEstResp.currency
Assert-Result ($estTotal -gt 0) "Fare estimated successfully: `$$estTotal $estCurr"

# 1.5 Create Ride Request
Write-Host "5. Create Ride Request via Ride Management Service..."
$createRideBody = @{
    pickup = @{
        address   = "Colombo Fort Station"
        latitude  = 6.9344
        longitude = 79.8428
    }
    destination = @{
        address   = "Bambalapitiya Junction"
        latitude  = 6.8918
        longitude = 79.8587
    }
    estimatedDistanceKm      = 6.2
    estimatedDurationMinutes = 18.0
} | ConvertTo-Json

$rideResp = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides" -Method Post -Body $createRideBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
Assert-Result ($rideResp.status -eq "REQUESTED") "Ride created in REQUESTED state with ID: $($rideResp.rideId)"
$rideId = $rideResp.rideId

# 1.6 Query Eligible Drivers
Write-Host "6. Query Eligible Drivers for Ride..."
$eligibleDrivers = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/eligible-drivers" -Method Get -Headers @{ Authorization = "Bearer $passengerToken" }
Assert-Result ($eligibleDrivers.Count -ge 1) "Driver & Vehicle Service returned $($eligibleDrivers.Count) eligible driver(s)"

# 1.7 Assign Driver
Write-Host "7. Assign Driver to Ride..."
$assignBody = @{ driverId = $driverId } | ConvertTo-Json
$assignResp = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/assign" -Method Patch -Body $assignBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
Assert-Result ($assignResp.status -eq "ASSIGNED" -and $assignResp.driverId -eq $driverId) "Ride status transitioned to ASSIGNED"

# 1.8 Driver Accepts Ride
Write-Host "8. Driver Accepts Ride..."
$acceptResp = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/accept" -Method Patch -Headers @{ Authorization = "Bearer $driverToken" }
Assert-Result ($acceptResp.status -eq "ACCEPTED") "Ride status transitioned to ACCEPTED"

# 1.9 Driver Starts Ride
Write-Host "9. Driver Starts Ride..."
$startResp = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/start" -Method Patch -Headers @{ Authorization = "Bearer $driverToken" }
Assert-Result ($startResp.status -eq "IN_PROGRESS") "Ride status transitioned to IN_PROGRESS"

# Verify driver status in Driver Service transitioned to ON_TRIP
$driverDetails = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$driverId" -Method Get -Headers @{ Authorization = "Bearer $driverToken" }
$currentStatus = if ($driverDetails.profile) { $driverDetails.profile.availabilityStatus } else { $driverDetails.driverProfile.availabilityStatus }
Assert-Result ($currentStatus -eq "ON_TRIP") "Driver availability synchronized to ON_TRIP"

# 1.10 Driver Completes Ride & Processes Settlement
Write-Host "10. Driver Completes Ride & Settle Payment..."
$completeBody = @{
    actualDistanceKm      = 6.5
    actualDurationMinutes = 20.0
    paymentMethod         = "SIMULATED_WALLET"
    surgeMultiplier       = 1.0
} | ConvertTo-Json

$completeResp = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$rideId/complete" -Method Patch -Body $completeBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $driverToken" }
Assert-Result ($completeResp.status -eq "COMPLETED") "Ride status transitioned to COMPLETED"
$fareTotal = $completeResp.finalFare
Assert-Result ($fareTotal -gt 0) "Final fare persisted: `$$fareTotal"
Assert-Result ($completeResp.paymentReference -ne $null) "Payment reference received: $($completeResp.paymentReference)"
Assert-Result ($completeResp.receiptId -ne $null) "Receipt reference received: $($completeResp.receiptId)"
$receiptId = $completeResp.receiptId

# Verify driver availability reset to AVAILABLE
$driverDetailsAfter = Invoke-RestMethod -Uri "$DRIVER_URL/api/v1/drivers/$driverId" -Method Get -Headers @{ Authorization = "Bearer $driverToken" }
$finalStatus = if ($driverDetailsAfter.profile) { $driverDetailsAfter.profile.availabilityStatus } else { $driverDetailsAfter.driverProfile.availabilityStatus }
Assert-Result ($finalStatus -eq "AVAILABLE") "Driver availability reset to AVAILABLE after trip completion"

# 1.11 Retrieve Itemized Receipt
Write-Host "11. Retrieve Itemized Receipt via Fare & Payment Service..."
$receiptResp = Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/payments/receipts/$receiptId" -Method Get -Headers @{ Authorization = "Bearer $passengerToken" }
$rcptTotal = $receiptResp.totalAmount
Assert-Result ($receiptResp.receiptNumber -ne $null) "Receipt retrieved: #$($receiptResp.receiptNumber) Total: `$$rcptTotal"

# -----------------------------------------------------------------------------
# 2. NEGATIVE FLOW 1: No Available Driver
# -----------------------------------------------------------------------------
Write-Host "`n>>> [FLOW 2] NEGATIVE FLOW 1: No Available Driver" -ForegroundColor Yellow
$remoteRideBody = @{
    pickup = @{
        address   = "Remote Jungle Post, Wilpattu"
        latitude  = 8.4500
        longitude = 79.9800
    }
    destination = @{
        address   = "Anuradhapura Clock Tower"
        latitude  = 8.3114
        longitude = 80.4037
    }
    estimatedDistanceKm      = 45.0
    estimatedDurationMinutes = 60.0
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides" -Method Post -Body $remoteRideBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
    Assert-Result $false "Expected NoDriversAvailableException"
} catch {
    $err = Get-HttpError $_.Exception
    Assert-Result ($err.StatusCode -eq 404 -and $err.Body.errorCode -eq "NO_DRIVERS_AVAILABLE") "HTTP 404 NO_DRIVERS_AVAILABLE correctly returned"
}

# -----------------------------------------------------------------------------
# 3. NEGATIVE FLOW 2: Invalid Ride Status Transition
# -----------------------------------------------------------------------------
Write-Host "`n>>> [FLOW 3] NEGATIVE FLOW 2: Invalid Ride Status Transition" -ForegroundColor Yellow
# Create a fresh ride in REQUESTED state
$freshRideResp = Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides" -Method Post -Body $createRideBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
$freshRideId = $freshRideResp.rideId

# Attempt to skip directly to complete (REQUESTED -> COMPLETED is illegal)
try {
    $illegalCompBody = @{
        actualDistanceKm      = 5.0
        actualDurationMinutes = 15.0
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$freshRideId/complete" -Method Patch -Body $illegalCompBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $driverToken" }
    Assert-Result $false "Expected IllegalRideStateTransitionException"
} catch {
    $err = Get-HttpError $_.Exception
    Assert-Result ($err.StatusCode -eq 409 -and $err.Body.errorCode -eq "INVALID_STATE_TRANSITION") "HTTP 409 INVALID_STATE_TRANSITION correctly returned"
}

# -----------------------------------------------------------------------------
# 4. NEGATIVE FLOW 3: Unauthorized Operation
# -----------------------------------------------------------------------------
Write-Host "`n>>> [FLOW 4] NEGATIVE FLOW 3: Unauthorized Operation" -ForegroundColor Yellow
# Register another driver (Intruder)
$intruderBody = @{
    name = "Intruder Driver"
    email = "intruder.$timestamp@ridelink.com"
    password = "DriverSecure123!"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/register/driver" -Method Post -Body $intruderBody -ContentType "application/json" | Out-Null
$intruderLogin = Invoke-RestMethod -Uri "$ACCOUNT_URL/api/v1/auth/login" -Method Post -Body $intruderBody -ContentType "application/json"
$intruderToken = $intruderLogin.token

# Assign the fresh ride to the legitimate driver
Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$freshRideId/assign" -Method Patch -Body (@{ driverId = $driverId } | ConvertTo-Json) -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" } | Out-Null

# Intruder driver attempts to accept someone else's assigned ride
try {
    Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides/$freshRideId/accept" -Method Patch -Headers @{ Authorization = "Bearer $intruderToken" }
    Assert-Result $false "Expected UnauthorizedOperationException"
} catch {
    $err = Get-HttpError $_.Exception
    Assert-Result ($err.StatusCode -eq 403 -and $err.Body.errorCode -eq "UNAUTHORIZED_ACCESS") "HTTP 403 UNAUTHORIZED_ACCESS correctly returned"
}

# -----------------------------------------------------------------------------
# 5. NEGATIVE FLOW 4: Invalid Input Validation
# -----------------------------------------------------------------------------
Write-Host "`n>>> [FLOW 5] NEGATIVE FLOW 4: Invalid Input Validation" -ForegroundColor Yellow
$invalidInputBody = @{
    pickup = @{
        address   = "Test Point"
        latitude  = 195.0 # Invalid > 90.0
        longitude = 79.8428
    }
    destination = @{
        address   = "Test Point B"
        latitude  = 6.8918
        longitude = 79.8587
    }
    estimatedDistanceKm      = -10.0 # Invalid <= 0
    estimatedDurationMinutes = 0.0   # Invalid < 1.0
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$RIDE_URL/api/v1/rides" -Method Post -Body $invalidInputBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
    Assert-Result $false "Expected MethodArgumentNotValidException"
} catch {
    $err = Get-HttpError $_.Exception
    Assert-Result ($err.StatusCode -eq 400 -and $err.Body.errorCode -eq "VALIDATION_FAILED") "HTTP 400 VALIDATION_FAILED with field error details correctly returned"
}

# -----------------------------------------------------------------------------
# 6. NEGATIVE FLOW 5: Failed Simulated Payment
# -----------------------------------------------------------------------------
Write-Host "`n>>> [FLOW 6] NEGATIVE FLOW 5: Failed Simulated Payment" -ForegroundColor Yellow
$failPaymentBody = @{
    rideId          = "ride-fail-sim-$timestamp"
    passengerId     = $passengerId
    driverId        = $driverId
    amount          = 50.00
    paymentMethod   = "CREDIT_CARD"
    simulateFailure = $true
    failureReason   = "INSUFFICIENT_FUNDS"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$PAYMENT_URL/api/v1/payments/process" -Method Post -Body $failPaymentBody -ContentType "application/json" -Headers @{ Authorization = "Bearer $passengerToken" }
    Assert-Result $false "Expected PaymentFailedException"
} catch {
    $err = Get-HttpError $_.Exception
    Assert-Result ($err.StatusCode -eq 422 -and $err.Body.errorCode -eq "PAYMENT_DECLINED") "HTTP 422 PAYMENT_DECLINED with INSUFFICIENT_FUNDS reason correctly returned"
}

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "   INTEGRATION EXECUTION SUMMARY: ALL TESTS PASSED!" -ForegroundColor Green
Write-Host "   Total Passed Assertions: $global:passedCount" -ForegroundColor Green
Write-Host "   Total Failed Assertions: $global:failedCount" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Cyan
