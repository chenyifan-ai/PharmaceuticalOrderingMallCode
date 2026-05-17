@echo off
chcp 65001 >nul
echo ========================================
echo    医药订货系统 - 停止服务脚本
echo ========================================
echo.

echo [信息] 正在停止服务...
docker-compose down

echo.
echo [信息] 服务已停止
echo.
pause
