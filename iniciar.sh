#!/bin/bash
# ============================================================
#  iniciar.sh — Arranca el servidor Spring Boot (macOS/Linux)
# ============================================================
#
#  USO:    Abre una terminal en la carpeta del proyecto y ejecuta:
#
#            ./iniciar.sh
#
#  ¿QUÉ HACE ESTE SCRIPT?
#
#    1. Comprueba que Java está instalado
#    2. Compila el proyecto con Maven (sin necesidad de instalarlo)
#    3. Arranca un servidor Tomcat embebido en el puerto 8080
#    4. Despliega nuestra API REST automáticamente
#
#  Para PARAR el servidor: pulsa Ctrl + C en esta terminal
#
# ============================================================

clear

echo "╔══════════════════════════════════════════════════════════╗"
echo "║          UserApp — Spring Boot + JPA + H2               ║"
echo "║          Proyecto de ejemplo — DAW / DAM                ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# ── Paso 1: Comprobar que estamos en la carpeta correcta ──
cd "$(dirname "$0")" || exit 1

if [ ! -f "pom.xml" ]; then
    echo "❌ ERROR: No se encuentra el archivo pom.xml"
    echo "   Asegúrate de ejecutar este script desde la carpeta del proyecto."
    exit 1
fi

echo "📁 Carpeta del proyecto: $(pwd)"
echo ""

# ── Paso 2: Comprobar que Java está instalado ──
echo "🔍 Comprobando Java..."

if ! command -v java &> /dev/null; then
    echo ""
    echo "❌ ERROR: Java no está instalado o no está en el PATH."
    echo ""
    echo "   Instala Java 17 o superior:"
    echo "     • macOS:  brew install openjdk@17"
    echo "     • Linux:  sudo apt install openjdk-17-jdk"
    echo ""
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "   ✅ $JAVA_VERSION"
echo ""

# ── Paso 3: Comprobar que el Maven Wrapper existe ──
if [ ! -f "./mvnw" ]; then
    echo "❌ ERROR: No se encuentra ./mvnw (Maven Wrapper)"
    echo "   El proyecto necesita el archivo mvnw para compilar."
    exit 1
fi

chmod +x ./mvnw

# ── Paso 4: Arrancar Spring Boot ──
echo "🚀 Arrancando el servidor Spring Boot..."
echo ""
echo "   Esto puede tardar unos segundos la primera vez"
echo "   (Maven descarga las dependencias necesarias)."
echo ""
echo "┌──────────────────────────────────────────────────────────┐"
echo "│  Cuando veas 'Started UserappApplication', abre:        │"
echo "│                                                          │"
echo "│    👉  http://localhost:8080                             │"
echo "│                                                          │"
echo "│  Para PARAR el servidor: pulsa Ctrl + C                 │"
echo "└──────────────────────────────────────────────────────────┘"
echo ""

./mvnw spring-boot:run
