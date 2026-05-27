# UserApp — Spring Boot + JPA + H2 `v1.0`

Proyecto de ejemplo para el alumnado de **1º DAM / DAW**.
Una API REST con CRUD completo de **usuarios**, **notas** y **etiquetas** usando Spring Boot, JPA y una base de datos H2 en memoria.

Incluye ejemplos progresivos de relaciones JPA:
- `@ManyToOne` / `@OneToMany` entre `Nota` y `User`
- `@ManyToMany` entre `Nota` y `Etiqueta`
- Métodos derivados y consultas `@Query` JPQL en los repositorios

---

## Ramas del proyecto

Este repositorio tiene varias ramas. Cada una añade una capa de seguridad diferente sobre esta base:

| Rama | Seguridad | Descripcion |
|---|---|---|
| `main` (esta) | Ninguna — API abierta | Base completa v1.0 |
| `v1.5-spring-security` | HTTP Basic Auth | Usuario + contraseña en cada peticion de escritura |
| `v1.5-api-key` | Clave en cabecera | Header `X-API-KEY` en cada peticion de escritura |
| `v1.5-jwt` | JSON Web Token | Login → token → peticiones autenticadas |

Para cambiar de rama:

```bash
git checkout v1.5-spring-security
git checkout v1.5-api-key
git checkout v1.5-jwt
git checkout main
```

Para ver exactamente que se anade en cada rama respecto a v1.0:

```bash
git diff main...v1.5-spring-security
git diff main...v1.5-api-key
git diff main...v1.5-jwt
```

---

## Requisitos previos

- **Java 17** o superior
- No necesitas instalar Maven (el proyecto incluye Maven Wrapper)

```bash
java -version
```

---

## Arrancar el servidor

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

Cuando veas `Started UserappApplication` en la consola, abre:

> **http://localhost:8080**

Para parar: `Ctrl + C`

---

## Que incluye la aplicacion

| URL | Descripcion |
|---|---|
| `/` | Pagina de inicio con enlaces a todo |
| `/users.html` | CRUD de usuarios |
| `/notas.html` | CRUD de notas — muestra `@ManyToOne` en accion |
| `/etiquetas.html` | CRUD de etiquetas — muestra `@ManyToMany` en accion |
| `/h2-console` | Consola SQL de la base de datos H2 |
| `/h2-info.html` | Instrucciones para la consola H2 |
| `/actuator-info.html` | Endpoints de Actuator |
| `/health-info.html` | Health check en tiempo real |

---

## API REST — Endpoints

> En esta rama (`main`) **todos los endpoints son publicos**. No se necesita autenticacion.

### Usuarios — `/api/v1/users`

| Metodo | URL | Descripcion | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/users` | Listar todos los usuarios | `200 OK` |
| `GET` | `/api/v1/users/{id}` | Obtener usuario por ID | `200` / `404` |
| `GET` | `/api/v1/users/buscar?email=` | Buscar por email | `200` / `404` |
| `POST` | `/api/v1/users` | Crear usuario | `201 Created` |
| `PUT` | `/api/v1/users/{id}` | Actualizar usuario | `200` / `404` |
| `DELETE` | `/api/v1/users/{id}` | Eliminar usuario | `204` / `404` |

### Notas — `/api/v1/notas`

| Metodo | URL | Descripcion | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/notas` | Listar todas las notas | `200 OK` |
| `GET` | `/api/v1/notas/{id}` | Obtener nota por ID | `200` / `404` |
| `GET` | `/api/v1/notas/usuario/{id}` | Notas de un usuario | `200 OK` |
| `GET` | `/api/v1/notas/buscar?titulo=&usuarioId=&sortBy=&order=` | Busqueda con filtros y ordenacion | `200 OK` |
| `GET` | `/api/v1/notas/buscar-usuario?nombre=` | Notas por nombre de usuario (`@Query` JPQL) | `200 OK` |
| `GET` | `/api/v1/notas/count/usuario/{id}` | Contar notas de un usuario (`@Query` JPQL) | `200 OK` |
| `POST` | `/api/v1/notas` | Crear nota | `201 Created` |
| `PUT` | `/api/v1/notas/{id}` | Actualizar nota | `200` / `404` |
| `DELETE` | `/api/v1/notas/{id}` | Eliminar nota | `204` / `404` |

