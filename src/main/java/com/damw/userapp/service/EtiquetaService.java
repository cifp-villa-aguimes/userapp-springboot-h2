package com.damw.userapp.service;

import com.damw.userapp.model.Etiqueta;
import com.damw.userapp.repository.EtiquetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// @Service marca esta clase como un componente de lógica de negocio.
// Spring la detecta automáticamente y la registra en el contexto.
@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    // Inyección de dependencias por constructor (buena práctica recomendada)
    public EtiquetaService(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    // Devuelve todas las etiquetas de la base de datos
    public List<Etiqueta> listAll() {
        return etiquetaRepository.findAll();
    }

    // Busca una etiqueta por su ID. Devuelve Optional para manejar el caso de "no encontrada"
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
    // Al eliminar la etiqueta, Hibernate borra automáticamente las filas de
    // la tabla NOTA_ETIQUETA que la referenciaban (limpieza de la tabla intermedia).
    public boolean delete(Long id) {
        if (etiquetaRepository.existsById(id)) {
            etiquetaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
