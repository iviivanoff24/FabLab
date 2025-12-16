@echo off
cd /d "%~dp0"
echo [INFO] Verificando entorno Docker para FabLab...

REM 1. Verificar si existe el contenedor
docker ps -a --format "{{.Names}}" | findstr /X "fablab-mysql" >nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] El contenedor 'fablab-mysql' ya existe.
    echo [INFO] Iniciando contenedor...
    docker start fablab-mysql
) else (
    echo [INFO] El contenedor 'fablab-mysql' NO existe. Procediendo con instalacion limpia.
    REM 2. Volumen de datos
    echo [SETUP] Verificando volumen de datos 'fablab_mysql_data'...
    docker volume inspect fablab_mysql_data >nul 2>&1 || docker volume create fablab_mysql_data

    REM 3. Red Docker
    echo [SETUP] Verificando red 'fablab_net'...
    docker network inspect fablab_net >nul 2>&1 || docker network create fablab_net

    REM 4. Docker Run
    echo [INFO] Creando y ejecutando nuevo contenedor...
    docker run --name fablab-mysql ^
      --network fablab_net ^
      -e MYSQL_ROOT_PASSWORD=admin ^
      -e MYSQL_DATABASE=fablabdb ^
      -e MYSQL_USER=admin ^
      -e MYSQL_PASSWORD=admin ^
      -p 3307:3306 ^
      -v fablab_mysql_data:/var/lib/mysql ^
      -v "%~dp0docker\mysql\init:/docker-entrypoint-initdb.d:ro" ^
      -d mysql:8.0
)

if %ERRORLEVEL% EQU 0 (
    echo [EXITO] Contenedor MySQL operando en puerto 3307.
) else (
    echo [ERROR] Hubo un problema al iniciar el contenedor.
)
pause
