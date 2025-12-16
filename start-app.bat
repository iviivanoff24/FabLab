@echo off
echo [INFO] Iniciando aplicacion Spring Boot (Clean + Compile + Run)...

echo [INFO] Eliminando carpeta target...
if exist "target" rmdir /s /q "target"

call mvnw clean compile spring-boot:run
pause
