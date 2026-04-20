package com.damw.userapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity // Marca esta clase como una entidad JPA (se mapea a una tabla en la BD)
@Table(name = "users") // Nombre explícito de la tabla en la base de datos
@Data // Lombok: genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Lombok: constructor vacío (obligatorio para JPA)
@AllArgsConstructor // Lombok: constructor con todos los campos
@Builder // Lombok: patrón builder para crear objetos de forma legible
public class User {

    @Id // Este campo es la clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento gestionado por la BD
    private Long id;

    private String nombre;

    private String email;

    // -------------------------------------------------------------------------
    // @OneToMany — "UN usuario puede tener MUCHAS notas"
    // -------------------------------------------------------------------------
    // Esta anotación define el lado "uno" de la relación. Es la relación inversa
    // de @ManyToOne en Nota.java — las dos juntas forman la relación bidireccional.
    //
    // IMPORTANTE: @OneToMany NO crea ninguna columna nueva en la tabla USERS.
    // La FK (USUARIO_ID) sigue viviendo en la tabla NOTAS, igual que antes.
    // Este campo solo existe en Java para poder navegar la relación en código:
    //
    //   Sin @OneToMany:   user.getNotas()  →  ✘ no existe, tendrías que ir al repo
    //   Con @OneToMany:   user.getNotas()  →  ✔ JPA carga la lista automáticamente
    //
    // mappedBy = "usuario"
    //   Le dice a JPA: "la FK no está aquí, está en el campo 'usuario' de Nota".
    //   Sin mappedBy, JPA crearía una tabla intermedia innecesaria (USER_NOTAS).
    //
    //   Nota.java                        User.java
    //   ┌─────────────────────┐          ┌──────────────────────────────┐
    //   │ @ManyToOne          │          │ @OneToMany(mappedBy="usuario")│
    //   │ @JoinColumn(        │          │                              │
    //   │   name="usuario_id")│          │  ← no crea columna en USERS  │
    //   │ private User usuario│◄─────────│ private List<Nota> notas     │
    //   └─────────────────────┘          └──────────────────────────────┘
    //         ▲ dueño de la FK                    ▲ lado inverso
    //
    // Regla para recordarlo:
    //   mappedBy apunta al nombre del campo en la clase del otro lado (@ManyToOne).
    //   En Nota.java el campo se llama "usuario" → mappedBy = "usuario".
    //
    // @JsonIgnore evita la recursión infinita al serializar a JSON: User → Nota → User → ...
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    // -------------------------------------------------------------------------
    // @ToString.Exclude — anotación de Lombok
    // -------------------------------------------------------------------------
    // @Data genera automáticamente un método toString() que imprime TODOS los
    // campos de la clase. Sin esta anotación, al imprimir un User ocurriría:
    //
    //   User.toString()
    //     └─► imprime el campo "notas" → llama a Nota.toString()
    //           └─► imprime el campo "usuario" → llama a User.toString()
    //                 └─► imprime el campo "notas" → llama a Nota.toString()
    //                       └─► ... ∞  StackOverflowError
    //
    // Con @ToString.Exclude le decimos a Lombok:
    //   "excluye el campo 'notas' del toString() generado"
    //
    // Resultado — toString() generado CON esta anotación:
    //   User(id=1, nombre=Ana, email=ana@mail.com)   ✔ seguro
    //
    // Resultado — toString() generado SIN esta anotación:
    //   User(id=1, nombre=Ana, email=ana@mail.com, notas=[Nota(id=1, titulo=...,
    //   usuario=User(id=1, nombre=Ana, notas=[Nota(id=1, ...   ← bucle infinito ✘
    // -------------------------------------------------------------------------
    @ToString.Exclude
    private List<Nota> notas = new ArrayList<>();
}

/*
 * ============================================================================
 * EQUIVALENTE SIN LOMBOK — así sería esta misma clase sin las anotaciones
 * de Lombok. Todo este código lo genera Lombok automáticamente en tiempo
 * de compilación. Fíjate en la diferencia de líneas: ~28 con Lombok vs ~120 sin
 * él.
 * ============================================================================
 *
 * @Entity
 * 
 * @Table(name = "users")
 * public class User {
 *
 * @Id
 * 
 * @GeneratedValue(strategy = GenerationType.IDENTITY)
 * private Long id;
 * private String nombre;
 * private String email;
 *
 * // --- Constructor vacío (equivale a @NoArgsConstructor) ---
 * // JPA lo necesita obligatoriamente para crear instancias por reflexión
 * public User() {
 * }
 *
 * // --- Constructor con todos los campos (equivale a @AllArgsConstructor) ---
 * public User(Long id, String nombre, String email) {
 * this.id = id;
 * this.nombre = nombre;
 * this.email = email;
 * }
 *
 * // --- Getters (equivale a parte de @Data) ---
 * public Long getId() {
 * return id;
 * }
 *
 * public String getNombre() {
 * return nombre;
 * }
 *
 * public String getEmail() {
 * return email;
 * }
 *
 * // --- Setters (equivale a parte de @Data) ---
 * public void setId(Long id) {
 * this.id = id;
 * }
 *
 * public void setNombre(String nombre) {
 * this.nombre = nombre;
 * }
 *
 * public void setEmail(String email) {
 * this.email = email;
 * }
 *
 * // --- equals() (equivale a parte de @Data) ---
 * // Compara dos objetos User por el valor de todos sus campos
 * 
 * @Override
 * public boolean equals(Object o) {
 * if (this == o) return true;
 * if (o == null || getClass() != o.getClass()) return false;
 * User user = (User) o;
 * return Objects.equals(id, user.id)
 * && Objects.equals(nombre, user.nombre)
 * && Objects.equals(email, user.email);
 * }
 *
 * // --- hashCode() (equivale a parte de @Data) ---
 * // Genera un código hash basado en todos los campos
 * 
 * @Override
 * public int hashCode() {
 * return Objects.hash(id, nombre, email);
 * }
 *
 * // --- toString() (equivale a parte de @Data) ---
 * // Representación en texto del objeto, útil para depuración
 * 
 * @Override
 * public String toString() {
 * return "User{" +
 * "id=" + id +
 * ", nombre='" + nombre + '\'' +
 * ", email='" + email + '\'' +
 * '}';
 * }
 *
 * // --- Builder (equivale a @Builder) ---
 * // Permite crear objetos así:
 * User.builder().nombre("Ana").email("ana@mail.com").build()
 * public static UserBuilder builder() {
 * return new UserBuilder();
 * }
 *
 * public static class UserBuilder {
 * private Long id;
 * private String nombre;
 * private String email;
 *
 * public UserBuilder id(Long id) {
 * this.id = id;
 * return this;
 * }
 *
 * public UserBuilder nombre(String nombre) {
 * this.nombre = nombre;
 * return this;
 * }
 *
 * public UserBuilder email(String email) {
 * this.email = email;
 * return this;
 * }
 *
 * public User build() {
 * return new User(id, nombre, email);
 * }
 * }
 * }
 */
