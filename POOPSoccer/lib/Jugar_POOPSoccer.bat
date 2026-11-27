@echo off
setlocal
cd /d "%~dp0"

set "JAVAW_CMD="
set "JAVAC_CMD="
set "JAR_CMD="

if defined JAVA_HOME call :try_jdk_bin "%JAVA_HOME%\bin"

if not defined JAVAC_CMD (
    for /f "delims=" %%I in ('where javac 2^>nul') do call :try_jdk_bin "%%~dpI"
)

if not defined JAVAC_CMD (
    for /d %%D in ("%ProgramFiles%\Java\jdk-*") do call :try_jdk_dir "%%~D"
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do call :try_jdk_dir "%%~D"
    for /d %%D in ("%ProgramFiles%\Microsoft\jdk-*") do call :try_jdk_dir "%%~D"
    for /d %%D in ("%USERPROFILE%\.jdks\*") do call :try_jdk_dir "%%~D"
)

if not defined JAVAW_CMD (
    if defined JAVA_HOME call :try_jre_bin "%JAVA_HOME%\bin"
    for /f "delims=" %%I in ('where java 2^>nul') do call :try_jre_bin "%%~dpI"
    for /d %%D in ("%ProgramFiles%\Java\*") do call :try_jre_dir "%%~D"
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\*") do call :try_jre_dir "%%~D"
    for /d %%D in ("%ProgramFiles%\Microsoft\*") do call :try_jre_dir "%%~D"
    for /d %%D in ("%USERPROFILE%\.jdks\*") do call :try_jre_dir "%%~D"
)

if not defined JAVAW_CMD (
    echo No se encontro Java 21 o superior en el sistema.
    if defined JAVA_HOME echo JAVA_HOME actual apunta a "%JAVA_HOME%", pero se requiere Java 21 o superior.
    echo Instala JDK/JRE 21 o superior para ejecutar POOPSoccer.
    pause
    exit /b 1
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
"%JAVAC_CMD%" --release 21 -encoding UTF-8 -cp "%~dp0sqlite-jdbc-3.54.1.0.jar" -d "..\out\production\POOPSoccer" "@%TEMP%\poopsoccer_sources.txt"
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

if /i "%~1"=="--build-only" exit /b 0

start "" "%JAVAW_CMD%" -jar "%~dp0POOPSoccer.jar"
exit /b 0

:try_jdk_dir
call :try_jdk_bin "%~1\bin"
exit /b 0

:try_jdk_bin
if defined JAVAC_CMD exit /b 0
set "CAND_BIN=%~1"
if not defined CAND_BIN exit /b 0
if "%CAND_BIN:~-1%"=="\" set "CAND_BIN=%CAND_BIN:~0,-1%"
if not exist "%CAND_BIN%\javac.exe" exit /b 0
if not exist "%CAND_BIN%\jar.exe" exit /b 0
if not exist "%CAND_BIN%\javaw.exe" exit /b 0
set "JDK_MAJOR=0"
for /f "tokens=2 delims= " %%V in ('""%CAND_BIN%\javac.exe" -version" 2^>^&1') do (
    for /f "tokens=1 delims=.-_+" %%M in ("%%V") do set "JDK_MAJOR=%%M"
)
set "JDK_NUM=0"
set /a "JDK_NUM=JDK_MAJOR" 2>nul
if %JDK_NUM% GEQ 21 (
    set "JAVAC_CMD=%CAND_BIN%\javac.exe"
    set "JAR_CMD=%CAND_BIN%\jar.exe"
    set "JAVAW_CMD=%CAND_BIN%\javaw.exe"
)
exit /b 0

:try_jre_dir
call :try_jre_bin "%~1\bin"
exit /b 0

:try_jre_bin
if defined JAVAW_CMD exit /b 0
set "CAND_BIN=%~1"
if not defined CAND_BIN exit /b 0
if "%CAND_BIN:~-1%"=="\" set "CAND_BIN=%CAND_BIN:~0,-1%"
if not exist "%CAND_BIN%\java.exe" exit /b 0
if not exist "%CAND_BIN%\javaw.exe" exit /b 0
set "JRE_MAJOR=0"
for /f "tokens=3 delims= " %%V in ('""%CAND_BIN%\java.exe" -version" 2^>^&1 ^| findstr /i "version"') do (
    for /f "tokens=1 delims=.-_+" %%M in ("%%~V") do set "JRE_MAJOR=%%M"
)
set "JRE_NUM=0"
set /a "JRE_NUM=JRE_MAJOR" 2>nul
if %JRE_NUM% GEQ 21 (
    set "JAVAW_CMD=%CAND_BIN%\javaw.exe"
)
exit /b 0
