package com.damw.userapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
@Table(name = "etiquetas") // Nombre explícito de la tabla en la base de datos
@Data // Lombok: genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Lombok: constructor vacío (obligatorio para JPA)
@AllArgsConstructor // Lombok: constructor con todos los campos
@Builder // Lombok: patrón builder para crear objetos de forma legible
public class Etiqueta {

    @Id // Este campo es la clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento gestionado por la BD
    private Long id;

    private String nombre;

    // -------------------------------------------------------------------------
    // @ManyToMany(mappedBy = "etiquetas") — lado INVERSO de la relación
    // -------------------------------------------------------------------------
    // Una etiqueta puede estar en MUCHAS notas, y una nota puede tener MUCHAS
    // etiquetas. Eso es una relación @ManyToMany bidireccional.
    //
    // En una relación @ManyToMany bidireccional uno de los dos lados es el DUEÑO
    // y el otro es el INVERSO:
    //
    //   DUEÑO (Nota.java):    tiene @JoinTable → define la tabla intermedia
    //   INVERSO (aquí):       tiene mappedBy   → solo "lee" lo que el dueño define
    //
    // mappedBy = "etiquetas"
    //   Le dice a JPA: "la definición real de esta relación (la tabla intermedia
    //   NOTA_ETIQUETA) vive en el campo que se llama 'etiquetas' dentro de Nota.java".
    //   Sin mappedBy, JPA crearía UNA SEGUNDA tabla intermedia innecesaria.
    //
    //   Nota.java                              Etiqueta.java
    //   ┌──────────────────────────────┐       ┌──────────────────────────────────┐
    //   │ @ManyToMany                  │       │ @ManyToMany(mappedBy="etiquetas")│
    //   │ @JoinTable(name="nota_etiq..)│       │                                  │
    //   │ Set<Etiqueta> etiquetas      │──────►│ Set<Nota> notas                  │
    //   └──────────────────────────────┘       └──────────────────────────────────┘
    //         ▲ dueño: controla la tabla            ▲ inverso: solo navega
    //           intermedia NOTA_ETIQUETA               la relación
    //
    // Regla nemotécnica:
    //   mappedBy = nombre del campo en la OTRA clase (la dueña).
    //   En Nota.java el campo se llama "etiquetas" → mappedBy = "etiquetas".
    //
    // ¿Por qué @JsonIgnore aquí y NO en Nota.java?
    //   Al serializar una Nota a JSON queremos ver sus etiquetas:
    //     { "id": 1, "titulo": "Apuntes", "etiquetas": [{"id":1,"nombre":"Java"}] }
    //   Pero si al serializar Etiqueta también incluyéramos sus notas, el resultado
    //   sería una recursión infinita: Nota → Etiqueta → Nota → Etiqueta → ...
    //   @JsonIgnore en el lado inverso (Etiqueta.notas) corta ese bucle.
    //
    // ¿Por qué Set en vez de List?
    //   - Set garantiza unicidad: una nota no puede tener la misma etiqueta dos veces.
    //   - Con @ManyToMany y List, Hibernate genera DELETE + INSERT de todas las filas
    //     de la tabla intermedia al hacer cualquier cambio ("Bag semantics").
    //     Con Set, Hibernate solo inserta o borra las filas estrictamente necesarias.
    //   - Set es el tipo recomendado por las guías de Hibernate para @ManyToMany.
    // -------------------------------------------------------------------------
    @ManyToMany(mappedBy = "etiquetas")
    @JsonIgnore
    @ToString.Exclude        // evita StackOverflowError en toString(): Etiqueta → Nota → Etiqueta → ...
    @EqualsAndHashCode.Exclude // evita recursión infinita en hashCode(): Etiqueta.notas → Nota.etiquetas → Etiqueta.notas → ...
    @Builder.Default          // necesario para que @Builder de Lombok respete la inicialización con new HashSet<>()
    private Set<Nota> notas = new HashSet<>();
}
