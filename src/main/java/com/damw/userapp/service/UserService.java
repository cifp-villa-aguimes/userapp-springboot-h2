package com.damw.userapp.service;

import com.damw.userapp.model.User;
import com.damw.userapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// @Service marca esta clase como un componente de lógica de negocio.
// Spring la detecta automáticamente y la registra en el contexto.
@Service
public class UserService {

    private final UserRepository userRepository;

    // Inyección de dependencias por constructor (buena práctica recomendada)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Devuelve todos los usuarios de la base de datos
    public List<User> listAll() {
        return userRepository.findAll();
    }

    // Busca un usuario por su ID. Devuelve Optional para manejar el caso de "no encontrado"
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Guarda un nuevo usuario en la base de datos
    public User save(User user) {
        return userRepository.save(user);
    }

    // Actualiza un usuario existente. Si no existe, devuelve Optional vacío
    public Optional<User> update(Long id, User datosActualizados) {
        return userRepository.findById(id)
                .map(usuarioExistente -> {
                    usuarioExistente.setNombre(datosActualizados.getNombre());
                    usuarioExistente.setEmail(datosActualizados.getEmail());
                    return userRepository.save(usuarioExistente);
                });
    }

    // Elimina un usuario por su ID. Devuelve true si existía, false si no
    public boolean delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
