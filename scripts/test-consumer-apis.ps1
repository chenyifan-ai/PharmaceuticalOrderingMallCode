$base = "http://localhost:8080"
$failed = 0
$passed = 0

function Login($phone, $pwd) {
    $body = '{"phone":"' + $phone + '","password":"' + $pwd + '"}'
    $resp = Invoke-WebRequest -Uri "$base/api/auth/login" -Method POST -ContentType "application/json; charset=utf-8" -Body $body -UseBasicParsing -TimeoutSec 30
    $r = $resp.Content | ConvertFrom-Json
    if ($r.code -ne 200) { throw "login failed: $($r.message)" }
    return $r.data
}

function Api($method, $path, $token, $body = $null, $query = $null) {
    Start-Sleep -Milliseconds 150
    $uri = "$base$path"
    if ($query) {
        $qs = ($query.GetEnumerator() | ForEach-Object { "$($_.Key)=$([uri]::EscapeDataString([string]$_.Value))" }) -join "&"
        $uri += "?$qs"
    }
    try {
        $params = @{
            Uri = $uri
            Method = $method
            TimeoutSec = 30
            UseBasicParsing = $true
        }
        if ($token) { $params.Headers = @{ Authorization = "Bearer $token" } }
        if ($null -ne $body) {
            $params.ContentType = "application/json; charset=utf-8"
            $params.Body = ($body | ConvertTo-Json -Depth 10 -Compress)
        }
        $resp = Invoke-WebRequest @params
        $r = $resp.Content | ConvertFrom-Json
        $ok = ($r.code -eq 200)
        if ($ok) { $script:passed++ } else { $script:failed++ }
        $status = if ($ok) { "OK" } else { "FAIL" }
        Write-Host "[$status] $method $path -> $($r.code) $($r.message)"
        return @{ ok = $ok; data = $r.data }
    } catch {
        $script:failed++
        Write-Host "[ERR] $method $path -> $($_.Exception.Message)" -ForegroundColor Red
        return @{ ok = $false }
    }
}

Write-Host "=== C-end API smoke test ===" -ForegroundColor Cyan

try {
    $login = Login "13800000002" "admin123"
    $tok = $login.token
    Write-Host "Logged in userType=$($login.userType)" -ForegroundColor Green
} catch {
    Write-Host "Cannot login - start backend on port 8080" -ForegroundColor Red
    exit 1
}

Api GET "/api/user/info" $tok
Api GET "/api/c/product/list" $tok @{ page = 1; pageSize = 10 }
Api GET "/api/c/product/detail/1" $tok
Api GET "/api/c/product/categories" $tok
Api GET "/api/c/product/hot-searches" $null
Api GET "/api/c/product/recommend" $null @{ page = 1; pageSize = 5 }
Api GET "/api/cart/list" $tok
Api POST "/api/cart/add" $tok @{ productId = 1; quantity = 1 }
Api GET "/api/address/list" $tok
Api GET "/api/qualification/my" $tok
Api GET "/api/prescription/list" $tok @{ page = 1; pageSize = 10 }
Api GET "/api/c/order/list" $tok @{ page = 1; pageSize = 10 }
Api GET "/api/message/list" $tok @{ page = 1; pageSize = 10 }
Api GET "/api/message/unreadCount" $tok
Api GET "/api/product/tier-price/get/1" $tok
Api GET "/api/medicationUser/list" $tok

Write-Host ""
Write-Host "Passed: $passed  Failed: $failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
exit $(if ($failed -gt 0) { 1 } else { 0 })
