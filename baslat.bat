@echo off
chcp 65001 >nul
title Yuz Tanima ile Yoklama Sistemi

echo ========================================
echo   Adiyaman Universitesi
echo   Yuz Tanima ile Yoklama Sistemi
echo ========================================
echo.

REM Python servisi
echo [1/2] Python yuz tanima servisi baslatiliyor...
echo     * Numpy versiyonu kontrol ediliyor (2.x uyumsuzlugu icin)...
cmd /c "py -3.12 -m pip install "numpy<2""
start "Python Yuz Tanima Servisi" cmd /k "cd /d %~dp0python-yuz-servisi && py -3.12 app.py"

timeout /t 3 /nobreak >nul

REM Spring Boot backend
echo [2/2] Spring Boot backend baslatiliyor...
echo.

REM 20 saniye sonra tarayici ac
start "" cmd /c "timeout /t 20 /nobreak >nul && start http://localhost:8080"

cd /d "%~dp0backend"
.\mvnw.cmd spring-boot:run

pause
