@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   MediTrack Backend - Starting...
echo ========================================
echo.

cd /d "%~dp0"
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

if not exist "target\mabini-0.0.1-SNAPSHOT.jar" (
    echo ERROR: JAR file not found!
    echo Please run: mvnw.cmd clean package -DskipTests
    echo.
    pause
    exit /b 1
)

echo Starting on http://localhost:8080
echo API: http://localhost:8080/api
echo.
echo Press Ctrl+C to stop the server
echo.

"%JAVA_HOME%\bin\java.exe" -jar "target/mabini-0.0.1-SNAPSHOT.jar"
pause
