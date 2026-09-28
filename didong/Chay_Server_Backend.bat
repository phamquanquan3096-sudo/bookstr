@echo off
title "Server Backend Java 8080"
echo ===================================================
echo   DANG KHOI DONG SERVER BACKEND JAVA (PORT 8080)...
echo ===================================================

cd /d "%~dp0qldl_java"
call gradlew.bat bootRun
pause
