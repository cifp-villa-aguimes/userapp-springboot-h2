package com.damw.userapp.service;

import com.damw.userapp.model.Nota;
import com.damw.userapp.repository.NotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// @Service marca esta clase como un componente de lógica de negocio.
// Spring la detecta automáticamente y la registra en el contexto.
@Service
public class NotaService {

    private final NotaRepository notaRepository;

    // Inyección de dependencias por constructor (buena práctica recomendada)
    public NotaService(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
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

    // Guarda una nueva nota en la base de datos
    public Nota save(Nota nota) {
        return notaRepository.save(nota);
    }

    // Actualiza una nota existente. Si no existe, devuelve Optional vacío
    public Optional<Nota> update(Long id, Nota datosActualizados) {
        return notaRepository.findById(id)
                .map(notaExistente -> {
                    notaExistente.setTitulo(datosActualizados.getTitulo());
                    notaExistente.setContenido(datosActualizados.getContenido());
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
}
