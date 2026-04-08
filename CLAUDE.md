# CLAUDE.md — Proyecto userapp

## Quién soy

Soy profesor de **1º DAM y DAW** en un ciclo formativo de desarrollo de aplicaciones.
Este proyecto es un **ejemplo didáctico** que uso en clase para enseñar Spring Boot, JPA y H2.

---

## ¿Qué es este proyecto?

Una API REST en Spring Boot 4.x con H2 en memoria que implementa un CRUD de usuarios.
Sirve como ejemplo progresivo para el alumnado: primero con H2 (sin instalar nada),
después evolucionará a MySQL para ver cómo JPA abstrae la base de datos.

El proyecto incluye páginas HTML estáticas en `src/main/resources/static/` que sirven
como dashboard visual para el alumnado:

- `index.html` → página de inicio con enlaces
- `h2-info.html` → instrucciones para conectarse a la consola H2
- `actuator-info.html` → visualización de endpoints de Actuator
- `health-info.html` → health check en tiempo real

---

## Stack técnico

| Tecnología    | Versión / Detalle                       |
| ------------- | --------------------------------------- |
| Java          | 17                                      |
| Spring Boot   | 4.x                                     |
| Base de datos | H2 en memoria (`jdbc:h2:mem:userappdb`) |
| ORM           | Spring Data JPA / Hibernate             |
| Lombok        | Sí, para reducir boilerplate            |
| Actuator      | Habilitado (health, info, metrics)      |
| Build         | Maven                                   |

---

## Estructura del proyecto

```
src/main/java/com/damw/userapp/
├── config/              ← configuraciones Spring (solo si es estrictamente necesario)
├── controller/          ← @RestController, endpoints HTTP
├── model/               ← @Entity, clases JPA
├── repository/          ← interfaces JpaRepository
├── service/             ← @Service, lógica de negocio
└── UserappApplication.java

src/main/resources/
├── static/              ← HTML estático (NO tocar sin permiso explícito)
│   ├── index.html
│   ├── h2-info.html
│   ├── actuator-info.html
│   └── health-info.html
└── application.properties
```

---

## Configuración actual — application.properties

```properties
spring.application.name=userapp

# H2 en memoria
spring.datasource.url=jdbc:h2:mem:userappdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Consola H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

---

## Reglas de código — MUY IMPORTANTE

### Estilo general

- Código **limpio, simple y fácil de leer** para alumnado de primer curso
- **Sin abstracciones innecesarias** — nada de interfaces de servicio, DTOs complejos ni patrones avanzados
- **Comentarios en español** explicando qué hace cada anotación importante
- Inyección **siempre por constructor**, nunca con `@Autowired` en campo
- Usar **Lombok** en las entidades: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

### Arquitectura por capas — respetar siempre

```
Controller → Service → Repository → Entity → H2
```

- El Controller **nunca** accede directamente al Repository
- El Service **nunca** devuelve ResponseEntity (eso es responsabilidad del Controller)
- Cada capa tiene **una sola responsabilidad**

### Manejo de errores

- Usar `ResponseEntity` en los controllers con códigos HTTP correctos
- `Optional` + `.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build())` para los GET por ID
- DELETE devuelve `204 No Content`

### Ejemplo de entidad correcto

```java
@Entity
@Table(name = "users")  // nombre explícito de la tabla en la BD
@Data                   // Lombok: genera getters, setters, toString, equals
@NoArgsConstructor      // Lombok: constructor vacío (obligatorio para JPA)
@AllArgsConstructor     // Lombok: constructor con todos los campos
@Builder                // Lombok: patrón builder para crear objetos
public class User {

    @Id                 // este campo es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincremento
    private Long id;

    private String nombre;
    private String email;
}
```

---

## Lo que NO debe tocar Claude Code sin permiso explícito

- Los ficheros HTML en `src/main/resources/static/` — están revisados y aprobados
- El diseño visual de las páginas HTML — coherente con el estilo del Campus Moodle
- La versión de Spring Boot en el pom.xml — estamos en 4.x intencionadamente
- El nombre de la base de datos `userappdb` — referenciado en h2-info.html

---

## Nota importante — H2 Console en Spring Boot 4

En Spring Boot 4 la consola H2 **NO se auto-configura** con solo
`spring.h2.console.enabled=true` cuando se usa `spring-boot-starter-webmvc`.
La auto-configuración de H2 se movió a un módulo separado.

**SOLUCIÓN CORRECTA** — añadir al `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-h2console</artifactId>
</dependency>
```

Con esto el `H2ConsoleConfig.java` **NO es necesario** y debe eliminarse.
Sin esta dependencia, la consola H2 devuelve 404 aunque la propiedad esté habilitada.

---

## Objetivo pedagógico

Este proyecto demuestra al alumnado que:

1. JPA abstrae la base de datos — el código Java no cambia entre H2 y MySQL
2. La arquitectura por capas tiene sentido práctico, no es solo teoría
3. Spring Boot reduce enormemente el código necesario para una API funcional

Cuando se migre a MySQL, **solo cambiará el `application.properties`**.
El resto del código Java permanece intacto. Eso es lo que queremos que vean.
