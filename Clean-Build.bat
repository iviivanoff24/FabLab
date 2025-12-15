@echo off
cd /d "%~dp0"
if exist target (
    echo Borrando carpeta target...
    rmdir /s /q target
)
mvnw clean package
pause