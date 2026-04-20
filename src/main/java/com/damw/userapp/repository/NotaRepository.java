package com.damw.userapp.repository;

import com.damw.userapp.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Spring Data JPA genera automáticamente la implementación de esta interfaz.
// Solo con declararla ya tenemos todos los métodos CRUD disponibles.
public interface NotaRepository extends JpaRepository<Nota, Long> {

    // Método derivado: Spring JPA traduce el nombre del método a una consulta SQL.
    // findBy + UsuarioId → SELECT * FROM notas WHERE usuario_id = ?
    List<Nota> findByUsuarioId(Long usuarioId);
}
