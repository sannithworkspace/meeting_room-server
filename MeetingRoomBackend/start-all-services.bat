@echo off
setlocal enabledelayedexpansion

title MeetingRoom Services Launcher

echo ============================================================
echo   Meeting Room Enterprise Microservices Platform Launcher
echo ============================================================
echo.

:: Step 0: Free occupied microservice ports
echo [PRE-FLIGHT] Terminating any existing Java processes to free ports...
taskkill /F /IM java.exe /T >nul 2>&1
timeout /t 2 /nobreak >nul

:: Step 1: Ensure all JARs are compiled and packaged
if not exist "config-server\target\config-server-1.0.0-SNAPSHOT.jar" (
    echo [BUILD] Config Server JAR missing. Compiling all microservices...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Maven build failed. Please resolve build errors.
        pause
        exit /b 1
    )
)
if not exist "api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar" (
    echo [BUILD] API Gateway JAR missing. Compiling all microservices...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Maven build failed. Please resolve build errors.
        pause
        exit /b 1
    )
)

:: Step 2: Launch Config Server first
echo [1/7] Starting Config Server (Port 8888)...
start "ConfigServer" cmd /k "title ConfigServer && java -jar config-server\target\config-server-1.0.0-SNAPSHOT.jar"
echo Waiting 8 seconds for Config Server initialization...
timeout /t 8 /nobreak >nul

:: Step 3: Launch Eureka Server second
echo [2/7] Starting Eureka Service Discovery (Port 8761)...
start "EurekaServer" cmd /k "title EurekaServer && java -jar eureka-server\target\eureka-server-1.0.0-SNAPSHOT.jar"
echo Waiting 8 seconds for Eureka Registry initialization...
timeout /t 8 /nobreak >nul

:: Step 4: Launch Core Subservices
echo [3/7] Starting User Management Service (Port 8084)...
start "UserService" cmd /k "title UserService && java -jar user-service\target\user-service-1.0.0-SNAPSHOT.jar"

echo [4/7] Starting Authentication Service (Port 8085)...
start "AuthService" cmd /k "title AuthService && java -jar auth-service\target\auth-service-1.0.0-SNAPSHOT.jar"

echo [5/7] Starting Meeting Room Service (Port 8082)...
start "RoomService" cmd /k "title RoomService && java -jar room-service\target\room-service-1.0.0-SNAPSHOT.jar"

echo [6/7] Starting Booking Service (Port 8083)...
start "BookingService" cmd /k "title BookingService && java -jar booking-service\target\booking-service-1.0.0-SNAPSHOT.jar"

echo Waiting 10 seconds for downstream services to register with Eureka...
timeout /t 10 /nobreak >nul

:: Step 5: Launch API Gateway last so routes register dynamically
echo [7/7] Starting API Gateway Ingress (Port 8080)...
start "ApiGateway" cmd /k "title ApiGateway && java -jar api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar"

echo.
echo ============================================================
echo   ALL 7 MICROSERVICES LAUNCHED SUCCESSFULLY
echo ============================================================
echo   1. Config Server   : http://localhost:8888
echo   2. Eureka Server   : http://localhost:8761 (User: admin / Pass: admin123)
echo   3. User Service    : http://localhost:8084
echo   4. Auth Service    : http://localhost:8085
echo   5. Room Service    : http://localhost:8082
echo   6. Booking Service : http://localhost:8083
echo   7. API Gateway     : http://localhost:8080
echo   --------------------------------------------------------
echo   Swagger Documentation: http://localhost:8080/swagger-ui.html
echo   Zipkin Tracing UI    : http://localhost:9411
echo ============================================================
echo.
echo Press 'C' and hit ENTER to SHUT DOWN ALL SERVICES AT ONCE.
echo ============================================================

:loop
set /p userinput="Type 'c' and hit ENTER to stop all microservices: "
if /i "%userinput%"=="c" goto shutdown
goto loop

:shutdown
echo.
echo ============================================================
echo   TERMINATING ALL MICROSERVICES...
echo ============================================================

taskkill /F /IM java.exe /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq ConfigServer*" /F /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq EurekaServer*" /F /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq UserService*" /F /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq AuthService*" /F /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq RoomService*" /F /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq BookingService*" /F /T >nul 2>&1
taskkill /FI "WINDOWTITLE eq ApiGateway*" /F /T >nul 2>&1

echo [SUCCESS] All microservices terminated.
echo.
pause
exit /b 0
