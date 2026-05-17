# Pharmacy Mall API smoke tests
param([string]$BaseUrl = $(if ($env:API_BASE_URL) { $env:API_BASE_URL } else { 'http://localhost:8080/api' }))

$ErrorActionPreference = 'Stop'
$script:Passed = 0
$script:Failed = 0
$script:Results = @()

function Write-Result($id, $name, $ok, $detail) {
    $status = if ($ok) { 'PASS' } else { 'FAIL' }
    if ($ok) { $script:Passed++ } else { $script:Failed++ }
    $script:Results += [pscustomobject]@{ Id = $id; Name = $name; Status = $status; Detail = $detail }
    $color = if ($ok) { 'Green' } else { 'Red' }
    Write-Host "[$status] $id $name" -ForegroundColor $color
    if ($detail) { Write-Host "       $detail" -ForegroundColor DarkGray }
}

function Invoke-Api {
    param([string]$Method = 'GET', [string]$Path, [hashtable]$Headers = @{}, $Body = $null)
    $uri = "$BaseUrl$Path"
    $params = @{ Uri = $uri; Method = $Method; Headers = $Headers; UseBasicParsing = $true; TimeoutSec = 15 }
    if ($null -ne $Body) {
        $params.ContentType = 'application/json; charset=utf-8'
        $params.Body = ($Body | ConvertTo-Json -Depth 6 -Compress)
    }
    try {
        $resp = Invoke-WebRequest @params
        return @{ Ok = $true; Json = ($resp.Content | ConvertFrom-Json); Raw = $resp.Content }
    }
    catch {
        return @{ Ok = $false; Error = $_.Exception.Message; Raw = '' }
    }
}

function Login($phone, $password) {
    $r = Invoke-Api -Method POST -Path '/auth/login' -Body @{ phone = $phone; password = $password }
    if (-not $r.Ok -or $r.Json.code -ne 200) { return $null }
    return $r.Json.data.token
}

Write-Host '=== API Smoke Test ===' -ForegroundColor Cyan

$adminToken = Login '13800000000' 'admin123'
Write-Result 'BE-AUTH-01' 'Admin login' ($null -ne $adminToken) ''

$merchantToken = Login '13800000001' 'admin123'
Write-Result 'BE-AUTH-02' 'Merchant login' ($null -ne $merchantToken) ''

$userToken = Login '13800000002' 'admin123'
Write-Result 'BE-AUTH-03' 'Buyer login' ($null -ne $userToken) ''

$r = Invoke-Api -Path '/admin/order/list'
Write-Result 'BE-AUTH-04' 'Unauthorized order list' ((-not $r.Ok) -or ($r.Json.code -ne 200)) ''

$adminH = @{ Authorization = "Bearer $adminToken" }
$merchantH = @{ Authorization = "Bearer $merchantToken" }
$userH = @{ Authorization = "Bearer $userToken" }

$r = Invoke-Api -Path '/admin/order/list?page=1&pageSize=5' -Headers $adminH
Write-Result 'BE-ADMIN-01' 'Admin order list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/admin/dashboard/stats' -Headers $adminH
Write-Result 'BE-ADMIN-02' 'Dashboard stats' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/admin/qualification/list?page=1&pageSize=5' -Headers $adminH
Write-Result 'BE-ADMIN-03' 'Qualification list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/admin/payment/voucher/pending?page=1&pageSize=5' -Headers $adminH
Write-Result 'BE-ADMIN-04' 'Payment voucher pending' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/admin/settlement/list?page=1&pageSize=5' -Headers $adminH
Write-Result 'BE-ADMIN-05' 'Settlement list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/admin/operation-log/list?page=1&pageSize=5' -Headers $adminH
Write-Result 'BE-ADMIN-06' 'Operation log list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/admin/order/list?status=6&page=1&pageSize=5' -Headers $adminH
Write-Result 'BE-ADMIN-07' 'Refunding orders filter' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/merchant/order/list?page=1&pageSize=5' -Headers $merchantH
Write-Result 'BE-MERCHANT-01' 'Merchant order list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/merchant/product/list?page=1&pageSize=5' -Headers $merchantH
Write-Result 'BE-MERCHANT-02' 'Merchant product list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/merchant/stock/summary' -Headers $merchantH
Write-Result 'BE-MERCHANT-03' 'Merchant stock summary' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/c/product/list?page=1&pageSize=5'
Write-Result 'BE-C-01' 'Public product list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/c/home/'
Write-Result 'BE-C-02' 'Public home' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/cart/list' -Headers $userH
Write-Result 'BE-C-03' 'Cart list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/c/order/list?page=1&pageSize=5' -Headers $userH
Write-Result 'BE-C-04' 'C order list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/qualification/my' -Headers $userH
Write-Result 'BE-C-05' 'My qualification' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/c/coupon/my' -Headers $userH
Write-Result 'BE-C-06' 'My coupons' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/c/purchase/stats' -Headers $userH
Write-Result 'BE-C-07' 'Purchase stats' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Path '/prescription/list?page=1&pageSize=5' -Headers $userH
Write-Result 'BE-C-08' 'Prescription list' ($r.Ok -and $r.Json.code -eq 200) ''

$r = Invoke-Api -Method POST -Path '/admin/settlement/generate' -Headers $adminH -Body @{
    merchantId = 1; periodStart = '2020-01-01'; periodEnd = '2030-12-31'
}
$settleOk = $r.Ok -and $r.Json.code -eq 200
Write-Result 'BE-SETTLE-01' 'Generate settlement' $settleOk $(if ($r.Json) { $r.Json.message })

if ($settleOk -and $r.Json.data.id -and $r.Json.data.status -eq 0) {
    $sid = $r.Json.data.id
    $r2 = Invoke-Api -Method POST -Path "/admin/settlement/confirm/$sid" -Headers $adminH
    Write-Result 'BE-SETTLE-02' 'Confirm settlement' ($r2.Ok -and $r2.Json.code -eq 200) ''
}

$r = Invoke-Api -Path '/admin/order/list?status=3&page=1&pageSize=1' -Headers $adminH
$shipOrderId = $null
if ($r.Ok -and $r.Json.data.list -and $r.Json.data.list.Count -gt 0) {
    $shipOrderId = $r.Json.data.list[0].id
}
if ($shipOrderId) {
    $r = Invoke-Api -Method POST -Path '/order/status/refund/apply' -Headers $userH -Body @{
        orderId = $shipOrderId; reason = 'smoke test refund'
    }
    Write-Result 'BE-REFUND-01' 'Apply refund' ($r.Ok -and $r.Json.code -eq 200) ''
    $r = Invoke-Api -Method POST -Path "/admin/order/refund/$shipOrderId" -Headers $adminH -Body @{
        approved = $false; remark = 'smoke reject'
    }
    Write-Result 'BE-REFUND-02' 'Reject refund' ($r.Ok -and $r.Json.code -eq 200) ''
}
else {
    Write-Result 'BE-REFUND-01' 'Apply refund' $true 'skipped: no shipped order'
    Write-Result 'BE-REFUND-02' 'Reject refund' $true 'skipped'
}

Write-Host ''
Write-Host "Passed: $script:Passed  Failed: $script:Failed"
$reportDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'test-reports'
$null = New-Item -ItemType Directory -Force -Path $reportDir
$reportPath = Join-Path $reportDir ("api-smoke-{0}.csv" -f (Get-Date -Format 'yyyyMMdd-HHmmss'))
$script:Results | Export-Csv -Path $reportPath -NoTypeInformation -Encoding UTF8
Write-Host "Report: $reportPath"
if ($script:Failed -gt 0) { exit 1 }
