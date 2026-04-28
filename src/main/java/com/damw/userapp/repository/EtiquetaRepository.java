package com.damw.userapp.repository;

import com.damw.userapp.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data JPA genera automáticamente la implementación de esta interfaz.
// Solo con declararla ya tenemos: findAll, findById, save, deleteById, existsById, etc.
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    // Método derivado: Spring JPA traduce el nombre del método a SQL automáticamente.
    // findBy + Nombre → SELECT * FROM etiquetas WHERE nombre = ?
    // Útil para comprobar si ya existe una etiqueta con ese nombre antes de crear otra.
    Optional<Etiqueta> findByNombre(String nombre);
}