### Etiquetas — `/api/v1/etiquetas`

| Metodo | URL | Descripcion | Respuesta |
|---|---|---|---|
| `GET` | `/api/v1/etiquetas` | Listar todas las etiquetas | `200 OK` |
| `GET` | `/api/v1/etiquetas/{id}` | Obtener etiqueta por ID | `200` / `404` |
| `GET` | `/api/v1/etiquetas/buscar?nombre=` | Buscar por nombre | `200` / `404` |
| `POST` | `/api/v1/etiquetas` | Crear etiqueta | `201 Created` |
| `DELETE` | `/api/v1/etiquetas/{id}` | Eliminar etiqueta | `204` / `404` |

---

## Ejemplos con curl

```bash
# Crear un usuario
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Ana Garcia", "email": "ana@ejemplo.com"}'

# Crear una nota asignada al usuario id=1
curl -X POST http://localhost:8080/api/v1/notas \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Apuntes JPA", "contenido": "...", "usuario": {"id": 1}}'

# Buscar notas por titulo, filtrar por usuario y ordenar
curl "http://localhost:8080/api/v1/notas/buscar?titulo=apuntes&usuarioId=1&sortBy=titulo&order=asc"

# Contar notas del usuario id=1 (usa @Query JPQL)
curl http://localhost:8080/api/v1/notas/count/usuario/1

# Buscar notas por nombre del usuario (usa @Query JPQL con JOIN automatico)
curl "http://localhost:8080/api/v1/notas/buscar-usuario?nombre=Ana"

# Crear una etiqueta
curl -X POST http://localhost:8080/api/v1/etiquetas \
  -H "Content-Type: application/json" \
  -d '{"nombre": "importante"}'

# Crear nota con etiquetas
curl -X POST http://localhost:8080/api/v1/notas \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Con etiquetas", "contenido": "...", "usuario": {"id": 1}, "etiquetas": [{"id": 1}]}'
```

---

## Estructura del proyecto

```
src/main/java/com/damw/userapp/
├── controller/
│   ├── UserController.java     ← Endpoints HTTP de usuarios
│   ├── NotaController.java     ← Endpoints HTTP de notas + busqueda + @Query
│   └── EtiquetaController.java ← Endpoints HTTP de etiquetas
├── service/
│   ├── UserService.java        ← Logica de negocio de usuarios
│   ├── NotaService.java        ← Logica de negocio de notas
│   └── EtiquetaService.java    ← Logica de negocio de etiquetas (@Transactional en delete)
├── repository/
│   ├── UserRepository.java     ← findByEmail
│   ├── NotaRepository.java     ← metodos derivados + dos @Query JPQL
│   └── EtiquetaRepository.java ← findByNombre
├── model/
│   ├── User.java               ← Entidad JPA — tabla USERS (@OneToMany → Nota)
│   ├── Nota.java               ← Entidad JPA — tabla NOTAS (@ManyToOne → User, @ManyToMany → Etiqueta)
│   └── Etiqueta.java           ← Entidad JPA — tabla ETIQUETAS (@ManyToMany → Nota)
├── config/
│   └── H2ConsoleConfig.java    ← Registro manual del servlet H2 (Spring Boot 4.x)
└── UserappApplication.java
```

### Arquitectura por capas

```
Controller  →  Service  →  Repository  →  H2 Database
  (HTTP)       (logica)     (JPA/SQL)      (en memoria)
```

---

## Relaciones JPA

### @ManyToOne / @OneToMany — Nota y User

Una `Nota` pertenece a un `User`. Se modela con:

