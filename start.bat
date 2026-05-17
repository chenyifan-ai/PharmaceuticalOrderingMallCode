@echo off
chcp 65001 >nul
echo ========================================
echo    医药订货系统 - 一键启动脚本
echo ========================================
echo.

REM 检查 Docker 是否运行
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker，请先安装并启动 Docker Desktop
    pause
    exit /b 1
)

echo [信息] 正在启动服务...
docker-compose up -d --build

if %errorlevel% neq 0 (
    echo [错误] 启动失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo ========================================
echo    服务启动成功！
echo ========================================
echo.
echo 后端服务: http://localhost:8080
echo 前端服务: http://localhost:3000
echo H2控制台: http://localhost:8080/h2-console
echo.
echo 查看日志: docker-compose logs -f
echo 停止服务: docker-compose down
echo.
pause
