# 医药订货系统 - PowerShell 一键启动脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   医药订货系统 - 一键启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Docker 是否运行
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "[信息] Docker 已安装: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "[错误] 未检测到 Docker，请先安装并启动 Docker Desktop" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

# 检查 Docker 是否运行
try {
    $null = docker ps 2>&1
} catch {
    Write-Host "[错误] Docker 未运行，请先启动 Docker Desktop" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "[信息] 正在启动服务..." -ForegroundColor Yellow
docker-compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Host "[错误] 启动失败，请检查错误信息" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "   服务启动成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "后端服务: http://localhost:8080" -ForegroundColor White
Write-Host "前端服务: http://localhost:3000" -ForegroundColor White
Write-Host "H2控制台: http://localhost:8080/h2-console" -ForegroundColor White
Write-Host ""
Write-Host "查看日志: docker-compose logs -f" -ForegroundColor Gray
Write-Host "停止服务: docker-compose down" -ForegroundColor Gray
Write-Host ""
Read-Host "按回车键退出"
