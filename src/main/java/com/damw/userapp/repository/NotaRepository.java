package com.damw.userapp.repository;

import com.damw.userapp.model.Nota;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Spring Data JPA genera automáticamente la implementación de esta interfaz.
// Solo con declararla ya tenemos todos los métodos CRUD disponibles.
public interface NotaRepository extends JpaRepository<Nota, Long> {

    // Método derivado: Spring JPA traduce el nombre del método a una consulta SQL.
    // findBy + UsuarioId → SELECT * FROM notas WHERE usuario_id = ?
    List<Nota> findByUsuarioId(Long usuarioId);

    // Método derivado: busca notas cuyo título contenga el texto indicado.
    // Containing → LIKE '%valor%' (busca en cualquier posición del texto)
    // IgnoreCase → no distingue entre mayúsculas y minúsculas
    // El parámetro Sort se traduce a ORDER BY — Spring Data lo acepta en cualquier método derivado.
    // Spring Data genera automáticamente:
    //   SELECT * FROM notas WHERE LOWER(titulo) LIKE LOWER('%valor%') ORDER BY ...
    List<Nota> findByTituloContainingIgnoreCase(String titulo, Sort sort);

    // Método derivado combinado: filtra por título Y por usuario a la vez.
    // And combina dos condiciones — ambas deben cumplirse.
    // El parámetro Sort se pasa desde el controlador y se traduce a ORDER BY.
    // Spring Data genera:
    //   SELECT * FROM notas WHERE LOWER(titulo) LIKE '%valor%' AND usuario_id = ? ORDER BY ...
    List<Nota> findByTituloContainingIgnoreCaseAndUsuarioId(
            String titulo, Long usuarioId, Sort sort
    );

    // -------------------------------------------------------------------------
    // @Query — consultas JPQL explícitas
    // -------------------------------------------------------------------------
    // A diferencia de los métodos derivados (donde Spring infiere la consulta del
    // nombre del método), aquí escribimos la consulta JPQL nosotros mismos.
    //
    // JPQL usa nombres de CLASES Java y CAMPOS Java, NO nombres de tablas SQL:
    //   "Nota"         → tabla NOTAS en la base de datos
    //   "n.usuario.id" → columna USUARIO_ID via la relación @ManyToOne
    //
    // :usuarioId es un parámetro con nombre. @Param("usuarioId") lo vincula con
    // el argumento del método. Es más legible que los posicionales (?1, ?2).

    // COUNT devuelve un número entero — mapeamos a Long (nunca int, para evitar overflow).
    // Esta consulta no es expresable con un método derivado: COUNT(*) con navegación de relación.
    @Query("SELECT COUNT(n) FROM Nota n WHERE n.usuario.id = :usuarioId")
    Long contarNotasPorUsuario(@Param("usuarioId") Long usuarioId);

    // LIKE %:nombre% → busca el texto en cualquier posición (equivale a SQL LIKE '%valor%').
    // n.usuario.nombre navega la relación @ManyToOne — Hibernate genera el JOIN automáticamente.
    // Esta consulta tampoco es expresable con un método derivado: JPQL con navegación de relación en LIKE.
    @Query("SELECT n FROM Nota n WHERE n.usuario.nombre LIKE %:nombre%")
    List<Nota> findByNombreUsuario(@Param("nombre") String nombre);
}
