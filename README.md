# UserApp — Spring Boot + JPA + H2

Proyecto de ejemplo para el alumnado de **1º DAM / DAW**.
Una API REST con CRUD de usuarios usando Spring Boot, JPA y una base de datos H2 en memoria.

---

## Requisitos previos

- **Java 17** o superior instalado
- No necesitas instalar Maven (el proyecto incluye Maven Wrapper)

Comprueba tu version de Java:

```bash
java -version
```

---

## Arrancar el servidor

### macOS / Linux

```bash
./iniciar.sh
```

### Windows

Haz doble clic en `iniciar.bat`, o desde la terminal:

```cmd
iniciar.bat
```

### Alternativa manual

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

Una vez arrancado, veras en la consola el mensaje `Started UserappApplication`.
Abre el navegador en:

> **http://localhost:8080**

Para parar el servidor pulsa `Ctrl + C` en la terminal.

---

## Que incluye la aplicacion

| URL | Descripcion |
|---|---|
| `/` | Pagina de inicio con enlaces a todo |
| `/users.html` | Interfaz web para gestionar usuarios (CRUD) |
| `/h2-info.html` | Instrucciones para la consola H2 |
| `/h2-console` | Consola SQL de la base de datos H2 |
| `/actuator-info.html` | Visualizacion de endpoints de Actuator |
| `/health-info.html` | Health check en tiempo real |

---

## API REST — Endpoints

Base URL: `/api/v1/users`

| Metodo | URL | Descripcion | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/users` | Listar todos los usuarios | `200 OK` |
| `GET` | `/api/v1/users/{id}` | Obtener un usuario por ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/v1/users` | Crear un nuevo usuario | `201 Created` |
| `PUT` | `/api/v1/users/{id}` | Actualizar un usuario | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/v1/users/{id}` | Eliminar un usuario | `204 No Content` / `404 Not Found` |

### Ejemplo — Crear un usuario con curl

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'
```

---

## Estructura del proyecto

```
src/main/java/com/damw/userapp/
├── controller/          ← Endpoints HTTP (@RestController)
├── service/             ← Logica de negocio (@Service)
├── repository/          ← Acceso a datos (JpaRepository)
├── model/               ← Entidad JPA (User)
└── UserappApplication.java

src/main/resources/
├── static/              ← Paginas HTML del dashboard
└── application.properties
```

### Arquitectura por capas

```
Controller  →  Service  →  Repository  →  H2 Database
  (HTTP)       (logica)     (JPA/SQL)      (en memoria)
```

Cada capa tiene una unica responsabilidad. El Controller nunca accede directamente al Repository.

---

## Stack tecnico

| Tecnologia | Detalle |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.5 |
| Base de datos | H2 en memoria (`jdbc:h2:mem:userappdb`) |
| ORM | Spring Data JPA / Hibernate |
| Lombok | Reduce codigo repetitivo (getters, setters, etc.) |
| Actuator | Monitorizacion (health, info, metrics) |
| Build | Maven (via Maven Wrapper) |

---

## Conexion a la consola H2

Desde la pagina `/h2-info.html` o directamente en `/h2-console`:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:userappdb` |
| User | `sa` |
| Password | *(vacio)* |

> La base de datos es **en memoria**: los datos se pierden al parar el servidor.

---

## Objetivo pedagogico

Este proyecto demuestra que:

1. **JPA abstrae la base de datos** — el codigo Java no cambia entre H2 y MySQL
2. **La arquitectura por capas** tiene sentido practico, no es solo teoria
3. **Spring Boot** reduce enormemente el codigo necesario para una API funcional

Cuando se migre a MySQL, solo cambiara `application.properties`. El resto del codigo Java permanece intacto.
