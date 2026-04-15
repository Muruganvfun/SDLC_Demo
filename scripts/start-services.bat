@echo off
set JAVA_HOME=C:\trainings\factoryAI\SDLC_Demo\tools\jdk-22.0.1
set PATH=%JAVA_HOME%\bin;%PATH%
set MVN=C:\trainings\factoryAI\SDLC_Demo\tools\apache-maven-3.9.6\bin\mvn.cmd

echo Starting all OMS backend services...
echo.

start "AUTH-SERVICE 8081" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\auth-service && %MVN% spring-boot:run"
start "CATALOG-SERVICE 8082" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\catalog-service && %MVN% spring-boot:run"
start "INVENTORY-SERVICE 8084" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\inventory-service && %MVN% spring-boot:run"
start "ORDER-SERVICE 8083" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\order-service && %MVN% spring-boot:run"
start "PAYMENT-SERVICE 8085" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\payment-service && %MVN% spring-boot:run"
start "SHIPPING-SERVICE 8086" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\shipping-service && %MVN% spring-boot:run"
start "NOTIFICATION-SERVICE 8087" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\notification-service && %MVN% spring-boot:run"

echo Waiting 60 seconds for services to start...
timeout /t 60 /nobreak

start "API-GATEWAY 8080" cmd /k "cd /d C:\trainings\factoryAI\SDLC_Demo\backend\api-gateway && %MVN% spring-boot:run"

echo.
echo All services are starting. Check individual windows for status.
echo Services will be available at:
echo   - Auth Service: http://localhost:8081
echo   - Catalog Service: http://localhost:8082
echo   - Order Service: http://localhost:8083
echo   - Inventory Service: http://localhost:8084
echo   - Payment Service: http://localhost:8085
echo   - Shipping Service: http://localhost:8086
echo   - Notification Service: http://localhost:8087
echo   - API Gateway: http://localhost:8080
pause
