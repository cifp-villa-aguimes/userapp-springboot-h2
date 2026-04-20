package com.damw.userapp.controller;

import com.damw.userapp.model.Nota;
import com.damw.userapp.service.NotaService;
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
@RequestMapping("/api/v1/notas")
public class NotaController {

    private final NotaService notaService;

    // Inyección por constructor — Spring inyecta automáticamente el NotaService
    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    // GET /api/v1/notas → devuelve la lista completa de notas
    @GetMapping
    public ResponseEntity<List<Nota>> getAll() {
        List<Nota> notas = notaService.listAll();
        return ResponseEntity.ok(notas);
    }

    // GET /api/v1/notas/usuario/{id} → devuelve las notas de un usuario concreto
    // IMPORTANTE: este mapping va antes de /{id} para que Spring no confunda "usuario" con un Long
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Nota>> getByUsuario(@PathVariable Long id) {
        List<Nota> notas = notaService.findByUsuarioId(id);
        return ResponseEntity.ok(notas);
    }

    // GET /api/v1/notas/{id} → busca una nota por ID, devuelve 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<Nota> getById(@PathVariable Long id) {
        return notaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/v1/notas → crea una nueva nota con los datos del body JSON
    @PostMapping
    public ResponseEntity<Nota> create(@RequestBody Nota nota) {
        Nota guardada = notaService.save(nota);
        return ResponseEntity.status(201).body(guardada);
    }

    // PUT /api/v1/notas/{id} → actualiza una nota existente, devuelve 404 si no existe
    @PutMapping("/{id}")
    public ResponseEntity<Nota> update(@PathVariable Long id, @RequestBody Nota nota) {
        return notaService.update(id, nota)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/v1/notas/{id} → elimina una nota, devuelve 204 si OK o 404 si no existe
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (notaService.delete(id)) {
            return ResponseEntity.noContent().build();  // 204 No Content
        }
        return ResponseEntity.notFound().build();       // 404 Not Found
    }
}
