# UserApp — Spring Boot + JPA + H2 `v1.5-api-key`

> **Rama de seguridad** — Implementa un **filtro API Key personalizado** sin Spring Security.
> Los GET son publicos. POST, PUT y DELETE requieren la cabecera `X-API-KEY`.
>
> Base: [`main` (v1.0)](../../tree/main) — ver que se anade en esta rama: `git diff main...v1.5-api-key`

---

## Seguridad — Filtro API Key

### Modelo de acceso

| Tipo de peticion | Acceso | Como autenticarse |
|---|---|---|
| `GET /api/v1/**` | Libre — sin cabecera | — |
| `POST /api/v1/**` | Requiere API Key | Cabecera `X-API-KEY` |
| `PUT /api/v1/**` | Requiere API Key | Cabecera `X-API-KEY` |
| `DELETE /api/v1/**` | Requiere API Key | Cabecera `X-API-KEY` |
| `/h2-console/**` | Libre (excluido del filtro) | — |

### Clave configurada

| Campo | Valor |
|---|---|
| Propiedad | `app.api-key` en `application.properties` |
| Valor actual | `userapp-api-key-2026` |
| Cabecera HTTP | `X-API-KEY` |

> En produccion la clave debe venir de una variable de entorno, no del fichero de configuracion.

### Como autenticarse

**Con curl:**

```bash
# -H "X-API-KEY: ..." — anadir la cabecera con la clave
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: userapp-api-key-2026" \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'
```

**Con Postman:**

1. Abre la peticion POST/PUT/DELETE
2. Pestaña **Headers**
3. Añadir clave: `X-API-KEY` / valor: `userapp-api-key-2026`
4. Enviar

### Que pasa sin la cabecera

```bash
# Sin X-API-KEY → 401 Unauthorized con mensaje explicativo
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test"}'
# → HTTP 401 Unauthorized
# → "API Key requerida. Añade la cabecera X-API-KEY a tu peticion."
```

### Que pasa con clave incorrecta

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: claveEquivocada" \
  -d '{"nombre": "Test"}'
# → HTTP 401 Unauthorized
```

---

## Que se anade respecto a v1.0

Esta rama anade **exactamente un fichero** sobre el codigo base de v1.0:

```
src/main/java/com/damw/userapp/
└── security/
    └── ApiKeyFilter.java   ← NUEVO — filtro servlet personalizado
```

Y una linea en `application.properties`:

```properties
app.api-key=userapp-api-key-2026
```

**No se anade ninguna dependencia nueva** en `pom.xml`. El filtro usa unicamente las clases de Jakarta Servlet, que ya vienen incluidas en `spring-boot-starter-webmvc`.

Para ver el diff completo:

```bash
git diff main...v1.5-api-key
```

### Por que un filtro propio y no Spring Security

Esta rama demuestra que se puede proteger una API **sin necesidad de un framework de seguridad completo**. Un `OncePerRequestFilter` de Spring Web es suficiente para:

- Interceptar todas las peticiones HTTP
- Leer una cabecera personalizada
- Comparar su valor con la clave configurada
- Rechazar con 401 si no coincide

Es un patron muy comun en microservicios internos o APIs con autenticacion simple entre sistemas.

### Como funciona ApiKeyFilter

```
Peticion HTTP entrante
        │
        ▼
┌────────────────────────────────────────┐
│ ApiKeyFilter.shouldNotFilter()         │
│ ¿La URL empieza por /h2-console?       │◄─── Si → no aplica el filtro
└────────────────────────────────────────┘
        │ No
        ▼
┌────────────────────────────────────────┐
│ ¿Es POST, PUT o DELETE?                │◄─── No (es GET) → dejar pasar
└────────────────────────────────────────┘
        │ Si
        ▼
┌────────────────────────────────────────┐
│ ¿Viene la cabecera X-API-KEY?          │◄─── No → 401 Unauthorized
│ ¿Coincide con app.api-key?             │◄─── No → 401 Unauthorized
└────────────────────────────────────────┘
        │ Si
        ▼
    Controlador
```

---

## Arrancar el servidor

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

> **http://localhost:8080**

---

## API REST — Endpoints

### Usuarios — `/api/v1/users`

| Metodo | URL | API Key | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/users` | No | `200 OK` |
| `GET` | `/api/v1/users/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/users/buscar?email=` | No | `200` / `404` |
| `POST` | `/api/v1/users` | **Si** — `X-API-KEY: userapp-api-key-2026` | `201 Created` |
| `PUT` | `/api/v1/users/{id}` | **Si** | `200` / `404` |
| `DELETE` | `/api/v1/users/{id}` | **Si** | `204` / `404` |

### Notas — `/api/v1/notas`

| Metodo | URL | API Key | Respuesta |
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

| Metodo | URL | API Key | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/etiquetas` | No | `200 OK` |
| `GET` | `/api/v1/etiquetas/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/etiquetas/buscar?nombre=` | No | `200` / `404` |
| `POST` | `/api/v1/etiquetas` | **Si** | `201 Created` |
| `DELETE` | `/api/v1/etiquetas/{id}` | **Si** | `204` / `404` |

---

## Ejemplos con curl — flujo completo

```bash
# 1. Consultar (GET — sin cabecera)
curl http://localhost:8080/api/v1/users

# 2. Crear usuario (POST — con X-API-KEY)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: userapp-api-key-2026" \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'

# 3. Crear nota (POST — con X-API-KEY)
curl -X POST http://localhost:8080/api/v1/notas \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: userapp-api-key-2026" \
  -d '{"titulo": "Apuntes JPA", "contenido": "...", "usuario": {"id": 1}}'

# 4. Actualizar usuario (PUT — con X-API-KEY)
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: userapp-api-key-2026" \
  -d '{"nombre": "Ana Garcia Lopez", "email": "ana@ejemplo.com"}'

# 5. Eliminar nota (DELETE — con X-API-KEY)
curl -X DELETE http://localhost:8080/api/v1/notas/1 \
  -H "X-API-KEY: userapp-api-key-2026"
```

---

## Consola H2

Accesible **sin API Key** en `/h2-console`.

El filtro tiene `shouldNotFilter()` configurado para excluir `/h2-console/**` porque la consola H2 usa peticiones POST internamente y quedaria bloqueada sin esta excepcion.

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:userappdb` |
| User | `sa` |
| Password | *(dejar vacio)* |

---

## Comparativa con las otras ramas de seguridad

| | `v1.5-spring-security` | `v1.5-api-key` | `v1.5-jwt` |
|---|---|---|---|
| Mecanismo | HTTP Basic | Filtro personalizado | JSON Web Token |
| Credencial | Usuario + contrasena | Clave en cabecera | Token obtenido en login |
| Cabecera | `Authorization: Basic ...` | `X-API-KEY: ...` | `Authorization: Bearer ...` |
| Framework | Spring Security | Ninguno (servlet puro) | Spring Security + JJWT |
| Nuevas clases | 1 (`SecurityConfig`) | 1 (`ApiKeyFilter`) | 4 clases |
| Dependencias nuevas | `spring-boot-starter-security` | Ninguna | `spring-boot-starter-security` + JJWT |
