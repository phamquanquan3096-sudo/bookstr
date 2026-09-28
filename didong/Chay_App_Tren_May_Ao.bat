@echo off
title "CHAY TOAN BO HE THONG BOOK SHOP"
color 0A

echo ===================================================
echo   CHAO MUNG DEN VOI HE THONG BAN HANG BOOK SHOP
echo ===================================================
echo.

:: 1. Kiem tra va Khoi dong Server Backend Java 8080 (neu chua chay)
netstat -ano | findstr :8080 | findstr LISTENING > nul
if errorlevel 1 (
    echo [1/3] Server Backend Java chua chay. Dang khoi dong tren Port 8080...
    start "Server Backend Java 8080" cmd /k "title Server Backend Java 8080 && cd /d "%~dp0qldl_java" && gradlew.bat bootRun"
) else (
    echo [1/3] Server Backend Java dang hoat dong san sang tren Port 8080!
)

:: 2. Kiem tra va khoi dong May ao Android (Pixel_7a)
echo.
echo [2/3] Dang kiem tra May ao Android (Pixel_7a)...
set "ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "EMU_PATH=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"

if not exist "%ADB_PATH%" (
    set "ADB_PATH=C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)
if not exist "%EMU_PATH%" (
    set "EMU_PATH=C:\Users\ASUS\AppData\Local\Android\Sdk\emulator\emulator.exe"
)

:: Kiem tra xem tien trinh emulator.exe da chay tren Windows hay chua
tasklist /FI "IMAGENAME eq emulator.exe" 2>nul | findstr /i "emulator.exe" >nul
if errorlevel 1 (
    echo    - May ao chua mo. Dang khoi dong May ao Pixel_7a...
    start "" "%EMU_PATH%" -avd Pixel_7a
) else (
    echo    - May ao Android Pixel_7a dang chay san sang!
)

echo    - Dang cho he dieu hanh Android khoi dong hoan tat...
:WAIT_BOOT
"%ADB_PATH%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
if errorlevel 1 (
    timeout /t 3 /nobreak >nul
    goto WAIT_BOOT
)
echo    - May ao Android Pixel_7a dang hoat dong san sang!

:: Cau hinh nguon điên Cắm Sạc AC + Bat hieu ung chuyen dong chuan 1.0 (tranh treo SplashScreen Android 12+)
"%ADB_PATH%" shell dumpsys battery set ac 1 2>nul
"%ADB_PATH%" shell dumpsys battery set status 2 2>nul
"%ADB_PATH%" shell settings put global window_animation_scale 1.0 2>nul
"%ADB_PATH%" shell settings put global transition_animation_scale 1.0 2>nul
"%ADB_PATH%" shell settings put global animator_duration_scale 1.0 2>nul
"%ADB_PATH%" shell settings put secure show_ime_with_hard_keyboard 1 2>nul
"%ADB_PATH%" shell settings put secure lockscreen.disabled 1 2>nul
"%ADB_PATH%" shell settings put system screen_off_timeout 1800000 2>nul
"%ADB_PATH%" shell settings put global stay_on_while_plugged_in 7 2>nul
"%ADB_PATH%" shell svc power stayon true 2>nul
"%ADB_PATH%" shell wm dismiss-keyguard 2>nul

:: 3. Bien dich va Cai dat App Android len May ao
echo.
echo [3/3] Dang bien dich va cai dat App Book Shop len May ao...
cd /d "%~dp0qldl_android"
call gradlew.bat installDebug

if errorlevel 1 (
    echo.
    echo [LOI] Bien dich hoac cai dat App gap loi! Vui long kiem tra lai.
    pause
    exit /b 1
)

:: 4. Mo ung dung Book Shop tren May ao
echo.
echo ===================================================
echo   DANG MO UNG DUNG BOOK SHOP TREN MAY AO...
echo ===================================================
"%ADB_PATH%" shell dumpsys battery set ac 1 2>nul
"%ADB_PATH%" shell dumpsys battery set status 2 2>nul
"%ADB_PATH%" shell settings put global stay_on_while_plugged_in 7 2>nul
"%ADB_PATH%" shell settings put system screen_off_timeout 1800000 2>nul
"%ADB_PATH%" shell settings put secure lockscreen.disabled 1 2>nul
"%ADB_PATH%" shell svc power stayon true 2>nul
"%ADB_PATH%" shell wm dismiss-keyguard 2>nul
"%ADB_PATH%" shell am start -n com.quanly.app/.ui.MainActivity

echo.
echo [THANH CONG] Toan bo he thong (May ao + Server + App Book Shop) da duoc khoi chay!
echo Ban co the su dung ung dung ngay lap tuc tren may ao.
timeout /t 5
