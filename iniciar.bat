@echo off
chcp 65001 >nul 2>&1
:: ============================================================
::  iniciar.bat — Arranca el servidor Spring Boot (Windows)
:: ============================================================
::
::  USO:    Haz doble clic en este archivo, o abre una terminal
::          en la carpeta del proyecto y ejecuta:
::
::            iniciar.bat
::
::  QUE HACE ESTE SCRIPT?
::
::    1. Comprueba que Java esta instalado
::    2. Compila el proyecto con Maven (sin necesidad de instalarlo)
::    3. Arranca un servidor Tomcat embebido en el puerto 8080
::    4. Despliega nuestra API REST automaticamente
::
::  Para PARAR el servidor: pulsa Ctrl + C en esta ventana
::
:: ============================================================

cls

echo ================================================================
echo           UserApp - Spring Boot + JPA + H2
echo           Proyecto de ejemplo - DAW / DAM
echo ================================================================
echo.

:: -- Paso 1: Ir a la carpeta del script --
cd /d "%~dp0"

if not exist "pom.xml" (
    echo ERROR: No se encuentra el archivo pom.xml
    echo    Asegurate de ejecutar este script desde la carpeta del proyecto.
    pause
    exit /b 1
)

echo Carpeta del proyecto: %cd%
echo.

:: -- Paso 2: Comprobar que Java esta instalado --
echo Comprobando Java...

java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Java no esta instalado o no esta en el PATH.
    echo.
    echo    Instala Java 17 o superior desde:
    echo      https://adoptium.net/
    echo.
    echo    Despues de instalar, cierra y vuelve a abrir esta ventana.
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    echo    OK: %%i
)
echo.

:: -- Paso 3: Comprobar que el Maven Wrapper existe --
if not exist "mvnw.cmd" (
    echo ERROR: No se encuentra mvnw.cmd ^(Maven Wrapper^)
    echo    El proyecto necesita el archivo mvnw.cmd para compilar.
    pause
    exit /b 1
)

:: -- Paso 4: Arrancar Spring Boot --
echo Arrancando el servidor Spring Boot...
echo.
echo    Esto puede tardar unos segundos la primera vez
echo    (Maven descarga las dependencias necesarias).
echo.
echo ================================================================
echo    Cuando veas 'Started UserappApplication', abre:
echo.
echo       http://localhost:8080
echo.
echo    Para PARAR el servidor: pulsa Ctrl + C
echo ================================================================
echo.

call mvnw.cmd spring-boot:run

pause
