@echo off
setlocal

echo.
echo ========================================
echo   MediTrack Backend - Starting...
echo ========================================
echo.

REM Change to the backend directory
cd /d "%~dp0"

REM Check if the JAR exists
if not exist "target\mabini-0.0.1-SNAPSHOT.jar" (
    echo ERROR: JAR file not found!
    echo Please run:
    echo     .\mvnw.cmd clean package -DskipTests
    echo.
    pause
    exit /b 1
)

echo Java Version:
java -version
echo.

echo ========================================
echo Starting Backend...
echo ========================================
echo.
echo Backend: http://localhost:8080
echo API:     http://localhost:8080/api
echo.
echo Press Ctrl+C to stop the server.
echo.

java -jar "target\mabini-0.0.1-SNAPSHOT.jar"

echo.
echo ========================================
echo Backend stopped.
echo ========================================
pause