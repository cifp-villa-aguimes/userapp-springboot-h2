package com.damw.userapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity // Marca esta clase como una entidad JPA (se mapea a una tabla en la BD)
@Table(name = "notas") // Nombre explícito de la tabla en la base de datos
@Data // Lombok: genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Lombok: constructor vacío (obligatorio para JPA)
@AllArgsConstructor // Lombok: constructor con todos los campos
@Builder // Lombok: patrón builder para crear objetos de forma legible
public class Nota {

    @Id // Este campo es la clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento gestionado por la BD
    private Long id;

    private String titulo;

    private String contenido;

    // -------------------------------------------------------------------------
    // @ManyToOne — "MUCHAS notas pertenecen a UN usuario"
    // -------------------------------------------------------------------------
    // Esta anotación define el lado "muchos" de la relación y es la responsable
    // de crear la columna de clave foránea (FK) en la tabla NOTAS.
    //
    // En Java tenemos un objeto:          En la BD se traduce a una columna FK:
    //
    //   Nota {                              tabla NOTAS
    //     Long id;                         ┌────────────────────------───────┐
    //     String titulo;                   │ ID  │ TITULO      │ USUARIO_ID  │
    //     String contenido;                │  1  │ "Apuntes"   │      3      │──┐
    //     User usuario;   ←── objeto       │  2  │ "Ejercicio" │      3      │──┤
    //   }                                  └──────────────────────────-----──┘  │
    //                                                                           │
    //                                      tabla USERS                          │
    //                                      ┌───────────────────-─┐              │
    //                                      │ ID  │ NOMBRE        │              │
    //                                      │  3  │ "Ana"         │◄───────-----─┘
    //                                      └────────────────────-┘
    //
    // @JoinColumn(name = "usuario_id") le dice a JPA cómo llamar a esa columna
    // FK en la base de datos. Sin esta anotación, JPA inventaría un nombre.
    //
    // Regla para recordarlo:
    //   @ManyToOne siempre va en el lado que tiene la FK en la tabla.
    //   Aquí NOTAS tiene USUARIO_ID, por eso @ManyToOne está en Nota, no en User.
    //
    // La relación inversa (@OneToMany) está declarada en User.java.
    // -------------------------------------------------------------------------
    @ManyToOne
    @JoinColumn(name = "usuario_id") // nombre de la columna FK en la tabla NOTAS
    @ToString.Exclude          // evita recursión infinita en toString(): Nota → User → Nota → ...
    @EqualsAndHashCode.Exclude // evita recursión infinita en hashCode(): Nota → User → Nota → ...
    private User usuario;

    // -------------------------------------------------------------------------
    // @ManyToMany — "UNA nota puede tener MUCHAS etiquetas,
    //               y UNA etiqueta puede estar en MUCHAS notas"
    // -------------------------------------------------------------------------
    // Esta es la relación DUEÑA: Nota controla la tabla intermedia.
    // Regla práctica: la clase cuyo formulario HTML incluye la selección múltiple
    // es generalmente la buena candidata para ser el lado dueño.
    //
    // @JoinTable define la tabla intermedia que Hibernate genera automáticamente:
    //
    //   tabla NOTA_ETIQUETA (generada por Hibernate, sin código SQL)
    //   ┌──────────┬─────────────┐
    //   │ NOTA_ID  │ ETIQUETA_ID │
    //   │    1     │      2      │   ← nota 1 tiene etiqueta 2
    //   │    1     │      5      │   ← nota 1 tiene también etiqueta 5
    //   │    3     │      2      │   ← nota 3 también tiene etiqueta 2
    //   └──────────┴─────────────┘
    //
    //   name = "nota_etiqueta"       → nombre de la tabla intermedia en la BD
    //   joinColumns                  → FK que apunta a ESTA tabla (NOTAS → nota_id)
    //   inverseJoinColumns           → FK que apunta a la OTRA tabla (ETIQUETAS → etiqueta_id)
    //
    // No hay cascade: si se elimina una Nota, sus Etiquetas no se borran.
    // Solo se eliminan las filas de la tabla intermedia que las relacionaban.
    //
    // El lado inverso de esta relación está en Etiqueta.java:
    //   @ManyToMany(mappedBy = "etiquetas") Set<Nota> notas
    // -------------------------------------------------------------------------
    @ManyToMany
    @JoinTable(
        name = "nota_etiqueta",
        joinColumns = @JoinColumn(name = "nota_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    @Builder.Default           // necesario para que @Builder de Lombok respete la inicialización con new HashSet<>()
    @EqualsAndHashCode.Exclude // evita recursión infinita en hashCode(): Nota → Etiqueta → Nota → ...
    private Set<Etiqueta> etiquetas = new HashSet<>();
}
