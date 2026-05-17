# Frontend static smoke: routes and view files exist
$root = Join-Path (Split-Path $PSScriptRoot -Parent) 'admin-web\src'
$routes = @(
    @{ Id='FE-ROUTE-01'; Path='views\RefundAuditList.vue'; Route='refund-audit' },
    @{ Id='FE-ROUTE-02'; Path='views\SettlementList.vue'; Route='settlements' },
    @{ Id='FE-ROUTE-03'; Path='views\PaymentVoucherList.vue'; Route='payment-vouchers' },
    @{ Id='FE-ROUTE-04'; Path='views\CPurchaseStats.vue'; Route='purchase-stats' },
    @{ Id='FE-ROUTE-05'; Path='views\CPrescriptions.vue'; Route='prescriptions' },
    @{ Id='FE-ROUTE-06'; Path='views\CPay.vue'; Route='pay' },
    @{ Id='FE-ROUTE-07'; Path='api\admin\settlement.js'; Route='settlements' }
)
$routerText = @(
    (Get-Content (Join-Path $root 'router\index.js') -Raw -Encoding UTF8)
    (Get-Content (Join-Path $root 'router\consumer-routes.js') -Raw -Encoding UTF8)
) -join ' '
$passed = 0; $failed = 0
foreach ($item in $routes) {
    $fileOk = Test-Path (Join-Path $root $item.Path)
    $routeOk = $routerText -match $item.Route
    $ok = $fileOk -and $routeOk
    if ($ok) { $passed++ } else { $failed++ }
    $st = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Host "[$st] $($item.Id) $($item.Path) route=$($item.Route)"
}
Write-Host "Passed: $passed Failed: $failed"
if ($failed -gt 0) { exit 1 }
