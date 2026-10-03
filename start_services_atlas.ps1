<#
.SYNOPSIS
    Starts all four RideLink Microservices with MongoDB Atlas database isolation.
.DESCRIPTION
    Loads database connection URIs from .env and launches each service in its own
    terminal window, passing the isolated MONGODB_URI for that specific service.
#>

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "       STARTING RIDELINK MICROSERVICES (MONGODB ATLAS)           " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Load .env
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    # First pass: load all literal variables
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $varName = $parts[0].Trim()
            $varVal = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($varName, $varVal, [System.EnvironmentVariableTarget]::Process)
        }
    }
    # Second pass: expand any ${VAR_NAME} placeholders
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $varName = $parts[0].Trim()
            $varVal = [System.Environment]::GetEnvironmentVariable($varName)
            if ($varVal -match '\$\{([^}]+)\}') {
                $matches = [regex]::Matches($varVal, '\$\{([^}]+)\}')
                foreach ($m in $matches) {
                    $refName = $m.Groups[1].Value
                    $refVal = [System.Environment]::GetEnvironmentVariable($refName)
                    if ($refVal) {
                        $varVal = $varVal.Replace($m.Value, $refVal)
                    }
                }
                [System.Environment]::SetEnvironmentVariable($varName, $varVal, [System.EnvironmentVariableTarget]::Process)
            }
        }
    }
    Write-Host "[OK] Loaded credentials from .env" -ForegroundColor Green
} else {
    Write-Host "[ERROR] .env file not found! Please create .env from .env.example." -ForegroundColor Red
    exit 1
}

$services = @(
    @{
        Name = "Account Service"
        Dir = "account-service"
        Port = 8081
        Uri = $env:ACCOUNT_MONGODB_URI
        DbName = "ridelink_account_db"
    },
    @{
        Name = "Driver & Vehicle Service"
        Dir = "driver-vehicle-service"
        Port = 8082
        Uri = $env:DRIVER_MONGODB_URI
        DbName = "ridelink_driver_db"
    },
    @{
        Name = "Ride Management Service"
        Dir = "ride-management-service"
        Port = 8083
        Uri = $env:RIDE_MONGODB_URI
        DbName = "ridelink_ride_db"
    },
    @{
        Name = "Fare & Payment Service"
        Dir = "fare-payment-service"
        Port = 8084
        Uri = $env:PAYMENT_MONGODB_URI
        DbName = "ridelink_payment_db"
    }
)

foreach ($s in $services) {
    if (-not $s.Uri) {
        Write-Host "[ERROR] Missing URI for $($s.Name)! Check your .env file." -ForegroundColor Red
        exit 1
    }

    Write-Host "Launching $($s.Name) on port $($s.Port) -> Atlas DB: $($s.DbName)..." -ForegroundColor Yellow
    
    $svcDir = Join-Path $PSScriptRoot $s.Dir
    $jarPath = Join-Path $svcDir "target\$($s.Dir)-1.0.0.jar"
    
    $launchCmd = if (Test-Path $jarPath) {
        "java -jar '$jarPath' --spring.data.mongodb.uri='$($s.Uri)'"
    } else {
        "mvn spring-boot:run -pl $($s.Dir)"
    }

    $command = @"
`$host.UI.RawUI.WindowTitle = 'RideLink - $($s.Name) [Port $($s.Port)]'
`$env:MONGODB_URI = '$($s.Uri)'
Write-Host '=====================================================' -ForegroundColor Cyan
Write-Host ' $($s.Name) (Port $($s.Port))' -ForegroundColor Green
Write-Host ' Target Database: $($s.DbName)' -ForegroundColor Yellow
Write-Host '=====================================================' -ForegroundColor Cyan
cd '$PSScriptRoot'
$launchCmd
"@

    Start-Process powershell -ArgumentList "-NoExit", "-Command", $command
    Start-Sleep -Seconds 1
}

Write-Host "`nAll 4 services are launching in separate windows!" -ForegroundColor Green
Write-Host "Wait ~15-20 seconds for Spring Boot applications to initialize." -ForegroundColor Gray
Write-Host "Endpoints:" -ForegroundColor White
Write-Host "  * Account Service:        http://localhost:8081/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  * Driver/Vehicle Service: http://localhost:8082/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  * Ride Service:           http://localhost:8083/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  * Payment Service:        http://localhost:8084/swagger-ui.html" -ForegroundColor Cyan
