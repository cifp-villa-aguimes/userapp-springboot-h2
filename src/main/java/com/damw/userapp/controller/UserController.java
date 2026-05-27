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
import org.springframework.web.bind.annotation.RequestParam;
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

    // GET /api/v1/users/buscar?email=ana@email.com
    // Busca un usuario por email exacto.
    // Devuelve 200 OK con el usuario si existe, o 404 Not Found si no.
    //
    // @RequestParam sin required=false → el parámetro es obligatorio.
    // Si no se envía ?email=..., Spring devuelve automáticamente 400 Bad Request.
    //
    // IMPORTANTE: va ANTES de /{id} para que Spring no intente parsear "buscar" como Long.
    @GetMapping("/buscar")
    public ResponseEntity<User> buscarPorEmail(@RequestParam String email) {
        // findByEmail devuelve Optional<User>:
        //   - Optional con valor → el email existe en la BD
        //   - Optional vacío     → no hay ningún usuario con ese email
        //
        // .map(ResponseEntity::ok)                   → si tiene valor: envuelve el User en 200 OK
        // .orElse(notFound().build())                → si está vacío: devuelve 404 Not Found
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/{id} → busca un usuario por ID, devuelve 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        // El servicio devuelve Optional<User>. Lo procesamos con dos pasos:
        // .map(ResponseEntity::ok)                   → si tiene valor: crea 200 OK con el objeto
        // .orElse(ResponseEntity.notFound().build()) → si está vacío: devuelve 404
        // Nunca llamamos a .get() directamente — lanzaría excepción si el Optional está vacío.
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
