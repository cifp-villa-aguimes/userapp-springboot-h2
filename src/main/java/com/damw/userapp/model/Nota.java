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

    // @ManyToOne indica que MUCHAS notas pueden pertenecer a UN solo usuario.
    // Esta anotación es la que crea la clave foránea (FK) en la tabla NOTAS.
    // @JoinColumn define el nombre de esa columna FK en la BD: USUARIO_ID
    @ManyToOne
    @JoinColumn(name = "usuario_id") // columna FK que apunta a la tabla USERS
    @ToString.Exclude // evita recursión infinita en el toString() de Lombok: Nota → User → Nota → ...
    private User usuario;
}
