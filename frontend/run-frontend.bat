@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   MediTrack Frontend - Starting...
echo ========================================
echo.

cd /d "%~dp0"

if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
)

echo Starting on http://localhost:5173
echo.
echo Press Ctrl+C to stop the server
echo.

call npm run dev
pause
