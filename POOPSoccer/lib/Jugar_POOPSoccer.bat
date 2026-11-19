@echo off
cd /d "%~dp0"
where javaw >nul 2>&1
if errorlevel 1 (
    echo No se encontro Java instalado en el sistema.
    echo Instala Java 17 o superior para ejecutar POOPSoccer.
    pause
    exit /b 1
)
if not exist "%~dp0POOPSoccer.jar" (
    echo No se encontro POOPSoccer.jar en "%~dp0".
    pause
    exit /b 1
)
if not exist "%~dp0sqlite-jdbc-3.54.1.0.jar" (
    echo No se encontro sqlite-jdbc-3.54.1.0.jar en "%~dp0".
    pause
    exit /b 1
)
start "" javaw -jar "%~dp0POOPSoccer.jar"
