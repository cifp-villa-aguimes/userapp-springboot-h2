# UserApp — Spring Boot + JPA + H2 `v1.5-jwt`

> **Rama de seguridad** — Implementa autenticacion **JSON Web Token (JWT)** con Spring Security.
> Los GET son publicos. POST, PUT y DELETE requieren un token JWT obtenido en el login.
>
> Base: [`main` (v1.0)](../../tree/main) — ver que se anade en esta rama: `git diff main...v1.5-jwt`

---

## Seguridad — JSON Web Token (JWT)

### Flujo de autenticacion en 3 pasos

```
Paso 1 — Login                 Paso 2 — Guardar token         Paso 3 — Usar el token
──────────────────────         ──────────────────────         ────────────────────────────
POST /auth/login               El cliente almacena            POST /api/v1/notas
{"username":"admin",    ──►    el token recibido      ──►    Authorization: Bearer <token>
 "password":"admin123"}        {"token":"eyJhbGc..."}         → 201 Created
    ◄── {"token":"eyJ..."}
```

### Credenciales de login

| Campo | Valor |
|---|---|
| Endpoint | `POST /auth/login` |
| Body | `{"username": "admin", "password": "admin123"}` |
| Respuesta | `{"token": "eyJhbGciOiJIUzI1NiJ9..."}` |

### Modelo de acceso

| Tipo de peticion | Acceso | Como autenticarse |
|---|---|---|
| `POST /auth/login` | Libre — siempre publico | — (es donde se obtiene el token) |
| `GET /api/v1/**` | Libre — sin token | — |
| `POST /api/v1/**` | Requiere token JWT | `Authorization: Bearer <token>` |
| `PUT /api/v1/**` | Requiere token JWT | `Authorization: Bearer <token>` |
| `DELETE /api/v1/**` | Requiere token JWT | `Authorization: Bearer <token>` |
| `/h2-console/**` | Libre | — |

---

## Como usar la API con JWT

### Con curl — flujo completo paso a paso

```bash
# Paso 1 — Obtener el token haciendo login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
# Token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6...

# Paso 2 — Usar el token en peticiones de escritura
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'

# Paso 3 — Los GET no necesitan token
curl http://localhost:8080/api/v1/users
```

### Con Postman — flujo completo

**1. Login para obtener el token:**

- Metodo: `POST`
- URL: `http://localhost:8080/auth/login`
- Body → raw → JSON:
  ```json
  {"username": "admin", "password": "admin123"}
  ```
- Copiar el valor del campo `token` de la respuesta

**2. Usar el token en peticiones protegidas:**

- Pestaña **Authorization**
- Type → **Bearer Token**
- Pegar el token copiado
- Enviar la peticion

### Que pasa sin token

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test"}'
# → HTTP 401 Unauthorized
```

### Que pasa con token manipulado

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer tokenFalsoQueNoEsValido" \
  -d '{"nombre": "Test"}'
# → HTTP 401 Unauthorized
# La firma HMAC-SHA256 no coincide — el servidor detecta la manipulacion
```

### Cuando caduca el token

El token dura **24 horas** (configurable en `application.properties`). Pasado ese tiempo:

```bash
# → HTTP 401 Unauthorized (ExpiredJwtException en el servidor)
# Solucion: volver a hacer POST /auth/login para obtener un token nuevo
```

---

## Que se anade respecto a v1.0

Esta rama anade **cuatro clases** y una entrada en `pom.xml`:

```
src/main/java/com/damw/userapp/
└── security/
    ├── JwtUtil.java         ← NUEVO — genera y valida tokens JWT
    ├── JwtFilter.java       ← NUEVO — intercepta peticiones y extrae el token
    ├── SecurityConfig.java  ← NUEVO — configura Spring Security (STATELESS)
    └── AuthController.java  ← NUEVO — endpoint POST /auth/login
```

Y en `pom.xml`, cuatro dependencias:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

Para ver el diff completo:

```bash
git diff main...v1.5-jwt
```

---

## Como funciona JWT internamente

### Estructura del token

Un JWT tiene tres partes separadas por puntos:

```
eyJhbGciOiJIUzI1NiJ9  .  eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcw...  .  Xk7abc123...
        │                               │                                  │
    HEADER                           PAYLOAD                          SIGNATURE
  (algoritmo)                       (claims)                      (HMAC-SHA256)
```

- **Header**: algoritmo de firma (`HS256`)
- **Payload**: datos del usuario — `sub` (usuario), `iat` (emitido), `exp` (caduca)
- **Signature**: firma HMAC-SHA256 del header+payload con la clave secreta

