@echo off
setlocal
cd /d "%~dp0"

set "JAVAW_CMD="
set "JAVAC_CMD="
set "JAR_CMD="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW_CMD=%JAVA_HOME%\bin\javaw.exe"
    if exist "%JAVA_HOME%\bin\javac.exe" (
        if exist "%JAVA_HOME%\bin\jar.exe" (
            set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
            set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
        )
    )
)

if not defined JAVAW_CMD (
    where javaw >nul 2>&1
    if errorlevel 1 (
        echo No se encontro Java instalado en el sistema.
        echo Instala Java 17 o superior para ejecutar POOPSoccer.
        pause
        exit /b 1
    )
    set "JAVAW_CMD=javaw"
)

if not defined JAVAC_CMD (
    where javac >nul 2>&1
    if not errorlevel 1 (
        where jar >nul 2>&1
        if not errorlevel 1 (
            set "JAVAC_CMD=javac"
            set "JAR_CMD=jar"
        )
    )
)

if not exist "%~dp0sqlite-jdbc-3.54.1.0.jar" (
    echo No se encontro sqlite-jdbc-3.54.1.0.jar en "%~dp0".
    pause
    exit /b 1
)

if not exist "..\src\main\java\presentation\POOPSoccerGUI.java" goto :launch
if not exist "..\src\main\resources" goto :launch
if not defined JAVAC_CMD goto :launch

if exist "..\out\production\POOPSoccer" rmdir /s /q "..\out\production\POOPSoccer"
mkdir "..\out\production\POOPSoccer"

(
    dir /s /b "..\src\main\java\domain\*.java"
    dir /s /b "..\src\main\java\persistence\*.java"
    dir /s /b "..\src\main\java\presentation\*.java"
) > "%TEMP%\poopsoccer_sources.txt"

echo Compilando codigo actual...
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -cp "%~dp0sqlite-jdbc-3.54.1.0.jar" -d "..\out\production\POOPSoccer" "@%TEMP%\poopsoccer_sources.txt"
if errorlevel 1 (
    del /q "%TEMP%\poopsoccer_sources.txt" >nul 2>&1
    echo Error durante la compilacion.
    pause
    exit /b 1
)
del /q "%TEMP%\poopsoccer_sources.txt" >nul 2>&1

xcopy /E /I /Y /Q "..\src\main\resources\*" "..\out\production\POOPSoccer\" >nul
if errorlevel 1 (
    echo Error copiando recursos.
    pause
    exit /b 1
)

(
    echo Manifest-Version: 1.0
    echo Main-Class: presentation.POOPSoccerGUI
    echo Class-Path: sqlite-jdbc-3.54.1.0.jar
) > "%TEMP%\poopsoccer_manifest.mf"

if exist "%~dp0POOPSoccer.jar" del /q "%~dp0POOPSoccer.jar"
"%JAR_CMD%" cfm "%~dp0POOPSoccer.jar" "%TEMP%\poopsoccer_manifest.mf" -C "..\out\production\POOPSoccer" .
if errorlevel 1 (
    del /q "%TEMP%\poopsoccer_manifest.mf" >nul 2>&1
    echo Error generando POOPSoccer.jar.
    pause
    exit /b 1
)
del /q "%TEMP%\poopsoccer_manifest.mf" >nul 2>&1

:launch
if not exist "%~dp0POOPSoccer.jar" (
    echo No se encontro POOPSoccer.jar en "%~dp0".
    pause
    exit /b 1
)

start "" "%JAVAW_CMD%" -jar "%~dp0POOPSoccer.jar"
