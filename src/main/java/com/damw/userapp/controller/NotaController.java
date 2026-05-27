package com.damw.userapp.controller;

import com.damw.userapp.model.Nota;
import com.damw.userapp.service.NotaService;
import org.springframework.data.domain.Sort;
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

    // GET /api/v1/notas/buscar
    // Todos los parámetros son opcionales — sin parámetros devuelve todas las notas.
    //
    // Parámetros disponibles:
    //   ?titulo=apuntes    → filtra notas cuyo título contenga "apuntes" (sin importar mayúsculas)
    //   ?usuarioId=1       → filtra notas del usuario con id=1
    //   ?sortBy=titulo     → ordena por el campo indicado (por defecto: id)
    //   ?order=desc        → dirección de ordenación: asc o desc (por defecto: asc)
    //
    // IMPORTANTE: este mapping va ANTES de /{id} para que Spring no intente
    // parsear la cadena "buscar" como un Long y falle con error 400.
    @GetMapping("/buscar")
    public ResponseEntity<List<Nota>> buscar(
            // required = false → si no se envía el parámetro, su valor es null (no falla)
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long usuarioId,
            // defaultValue → si no se envía, usa este valor en lugar de null
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        // ── Paso 1: convertir el String "asc"/"desc" al enum Sort.Direction ──
        // Sort.Direction es un enum de Spring Data con dos valores: ASC y DESC.
        // No podemos pasar el String directamente a Sort.by() — necesitamos el enum.
        // equalsIgnoreCase acepta "ASC", "asc", "Asc"... para que el alumnado no
        // tenga que preocuparse por mayúsculas al probar la URL en el navegador.
        Sort.Direction dir = order.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC   // ORDER BY ... DESC
                : Sort.Direction.ASC;   // ORDER BY ... ASC  (cualquier otro valor)

        // ── Paso 2: construir el objeto Sort ──
        // Sort.by(dirección, campo) crea un objeto que representa "ORDER BY campo dirección".
        // Ejemplos de lo que generará Hibernate según los parámetros:
        //   sortBy="id",     dir=ASC  → ORDER BY id ASC    (orden de inserción, por defecto)
        //   sortBy="id",     dir=DESC → ORDER BY id DESC   (el más reciente primero)
        //   sortBy="titulo", dir=ASC  → ORDER BY titulo ASC  (alfabético A→Z)
        //   sortBy="titulo", dir=DESC → ORDER BY titulo DESC (alfabético Z→A)
        //
        // ¿Por qué usar Sort en lugar de concatenar SQL?
        //   → Spring Data valida el campo contra la entidad y construye el SQL de forma segura.
        //   → Concatenar strings SQL directamente sería vulnerable a SQL injection.
        Sort sort = Sort.by(dir, sortBy);

        return ResponseEntity.ok(notaService.buscar(titulo, usuarioId, sort));
    }

    // GET /api/v1/notas/count/usuario/{id}
    // Devuelve cuántas notas tiene un usuario. Usa la consulta @Query del repositorio.
    // Ejemplo: GET /api/v1/notas/count/usuario/1 → 3
    //
    // IMPORTANTE: este mapping va ANTES de /{id} para que Spring no confunda
    // la palabra "count" con un Long al intentar parsear el path.
    @GetMapping("/count/usuario/{id}")
    public ResponseEntity<Long> countByUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(notaService.contarNotasPorUsuario(id));
    }

    // GET /api/v1/notas/buscar-usuario?nombre=Ana
    // Devuelve las notas cuyo usuario tenga el texto indicado en su nombre.
    // Usa @Query JPQL con LIKE — Hibernate genera el JOIN con USERS automáticamente.
    @GetMapping("/buscar-usuario")
    public ResponseEntity<List<Nota>> buscarPorNombreUsuario(@RequestParam String nombre) {
        return ResponseEntity.ok(notaService.buscarPorNombreUsuario(nombre));
    }

    // GET /api/v1/notas/{id} → busca una nota por ID, devuelve 404 si no existe
    @GetMapping("/{id}")
    public ResponseEntity<Nota> getById(@PathVariable Long id) {
        // El servicio devuelve Optional<Nota>. Lo procesamos con dos pasos:
        // .map(ResponseEntity::ok)                   → si tiene valor: crea 200 OK con el objeto
        // .orElse(ResponseEntity.notFound().build()) → si está vacío: devuelve 404
        // Nunca llamamos a .get() directamente — lanzaría excepción si el Optional está vacío.
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
