package com.damw.userapp.controller;

import com.damw.userapp.model.Etiqueta;
import com.damw.userapp.service.EtiquetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @RestController = @Controller + @ResponseBody → todas las respuestas se serializan a JSON
// @RequestMapping define el prefijo de ruta para todos los endpoints de este controlador
@RestController
@RequestMapping("/api/v1/etiquetas")
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    // Inyección por constructor — Spring inyecta automáticamente el EtiquetaService
    public EtiquetaController(EtiquetaService etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    // GET /api/v1/etiquetas → devuelve la lista completa de etiquetas (200 OK)
    @GetMapping
    public ResponseEntity<List<Etiqueta>> getAll() {
        List<Etiqueta> etiquetas = etiquetaService.listAll();
        return ResponseEntity.ok(etiquetas);
    }

    // GET /api/v1/etiquetas/{id} → busca una etiqueta por ID, devuelve 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<Etiqueta> getById(@PathVariable Long id) {
        return etiquetaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/v1/etiquetas → crea una nueva etiqueta con los datos del body JSON (201 Created)
    @PostMapping
    public ResponseEntity<Etiqueta> create(@RequestBody Etiqueta etiqueta) {
        Etiqueta guardada = etiquetaService.save(etiqueta);
        return ResponseEntity.status(201).body(guardada);
    }

    // DELETE /api/v1/etiquetas/{id} → elimina una etiqueta
    // Devuelve 204 No Content si se eliminó correctamente, 404 Not Found si no existía.
    // Las filas de la tabla NOTA_ETIQUETA que apuntaban a esta etiqueta se borran automáticamente.
    // No incluimos PUT porque las etiquetas solo tienen nombre y no tiene sentido actualizarlas
    // en este ejemplo didáctico.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (etiquetaService.delete(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build();      // 404 Not Found
    }
}
