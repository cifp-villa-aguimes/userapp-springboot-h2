# UserApp — Spring Boot + JPA + H2 `v1.5-spring-security`

> **Rama de seguridad** — Implementa autenticacion **HTTP Basic** con Spring Security.
> Los GET son publicos. POST, PUT y DELETE requieren usuario y contrasena.
>
> Base: [`main` (v1.0)](../../tree/main) — ver que se anade en esta rama: `git diff main...v1.5-spring-security`

---

## Seguridad — HTTP Basic Auth

### Modelo de acceso

| Tipo de peticion | Acceso | Como autenticarse |
|---|---|---|
| `GET /api/v1/**` | Libre — sin credenciales | — |
| `POST /api/v1/**` | Requiere autenticacion | Usuario + contrasena |
| `PUT /api/v1/**` | Requiere autenticacion | Usuario + contrasena |
| `DELETE /api/v1/**` | Requiere autenticacion | Usuario + contrasena |
| `/h2-console/**` | Libre | — |
| `/*.html`, `/actuator/**` | Libre | — |

### Credenciales configuradas

| Campo | Valor |
|---|---|
| Usuario | `admin` |
| Contrasena | `admin123` |

> Las credenciales estan en memoria (clase `SecurityConfig`). En una aplicacion real vendrian de la base de datos.

### Como autenticarse

**Con curl:**

```bash
# -u usuario:contrasena — curl codifica las credenciales en Base64 automaticamente
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'
```

**Con Postman:**

1. Abre la peticion POST/PUT/DELETE
2. Pestaña **Authorization**
3. Type → **Basic Auth**
4. Username: `admin` / Password: `admin123`
5. Enviar

**Como funciona por debajo:**

HTTP Basic codifica `usuario:contrasena` en Base64 y lo envía en la cabecera:

```
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

El navegador (o Postman) hace esta codificacion automaticamente. El servidor la decodifica y verifica contra los usuarios registrados.

### Que pasa sin credenciales

```bash
# Sin -u → 401 Unauthorized
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test"}'
# → HTTP 401 Unauthorized
```

### Que pasa con credenciales incorrectas

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -u admin:contrasenaIncorrecta \
  -d '{"nombre": "Test"}'
# → HTTP 401 Unauthorized
```

---

## Que se anade respecto a v1.0

Esta rama anade **exactamente un fichero** sobre el codigo base de v1.0:

```
src/main/java/com/damw/userapp/
└── security/
    └── SecurityConfig.java   ← NUEVO — configuracion de Spring Security
```

Y una dependencia en `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Para ver el diff completo:

```bash
git diff main...v1.5-spring-security
```

### Por que solo un fichero

Spring Security se activa automaticamente al anadir la dependencia. Solo necesitamos `SecurityConfig.java` para:
- Definir que rutas son publicas y cuales requieren autenticacion
- Crear el usuario en memoria (`admin / admin123`)
- Configurar el mecanismo de autenticacion (HTTP Basic)

El resto del codigo (controladores, servicios, repositorios) **no se toca**. La seguridad se anade por encima, no dentro.

---

## Arrancar el servidor

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

> **http://localhost:8080**

---

## API REST — Endpoints

La siguiente tabla incluye el requisito de autenticacion para cada endpoint.

### Usuarios — `/api/v1/users`

| Metodo | URL | Auth | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/users` | No | `200 OK` |
| `GET` | `/api/v1/users/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/users/buscar?email=` | No | `200` / `404` |
| `POST` | `/api/v1/users` | **Si** — `admin:admin123` | `201 Created` |
| `PUT` | `/api/v1/users/{id}` | **Si** — `admin:admin123` | `200` / `404` |
| `DELETE` | `/api/v1/users/{id}` | **Si** — `admin:admin123` | `204` / `404` |

### Notas — `/api/v1/notas`

| Metodo | URL | Auth | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/notas` | No | `200 OK` |
| `GET` | `/api/v1/notas/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/notas/usuario/{id}` | No | `200 OK` |
| `GET` | `/api/v1/notas/buscar?titulo=&usuarioId=&sortBy=&order=` | No | `200 OK` |
| `GET` | `/api/v1/notas/buscar-usuario?nombre=` | No | `200 OK` |
| `GET` | `/api/v1/notas/count/usuario/{id}` | No | `200 OK` |
| `POST` | `/api/v1/notas` | **Si** | `201 Created` |
| `PUT` | `/api/v1/notas/{id}` | **Si** | `200` / `404` |
| `DELETE` | `/api/v1/notas/{id}` | **Si** | `204` / `404` |

### Etiquetas — `/api/v1/etiquetas`

| Metodo | URL | Auth | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/etiquetas` | No | `200 OK` |
| `GET` | `/api/v1/etiquetas/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/etiquetas/buscar?nombre=` | No | `200` / `404` |
| `POST` | `/api/v1/etiquetas` | **Si** | `201 Created` |
| `DELETE` | `/api/v1/etiquetas/{id}` | **Si** | `204` / `404` |

---

## Ejemplos con curl — flujo completo

```bash
# 1. Consultar (GET — sin credenciales)
curl http://localhost:8080/api/v1/users

# 2. Crear usuario (POST — con credenciales)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'

# 3. Crear nota para el usuario id=1 (POST — con credenciales)
curl -X POST http://localhost:8080/api/v1/notas \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"titulo": "Apuntes JPA", "contenido": "...", "usuario": {"id": 1}}'

# 4. Actualizar usuario (PUT — con credenciales)
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{"nombre": "Ana Garcia Lopez", "email": "ana@ejemplo.com"}'

# 5. Eliminar nota (DELETE — con credenciales)
curl -X DELETE http://localhost:8080/api/v1/notas/1 \
  -u admin:admin123
```

---

## Consola H2

Accesible **sin autenticacion** en `/h2-console`:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:userappdb` |
| User | `sa` |
| Password | *(dejar vacio)* |

> Spring Security esta configurado para permitir `/h2-console/**` sin credenciales.
> Ademas, `frameOptions sameOrigin` permite que el iframe de la consola H2 funcione correctamente.

---

## Comparativa con las otras ramas de seguridad

| | `v1.5-spring-security` | `v1.5-api-key` | `v1.5-jwt` |
|---|---|---|---|
| Mecanismo | HTTP Basic | Filtro personalizado | JSON Web Token |
| Credencial | Usuario + contrasena | Clave en cabecera | Token obtenido en login |
| Cabecera | `Authorization: Basic ...` | `X-API-KEY: ...` | `Authorization: Bearer ...` |
| Estado (sesion) | Con sesion (por defecto) | Sin estado | Sin estado (STATELESS) |
| Nuevas clases | 1 (`SecurityConfig`) | 1 (`ApiKeyFilter`) | 4 (`JwtUtil`, `JwtFilter`, `SecurityConfig`, `AuthController`) |
| Dependencias | `spring-boot-starter-security` | Ninguna | `spring-boot-starter-security` + JJWT |
