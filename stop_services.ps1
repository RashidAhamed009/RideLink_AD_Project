<#
.SYNOPSIS
    Stops any running RideLink services listening on ports 8081, 8082, 8083, 8084.
#>

Write-Host "Stopping any running RideLink services..." -ForegroundColor Yellow

$ports = @(8081, 8082, 8083, 8084)

foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conns) {
        $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($procId in $pids) {
            try {
                $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
                if ($proc) {
                    Write-Host "Killing process $($proc.ProcessName) (PID: $procId) listening on port $port..." -ForegroundColor Yellow
                    Stop-Process -Id $procId -Force
                    Write-Host "[OK] Port $port freed." -ForegroundColor Green
                }
            } catch {
                Write-Host "[WARN] Could not kill PID $procId : $_" -ForegroundColor DarkGray
            }
        }
    } else {
        Write-Host "Port $port is free." -ForegroundColor Gray
    }
}

Write-Host "Done!" -ForegroundColor Green