El payload es **codificado en Base64, no cifrado** — no incluir datos sensibles (contraseñas, etc.).

### Flujo tecnico

```
POST /auth/login
       │
       ▼
AuthController.login()
  └── authenticationManager.authenticate(usuario, contraseña)
        └── UserDetailsService verifica contra usuarios en memoria
  └── jwtUtil.generarToken(username)
        └── Jwts.builder().subject().issuedAt().expiration().signWith().compact()
  └── {"token": "eyJ..."}
       │
       ▼ (cliente guarda el token)
       │
POST /api/v1/notas  +  Authorization: Bearer eyJ...
       │
       ▼
JwtFilter.doFilterInternal()
  └── Extrae el token del header "Authorization: Bearer ..."
  └── jwtUtil.esValido(token)
        └── Jwts.parser().verifyWith(secretKey).parseSignedClaims(token)
        └── Verifica firma HMAC-SHA256 + fecha de expiracion
  └── Si valido → SecurityContextHolder.setAuthentication(...)
       │
       ▼
SecurityConfig — .anyRequest().authenticated() → PERMITIDO
       │
       ▼
NotaController.create() → 201 Created
```

### Por que STATELESS

En esta rama, Spring Security esta configurado con `SessionCreationPolicy.STATELESS`:

- El servidor **no crea ni guarda sesiones** — sin cookies de sesion
- Cada peticion es **independiente** — el cliente debe enviar el token en cada request
- Esto es el comportamiento correcto para APIs REST consumidas por SPAs o aplicaciones moviles

Comparado con HTTP Basic (rama `v1.5-spring-security`), donde Spring crea una sesion tras el primer login exitoso.

---

## Arrancar el servidor

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

> **http://localhost:8080**

---

## API REST — Endpoints

### Autenticacion — `/auth`

| Metodo | URL | Token | Body | Respuesta |
|---|---|---|---|---|
| `POST` | `/auth/login` | No | `{"username":"...","password":"..."}` | `200` + `{"token":"..."}` / `401` |

### Usuarios — `/api/v1/users`

| Metodo | URL | Token | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/users` | No | `200 OK` |
| `GET` | `/api/v1/users/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/users/buscar?email=` | No | `200` / `404` |
| `POST` | `/api/v1/users` | **Si** — `Bearer <token>` | `201 Created` |
| `PUT` | `/api/v1/users/{id}` | **Si** | `200` / `404` |
| `DELETE` | `/api/v1/users/{id}` | **Si** | `204` / `404` |

### Notas — `/api/v1/notas`

| Metodo | URL | Token | Respuesta |
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

| Metodo | URL | Token | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/etiquetas` | No | `200 OK` |
| `GET` | `/api/v1/etiquetas/{id}` | No | `200` / `404` |
| `GET` | `/api/v1/etiquetas/buscar?nombre=` | No | `200` / `404` |
| `POST` | `/api/v1/etiquetas` | **Si** | `201 Created` |
| `DELETE` | `/api/v1/etiquetas/{id}` | **Si** | `204` / `404` |

---

## Consola H2

Accesible **sin token** en `/h2-console`:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:userappdb` |
| User | `sa` |
| Password | *(dejar vacio)* |

---

## Configuracion JWT en application.properties

```properties
# Clave secreta — minimo 32 caracteres para HS256 (256 bits)
app.jwt.secret=claveSecretaMuyLargaParaHS256DeAlMenos32Chars!

# Expiracion en milisegundos (86400000 = 24 horas)
app.jwt.expiration=86400000
```

> En produccion la clave debe venir de una variable de entorno (`JWT_SECRET`), nunca del fichero de configuracion subido a git.

---

## Comparativa con las otras ramas de seguridad

| | `v1.5-spring-security` | `v1.5-api-key` | `v1.5-jwt` |
|---|---|---|---|
| Mecanismo | HTTP Basic | Filtro personalizado | JSON Web Token |
| Credencial | Usuario + contrasena | Clave fija en cabecera | Token con expiracion |
| Cabecera | `Authorization: Basic ...` | `X-API-KEY: ...` | `Authorization: Bearer ...` |
| Login explícito | No (se envía en cada peticion) | No | Si — `POST /auth/login` |
| Estado | Con sesion (por defecto) | Sin estado | Sin estado (STATELESS) |
| Token caduca | No | No | Si — 24 h por defecto |
| Nuevas clases | 1 | 1 | 4 |
| Dependencias | Spring Security | Ninguna | Spring Security + JJWT |
