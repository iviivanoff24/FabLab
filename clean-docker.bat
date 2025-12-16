@echo off
cd /d "%~dp0"
echo [INFO] Deteniendo y eliminando recursos Docker del FabLab...

echo [1/5] Deteniendo contenedor...
docker stop fablab-mysql

echo [2/5] Eliminando contenedor...
docker rm fablab-mysql

echo [3/5] Eliminando volumen de datos...
docker volume rm fablab_mysql_data

echo [4/5] Eliminando red...
docker network rm fablab_net

echo [EXITO] Limpieza completada.
pause
