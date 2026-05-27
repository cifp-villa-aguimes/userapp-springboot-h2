package com.damw.userapp.service;

import com.damw.userapp.model.Etiqueta;
import com.damw.userapp.repository.EtiquetaRepository;
import com.damw.userapp.repository.NotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

// @Service marca esta clase como un componente de lógica de negocio.
// Spring la detecta automáticamente y la registra en el contexto.
@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;
    private final NotaRepository notaRepository;

    // Inyección de dependencias por constructor (buena práctica recomendada).
    // NotaRepository es necesario para limpiar la tabla NOTA_ETIQUETA antes de
    // borrar una etiqueta (ver explicación en el método delete).
    public EtiquetaService(EtiquetaRepository etiquetaRepository, NotaRepository notaRepository) {
        this.etiquetaRepository = etiquetaRepository;
        this.notaRepository = notaRepository;
    }

    // Devuelve todas las etiquetas de la base de datos
    public List<Etiqueta> listAll() {
        return etiquetaRepository.findAll();
    }

    // Optional<Etiqueta> indica que puede no existir ninguna etiqueta con ese ID.
    // Es el patrón estándar de Spring Data JPA para búsquedas por clave primaria.
    public Optional<Etiqueta> findById(Long id) {
        return etiquetaRepository.findById(id);
    }

    // Busca una etiqueta por su nombre. Útil para comprobar duplicados.
    public Optional<Etiqueta> findByNombre(String nombre) {
        return etiquetaRepository.findByNombre(nombre);
    }

    // Guarda una nueva etiqueta en la base de datos
    public Etiqueta save(Etiqueta etiqueta) {
        return etiquetaRepository.save(etiqueta);
    }

    // Elimina una etiqueta por su ID. Devuelve true si existía, false si no.
    //
    // ¿Por qué necesitamos @Transactional y desvinculación manual?
    //
    // En @ManyToMany bidireccional solo el lado DUEÑO (Nota, con @JoinTable)
    // controla la tabla intermedia NOTA_ETIQUETA. El lado INVERSO (Etiqueta,
    // con mappedBy) no tiene esa responsabilidad.
    //
    // Si llamamos a etiquetaRepository.deleteById(id) directamente y la etiqueta
    // está en uso, H2 lanza una excepción de integridad referencial (FK violation)
    // porque quedarían filas huérfanas en NOTA_ETIQUETA apuntando a una etiqueta
    // que ya no existe.
    //
    // Solución: antes de borrar la etiqueta, recorremos todas las notas que la
    // contienen y la quitamos de su Set<Etiqueta>. Al guardar cada Nota (lado
    // dueño), Hibernate emite el DELETE de las filas correspondientes en
    // NOTA_ETIQUETA. Después ya podemos borrar la Etiqueta sin violar ninguna FK.
    //
    // @Transactional es obligatorio aquí porque etiqueta.getNotas() es una
    // colección LAZY: sin transacción activa, acceder a ella lanzaría
    // LazyInitializationException. Con @Transactional toda la operación ocurre
    // dentro de la misma sesión JPA.
    @Transactional
    public boolean delete(Long id) {
        return etiquetaRepository.findById(id).map(etiqueta -> {
            // Copiamos el Set para evitar ConcurrentModificationException al iterar
            // y modificar a la vez la colección etiqueta.getNotas()
            new HashSet<>(etiqueta.getNotas()).forEach(nota -> {
                nota.getEtiquetas().remove(etiqueta); // actualiza el lado dueño
                notaRepository.save(nota);            // Hibernate emite DELETE en NOTA_ETIQUETA
            });
            etiquetaRepository.delete(etiqueta);
            return true;
        }).orElse(false);
    }
}
