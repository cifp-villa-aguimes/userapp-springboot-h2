package com.damw.userapp.service;

import com.damw.userapp.model.Etiqueta;
import com.damw.userapp.model.Nota;
import com.damw.userapp.repository.EtiquetaRepository;
import com.damw.userapp.repository.NotaRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// @Service marca esta clase como un componente de lógica de negocio.
// Spring la detecta automáticamente y la registra en el contexto.
@Service
public class NotaService {

    private final NotaRepository notaRepository;
    private final EtiquetaRepository etiquetaRepository;

    // Inyección de dependencias por constructor (buena práctica recomendada).
    // Se inyectan los dos repositorios necesarios: notas y etiquetas.
    // EtiquetaRepository es necesario para resolver las etiquetas que llegan
    // del frontend como objetos "detached" (con solo el id) a proxies gestionados por JPA.
    public NotaService(NotaRepository notaRepository, EtiquetaRepository etiquetaRepository) {
        this.notaRepository = notaRepository;
        this.etiquetaRepository = etiquetaRepository;
    }

    // Devuelve todas las notas de la base de datos
    public List<Nota> listAll() {
        return notaRepository.findAll();
    }

    // Busca una nota por su ID. Devuelve Optional para manejar el caso de "no encontrada"
    public Optional<Nota> findById(Long id) {
        return notaRepository.findById(id);
    }

    // Devuelve todas las notas que pertenecen a un usuario concreto
    public List<Nota> findByUsuarioId(Long usuarioId) {
        return notaRepository.findByUsuarioId(usuarioId);
    }

    // Guarda una nueva nota en la base de datos.
    // ¿Por qué resolvemos las etiquetas antes de guardar?
    //   Cuando el frontend envía { "etiquetas": [{"id": 1}, {"id": 2}] }, Jackson
    //   crea objetos Etiqueta con solo el id relleno. Esos objetos son "detached":
    //   no están gestionados por la sesión JPA activa. Si intentamos guardar la nota
    //   directamente, Hibernate lanza TransientPropertyValueException.
    //   La solución es usar getReferenceById(id), que devuelve un proxy gestionado
    //   por JPA sin emitir ningún SELECT (es eficiente y resuelve el problema).
    public Nota save(Nota nota) {
        nota.setEtiquetas(resolverEtiquetas(nota.getEtiquetas()));
        return notaRepository.save(nota);
    }

    // Actualiza una nota existente. Si no existe, devuelve Optional vacío.
    // Copia todos los campos modificables: titulo, contenido, usuario y etiquetas.
    // Las etiquetas se resuelven igual que en save() para evitar el mismo problema
    // con entidades detached.
    public Optional<Nota> update(Long id, Nota datosActualizados) {
        return notaRepository.findById(id)
                .map(notaExistente -> {
                    notaExistente.setTitulo(datosActualizados.getTitulo());
                    notaExistente.setContenido(datosActualizados.getContenido());
                    notaExistente.setUsuario(datosActualizados.getUsuario());
                    notaExistente.setEtiquetas(resolverEtiquetas(datosActualizados.getEtiquetas()));
                    return notaRepository.save(notaExistente);
                });
    }

    // Elimina una nota por su ID. Devuelve true si existía, false si no
    public boolean delete(Long id) {
        if (notaRepository.existsById(id)) {
            notaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Método privado: convierte Set<Etiqueta> detached → Set<Etiqueta> reales
    // -------------------------------------------------------------------------
    // Cuando el frontend envía {"etiquetas":[{"id":1}]}, Jackson crea objetos
    // Etiqueta con solo el id. Son "detached" (no gestionados por JPA).
    //
    // Usamos findById(id) en lugar de getReferenceById(id) por dos razones:
    //   1. findById emite un SELECT y devuelve una entidad REAL con todos sus campos.
    //   2. getReferenceById devuelve un proxy de Hibernate que Jackson serializa
    //      incluyendo el campo interno "hibernateLazyInitializer" en el JSON.
    //
    // Si el id no existe, lanzamos excepción con mensaje claro.
    // -------------------------------------------------------------------------
    private Set<Etiqueta> resolverEtiquetas(Set<Etiqueta> etiquetasDetached) {
        if (etiquetasDetached == null || etiquetasDetached.isEmpty()) {
            return new HashSet<>();
        }
        return etiquetasDetached.stream()
                .map(e -> etiquetaRepository.findById(e.getId())
                        .orElseThrow(() -> new NoSuchElementException(
                                "Etiqueta no encontrada con id: " + e.getId())))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
