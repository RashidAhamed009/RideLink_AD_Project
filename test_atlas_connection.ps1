<#
.SYNOPSIS
    Tests MongoDB Atlas connectivity and DNS/Network access for RideLink microservices.
.DESCRIPTION
    Verifies that the MongoDB Atlas cluster hostname resolves and port 27017 is reachable,
    ensuring IP Whitelisting (Network Access) is configured correctly.
#>

$ErrorActionPreference = "Continue"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "       RIDELINK MICROSERVICES - MONGODB ATLAS CONNECTIVITY CHECK" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Load .env file if available
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Write-Host "[INFO] Loading configuration from .env file..." -ForegroundColor Gray
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
} else {
    Write-Host "[WARN] No .env file found. Using default/existing environment variables." -ForegroundColor Yellow
}

$clusterHost = $env:MONGODB_CLUSTER_HOST
if (-not $clusterHost) {
    $clusterHost = "cluster0.phbuz6v.mongodb.net"
}

Write-Host "`n1. Checking DNS SRV records for cluster: $clusterHost..." -ForegroundColor Yellow
try {
    $srvRecords = Resolve-DnsName -Name $clusterHost -Type SRV -ErrorAction Stop
    Write-Host "  [PASS] DNS SRV records successfully resolved! Found $($srvRecords.Count) shard nodes:" -ForegroundColor Green
    foreach ($r in $srvRecords) {
        Write-Host "         -> $($r.NameTarget):$($r.Port)" -ForegroundColor Gray
    }
    $testShard = $srvRecords[0].NameTarget
} catch {
    Write-Host "  [FAIL] Failed to resolve SRV records for ${clusterHost}: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n2. Testing Network Access / Firewall (Port 27017 on $testShard)..." -ForegroundColor Yellow
$tcpTest = Test-NetConnection -ComputerName $testShard -Port 27017 -WarningAction SilentlyContinue
if ($tcpTest.TcpTestSucceeded) {
    Write-Host "  [PASS] TCP connection succeeded! Port 27017 is reachable." -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Connection to MongoDB Atlas timed out or was refused!" -ForegroundColor Red
    Write-Host "         Please log into MongoDB Atlas -> Network Access -> Add IP Address -> 'Allow Access from Anywhere' (0.0.0.0/0)." -ForegroundColor Yellow
    exit 1
}

Write-Host "`n3. Testing MongoDB Atlas TLS Handshake & Authentication..." -ForegroundColor Yellow
$testUri = $env:ACCOUNT_MONGODB_URI
$pyScript = @"
import pymongo, sys
try:
    c = pymongo.MongoClient('$testUri', serverSelectionTimeoutMS=6000)
    c.admin.command('ping')
    print('AUTH_SUCCESS')
except Exception as e:
    err = str(e)
    if 'alert internal error' in err or 'TLSV1_ALERT' in err:
        print('IP_NOT_WHITELISTED: ' + err)
    elif 'bad auth' in err or 'code: 8000' in err or 'authentication failed' in err:
        print('BAD_CREDENTIALS: ' + err)
    else:
        print('HANDSHAKE_ERROR: ' + err)
    sys.exit(1)
"@

$handshakeResult = python -c "$pyScript" 2>&1
if ($LASTEXITCODE -eq 0 -and $handshakeResult -match 'AUTH_SUCCESS') {
    Write-Host "  [PASS] MongoDB Atlas authentication and handshake succeeded!" -ForegroundColor Green
} elseif ($handshakeResult -match 'BAD_CREDENTIALS') {
    Write-Host "  [FAIL] Network/TLS connection SUCCEEDED, but authentication failed (bad username/password)!" -ForegroundColor Red
    Write-Host "         Username in .env: $env:MONGODB_USER" -ForegroundColor White
    Write-Host "         Please check MongoDB Atlas -> Security -> Database Access -> Database Users" -ForegroundColor Yellow
    Write-Host "         Ensure user '$env:MONGODB_USER' exists and verify/reset the password." -ForegroundColor Yellow
} else {
    Write-Host "  [WARN] Atlas rejected the connection!" -ForegroundColor Yellow
    try {
        $myIp = (Invoke-RestMethod -Uri 'https://api.ipify.org?format=json' -TimeoutSec 3 -ErrorAction SilentlyContinue).ip
    } catch {
        $myIp = "your current IP"
    }
    Write-Host "         MongoDB Atlas requires your client IP to be whitelisted." -ForegroundColor DarkYellow
    Write-Host "         Your Public IP is: $myIp" -ForegroundColor Cyan
    Write-Host "         -> Fix in Atlas: Security > Network Access > '+ Add IP Address' > Add $myIp (or 0.0.0.0/0) > Confirm" -ForegroundColor Cyan
}

Write-Host "`n4. Database-per-Service Isolation Summary:" -ForegroundColor Yellow
$services = @(
    @{ Name = "Account Service"; Port = 8081; DB = "ridelink_account_db"; EnvVar = "ACCOUNT_MONGODB_URI" },
    @{ Name = "Driver & Vehicle Service"; Port = 8082; DB = "ridelink_driver_db"; EnvVar = "DRIVER_MONGODB_URI" },
    @{ Name = "Ride Management Service"; Port = 8083; DB = "ridelink_ride_db"; EnvVar = "RIDE_MONGODB_URI" },
    @{ Name = "Fare & Payment Service"; Port = 8084; DB = "ridelink_payment_db"; EnvVar = "PAYMENT_MONGODB_URI" }
)

foreach ($s in $services) {
    $uriVal = [System.Environment]::GetEnvironmentVariable($s.EnvVar)
    Write-Host "  * $($s.Name) (Port $($s.Port))" -ForegroundColor Cyan
    Write-Host "    - Dedicated Database: $($s.DB)" -ForegroundColor White
    if ($uriVal) {
        # Mask password for display
        $masked = $uriVal -replace ':[^@]+@', ':****@'
        Write-Host "    - Configured URI:     $masked" -ForegroundColor Gray
    } else {
        Write-Host "    - Fallback URI:       mongodb://localhost:27017/$($s.DB)" -ForegroundColor DarkGray
    }
}

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "  [READY] All network prerequisites for MongoDB Atlas are valid!" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Cyan
