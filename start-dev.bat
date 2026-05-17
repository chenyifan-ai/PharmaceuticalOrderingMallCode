@echo off
chcp 65001 >nul
echo ========================================
echo    医药订货系统 - 开发环境启动脚本
echo ========================================
echo.

REM 检查 Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 17
    pause
    exit /b 1
)

REM 检查 Node.js
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

echo [信息] 正在启动后端服务（后台运行）...
start "Backend" cmd /k "cd /d %~dp0 && mvnw spring-boot:run"

echo [信息] 等待后端启动...
timeout /t 10 /nobreak >nul

echo [信息] 正在启动前端服务...
cd /d %~dp0\admin-web
if not exist "node_modules" (
    echo [信息] 首次运行，正在安装前端依赖...
    call npm install
)
start "Frontend" cmd /k "cd /d %~dp0\admin-web && npm run dev"

cd /d %~dp0
echo.
echo ========================================
echo    开发环境启动中！
echo ========================================
echo.
echo 后端服务: http://localhost:8080
echo 前端服务: http://localhost:5173
echo H2控制台: http://localhost:8080/h2-console
echo.
echo 按任意键停止所有服务...
pause >nul

echo [信息] 正在停止服务...
taskkill /FI "WINDOWTITLE eq Backend*" /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq Frontend*" /T >nul 2>&1
echo [信息] 服务已停止
pause