```
USERS                    NOTAS
┌────────────────┐       ┌──────────────────────────────┐
│ id  (PK)       │◄──┐   │ id           (PK)             │
│ nombre         │   └───│ usuario_id   (FK → USERS.id)  │
│ email          │       │ titulo                        │
└────────────────┘       │ contenido                     │
                         └──────────────────────────────┘
```

- `@ManyToOne` en `Nota.usuario` → crea la columna FK `usuario_id` en tabla NOTAS
- `@OneToMany(mappedBy="usuario")` en `User.notas` → lado inverso, sin FK adicional
- `@JsonIgnore` en `User.notas` → evita recursion infinita al serializar a JSON
- `@ToString.Exclude` en `User.notas` → evita recursion en `toString()` de Lombok

### @ManyToMany — Nota y Etiqueta

Una `Nota` puede tener varias `Etiqueta`, y una `Etiqueta` puede estar en varias `Nota`:

```
NOTAS             NOTA_ETIQUETA       ETIQUETAS
┌──────────┐      ┌────────────────┐  ┌──────────────┐
│ id (PK)  │◄─────│ nota_id (FK)   │  │ id (PK)      │
│ titulo   │      │ etiqueta_id (FK)│──►│ nombre       │
│ ...      │      └────────────────┘  └──────────────┘
└──────────┘
```

- `@ManyToMany` + `@JoinTable` en `Nota.etiquetas` → lado propietario (crea la tabla intermedia)
- `@ManyToMany(mappedBy="etiquetas")` en `Etiqueta.notas` → lado inverso
- `@JsonIgnore` en `Etiqueta.notas` → evita recursion infinita al serializar

### @Query JPQL en NotaRepository

Dos consultas que no son expresables con metodos derivados:

```java
// Cuenta las notas de un usuario navegando la relacion @ManyToOne
@Query("SELECT COUNT(n) FROM Nota n WHERE n.usuario.id = :usuarioId")
Long contarNotasPorUsuario(@Param("usuarioId") Long usuarioId);

// Busca notas por el nombre del usuario — Hibernate genera el JOIN automaticamente
@Query("SELECT n FROM Nota n WHERE n.usuario.nombre LIKE %:nombre%")
List<Nota> findByNombreUsuario(@Param("nombre") String nombre);
```

JPQL usa **nombres de clases Java** (`Nota`, `User`) y **nombres de campos** (`n.usuario.nombre`),
no nombres de tablas SQL. Hibernate traduce a SQL automaticamente.

---

## Conexion a la consola H2

Accede a `/h2-console` y usa estos datos:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:userappdb` |
| User | `sa` |
| Password | *(dejar vacio)* |

> La base de datos es **en memoria**: los datos se pierden al parar el servidor.

Tablas disponibles: `USERS`, `NOTAS`, `ETIQUETAS`, `NOTA_ETIQUETA` (tabla intermedia del @ManyToMany)

---

## Stack tecnico

| Tecnologia | Detalle |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.5 |
| Base de datos | H2 en memoria (`jdbc:h2:mem:userappdb`) |
| ORM | Spring Data JPA / Hibernate |
| Lombok | Reduce codigo repetitivo (`@Data`, `@Builder`, etc.) |
| Actuator | Monitorizacion (`/actuator/health`, `/actuator/info`, `/actuator/metrics`) |
| Build | Maven (via Maven Wrapper — no necesitas instalarlo) |

---

## Objetivo pedagogico

Este proyecto demuestra que:

1. **JPA abstrae la base de datos** — el codigo Java no cambia entre H2 y MySQL
2. **Las relaciones JPA** se traducen directamente a claves foraneas y tablas intermedias en SQL
3. **La arquitectura por capas** tiene sentido practico, no es solo teoria
4. **Los metodos derivados** permiten escribir consultas SQL sin SQL
5. **`@Query` JPQL** permite consultas complejas que los metodos derivados no pueden expresar

Cuando se migre a MySQL, **solo cambiara `application.properties`**. El resto del codigo Java permanece intacto.
