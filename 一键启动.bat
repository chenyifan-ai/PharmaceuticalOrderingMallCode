@echo off
chcp 65001 >nul

title 医药商城 - 一键启动

echo ========================================
echo      医药商城 - 一键启动
echo ========================================
echo.

:: ====== 设置Java路径 ======
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: ====== 第一步：构建后端 ======
echo [1/3] 构建后端...
echo.

cd /d d:\code\yao

:: 先清理已有进程
taskkill /F /IM java.exe >nul 2>&1
timeout /t 1 /nobreak >nul

call mvnw.cmd clean package -DskipTests -q

if errorlevel 1 (
    echo [ERROR] 后端构建失败！请检查 pom.xml 和源码。
    pause
    exit /b 1
)
echo [OK] 后端构建成功！
echo.

:: ====== 第二步：启动后端 ======
echo [2/3] 启动后端服务（端口 8080）...
echo.

start "Pharmacy-Mall-Backend" /B java -jar target\pharmacy-mall-1.0.0.jar

timeout /t 8 /nobreak >nul

:: 验证后端是否启动
netstat -ano | findstr ":8080 " >nul
if errorlevel 1 (
    echo [WARN] 后端可能尚未就绪，检查 backend.log...
) else (
    echo [OK] 后端已启动 http://localhost:8080
)
echo.

:: ====== 第三步：启动前端 ======
echo [3/3] 启动管理后台前端（端口 3000）...
echo.

cd /d d:\code\yao\admin-web

if not exist "node_modules" (
    echo 安装前端依赖...
    call npm install
)

start "Pharmacy-Mall-Frontend" /B cmd /c "npm run dev"

timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo  全部启动完成！
echo ========================================
echo.
echo  后端地址:     http://localhost:8080
echo  H2 Console:   http://localhost:8080/h2-console
echo  管理后台:     http://localhost:3000/login
echo.
echo  管理员: 13800000000 / admin123
echo  商  家: 13800000001 / admin123
echo  用  户: 13800000002 / admin123
echo.
echo  管理后台登录页已默认填充管理员账号
echo ========================================
pause
