@echo off
title Mo May Ao Android (Pixel_7a)
echo Dang khoi dong may ao Android Pixel_7a...
set "ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "EMU_PATH=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"

if not exist "%ADB_PATH%" set "ADB_PATH=C:\Users\ASUS\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if not exist "%EMU_PATH%" set "EMU_PATH=C:\Users\ASUS\AppData\Local\Android\Sdk\emulator\emulator.exe"

:: Neu ADB bi unauthorized hoac offline, tu dong restart adb server
"%ADB_PATH%" devices 2>nul | findstr /i "unauthorized offline" >nul
if not errorlevel 1 (
    echo Phat hien ket noi ADB bi treo/unauthorized. Dang reset ADB server...
    "%ADB_PATH%" kill-server >nul 2>&1
    "%ADB_PATH%" start-server >nul 2>&1
)

:: Kiem tra xem tien trinh emulator.exe da chay tren Windows hay chưa
tasklist /FI "IMAGENAME eq emulator.exe" 2>nul | findstr /i "emulator.exe" >nul
if errorlevel 1 (
    echo Dang khoi dong May ao Pixel_7a...
    start "" "%EMU_PATH%" -avd Pixel_7a
) else (
    echo May ao Android Pixel_7a dang chay san sang!
)

set WAIT_COUNT=0
:WAIT_BOOT
set /a WAIT_COUNT+=1
"%ADB_PATH%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
if errorlevel 1 (
    if %WAIT_COUNT% GEQ 10 (
        echo Reset lai ket noi ADB daemon...
        "%ADB_PATH%" kill-server >nul 2>&1
        "%ADB_PATH%" start-server >nul 2>&1
        set WAIT_COUNT=0
    )
    timeout /t 2 /nobreak >nul
    goto WAIT_BOOT
)

:: Chuyen may ao sang che do Cam Sac AC 100% + Tat hieu ung chuyen dong
"%ADB_PATH%" shell dumpsys battery set ac 1 2>nul
"%ADB_PATH%" shell dumpsys battery set status 2 2>nul
"%ADB_PATH%" shell settings put global window_animation_scale 1.0 2>nul
"%ADB_PATH%" shell settings put global transition_animation_scale 1.0 2>nul
"%ADB_PATH%" shell settings put global animator_duration_scale 1.0 2>nul
"%ADB_PATH%" shell settings put lockscreen.disabled 1 2>nul
"%ADB_PATH%" shell settings put system screen_off_timeout 1800000 2>nul
"%ADB_PATH%" shell settings put global stay_on_while_plugged_in 7 2>nul
"%ADB_PATH%" shell svc power stayon true 2>nul
"%ADB_PATH%" shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS 2>nul
"%ADB_PATH%" shell input keyevent 82 2>nul
"%ADB_PATH%" shell wm dismiss-keyguard 2>nul

echo May ao Android Pixel_7a da bat sang va san sang!
timeout /t 3
