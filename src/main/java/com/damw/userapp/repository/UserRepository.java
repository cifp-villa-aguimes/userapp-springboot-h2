package com.damw.userapp.repository;

import com.damw.userapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository nos da gratis: findAll, findById, save, deleteById, etc.
// Solo necesitamos declarar métodos extra si queremos consultas personalizadas.
public interface UserRepository extends JpaRepository<User, Long> {

    // Método derivado: Spring Data genera la consulta SQL automáticamente
    // a partir del nombre del método → SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
}
