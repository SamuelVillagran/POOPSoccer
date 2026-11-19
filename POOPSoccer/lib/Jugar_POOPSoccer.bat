@echo off
cd /d "%~dp0"
where javaw >nul 2>&1
if errorlevel 1 (
    echo No se encontro Java instalado en el sistema.
    echo Instala Java 17 o superior para ejecutar POOPSoccer.
    pause
    exit /b 1
)
start "" javaw -jar "%~dp0POOPSoccer.jar"
