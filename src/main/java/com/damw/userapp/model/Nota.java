package com.damw.userapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    //     Long id;                         ┌─────────────────────────────┐
    //     String titulo;                   │ ID  │ TITULO │ USUARIO_ID  │
    //     String contenido;                │  1  │ "Apun" │      3      │──┐
    //     User usuario;   ←── objeto       │  2  │ "Ejer" │      3      │──┤
    //   }                                  └─────────────────────────────┘  │
    //                                                                        │
    //                                      tabla USERS                       │
    //                                      ┌────────────────────┐           │
    //                                      │ ID  │ NOMBRE        │           │
    //                                      │  3  │ "Ana"         │◄──────────┘
    //                                      └────────────────────┘
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
    @ToString.Exclude // evita recursión infinita en el toString() de Lombok: Nota → User → Nota → ...
    private User usuario;
}
