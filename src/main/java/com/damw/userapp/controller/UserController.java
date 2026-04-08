package com.damw.userapp.controller;

import com.damw.userapp.model.User;
import com.damw.userapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @RestController = @Controller + @ResponseBody → todas las respuestas se serializan a JSON
// @RequestMapping define el prefijo de ruta para todos los endpoints de este controlador
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // Inyección por constructor — Spring inyecta automáticamente el UserService
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/v1/users → devuelve la lista completa de usuarios
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> usuarios = userService.listAll();
        return ResponseEntity.ok(usuarios);
    }

    // GET /api/v1/users/{id} → busca un usuario por ID, devuelve 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/v1/users → crea un nuevo usuario con los datos del body JSON
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        User guardado = userService.save(user);
        return ResponseEntity.status(201).body(guardado);
    }

    // PUT /api/v1/users/{id} → actualiza un usuario existente, devuelve 404 si no existe
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/v1/users/{id} → elimina un usuario, devuelve 204 si OK o 404 si no existe
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (userService.delete(id)) {
            return ResponseEntity.noContent().build();  // 204 No Content
        }
        return ResponseEntity.notFound().build();       // 404 Not Found
    }
}
