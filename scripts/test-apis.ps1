$base = "http://localhost:8080"
$headers = @{ "Content-Type" = "application/json; charset=utf-8" }

function Login($phone, $pwd) {
    $body = @{ phone = $phone; password = $pwd } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -Headers $headers -Body $body
    if ($r.code -ne 200) { throw "login failed $phone : $($r.message)" }
    return $r.data.token
}

function Api($method, $path, $token, $body = $null, $query = $null) {
    $h = @{ Authorization = "Bearer $token"; Accept = "application/json" }
    $uri = "$base$path"
    if ($query) {
        $qs = ($query.GetEnumerator() | ForEach-Object { "$($_.Key)=$([uri]::EscapeDataString([string]$_.Value))" }) -join "&"
        $uri += "?$qs"
    }
    try {
        $params = @{ Uri = $uri; Method = $method; Headers = $h; TimeoutSec = 15 }
        if ($body -ne $null) {
            $params.ContentType = "application/json; charset=utf-8"
            $params.Body = ($body | ConvertTo-Json -Depth 10 -Compress)
        }
        $r = Invoke-RestMethod @params
        return @{ ok = ($r.code -eq 200); code = $r.code; msg = $r.message; path = $path }
    } catch {
        return @{ ok = $false; code = "ERR"; msg = $_.Exception.Message; path = $path }
    }
}

$adminTok = Login "13800000000" "admin123"
$merchantTok = Login "13800000001" "admin123"
$consumerTok = Login "13800000002" "admin123"

$tests = @(
    @{ m="GET"; p="/api/admin/product/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/admin/product/1"; t=$adminTok },
    @{ m="GET"; p="/api/admin/order/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/admin/order/detail/1"; t=$adminTok },
    @{ m="GET"; p="/api/admin/invoice/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/admin/invoice/1"; t=$adminTok },
    @{ m="GET"; p="/api/admin/merchant/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/admin/merchant/1"; t=$adminTok },
    @{ m="GET"; p="/api/admin/user/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/admin/user/1"; t=$adminTok },
    @{ m="GET"; p="/api/admin/qualification/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/admin/qualification/1"; t=$adminTok },
    @{ m="GET"; p="/api/admin/prescription/list"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/order/status/history/1"; t=$adminTok },
    @{ m="GET"; p="/api/c/product/list"; t=$consumerTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/c/product/detail/1"; t=$consumerTok },
    @{ m="GET"; p="/api/c/product/categories"; t=$consumerTok },
    @{ m="GET"; p="/api/c/product/category-tree"; t=$consumerTok },
    @{ m="GET"; p="/api/c/product/recommend"; t=$consumerTok; q=@{page=1;pageSize=5} },
    @{ m="GET"; p="/api/c/product/hot"; t=$consumerTok; q=@{page=1;pageSize=5} },
    @{ m="GET"; p="/api/c/order/list"; t=$consumerTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/c/order/1"; t=$consumerTok },
    @{ m="GET"; p="/api/merchant/order/list"; t=$merchantTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/merchant/product/list"; t=$merchantTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/merchant/info/me"; t=$merchantTok },
    @{ m="GET"; p="/api/merchant/invoice/list"; t=$merchantTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/user/info"; t=$consumerTok },
    @{ m="GET"; p="/api/cart/list"; t=$consumerTok },
    @{ m="GET"; p="/api/message/list"; t=$consumerTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/message/unreadCount"; t=$consumerTok },
    @{ m="GET"; p="/api/address/list"; t=$consumerTok },
    @{ m="GET"; p="/api/contact/list"; t=$consumerTok },
    @{ m="GET"; p="/api/order/list"; t=$consumerTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/order/detail/1"; t=$consumerTok },
    @{ m="GET"; p="/api/prescription/list"; t=$consumerTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/user/invoice/list"; t=$consumerTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/qualification/my"; t=$consumerTok },
    @{ m="GET"; p="/api/product/list"; t=$null; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/category/tree"; t=$null },
    @{ m="GET"; p="/api/pharmacist/prescriptions/pending"; t=$adminTok; q=@{page=1;pageSize=10} },
    @{ m="GET"; p="/api/pharmacist/statistics"; t=$adminTok },
    @{ m="GET"; p="/api/product/tier-price/get/1"; t=$consumerTok },
    @{ m="GET"; p="/api/payment/status/1"; t=$consumerTok },
    @{ m="GET"; p="/api/medicationUser/list"; t=$consumerTok },
    @{ m="GET"; p="/api/admin/prescription/detail/1"; t=$adminTok }
)

$fail = @()
foreach ($t in $tests) {
    $r = Api $t.m $t.p $t.t $null $t.q
    if (-not $r.ok) { $fail += $r }
    $status = if ($r.ok) { "OK" } else { "FAIL" }
    Write-Host "$status $($t.m) $($t.p) code=$($r.code) msg=$($r.msg)"
}

Write-Host "`n=== FAILED: $($fail.Count) ===" -ForegroundColor Red
$fail | ForEach-Object { Write-Host "$($_.path) -> $($_.msg)" }
