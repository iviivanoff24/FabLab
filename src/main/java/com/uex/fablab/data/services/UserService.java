package com.uex.fablab.data.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.UserRepository;

/**
 * Servicio de usuarios.
 * Gestión CRUD, búsqueda por email y lógica de creación con fecha por defecto.
 */
@Service
@Transactional
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    /** Lista todos los usuarios. */
    public List<User> listAll() {
        return repo.findAll();
    }

    /** Busca usuario por id. */
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    /**
     * Crea un usuario.
     * Establece fecha de registro por defecto y valida unicidad de email.
     */
    public User create(User user) {
        if (repo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (user.getFechaRegistro() == null) {
            user.setFechaRegistro(LocalDate.now());
        }
        return repo.save(user);
    }

    /** Actualiza datos de un usuario existente. */
    public Optional<User> update(Long id, User input) {
        return repo.findById(id).map(u -> {
            u.setName(input.getName());
            u.setEmail(input.getEmail());
            if (input.getPassword() != null && !input.getPassword().isBlank()) {
                u.setPassword(input.getPassword());
            }
            if (input.getTelefono() != null) {
                u.setTelefono(input.getTelefono());
            }
            return repo.save(u);
        });
    }

    /** Elimina un usuario por id. */
    public boolean delete(Long id) {
        return repo.findById(id).map(u -> {
            // Borrar con entidad cargada para aplicar cascadas JPA
            repo.delete(u);
            return true;
        }).orElse(false);
    }

    /** Busca usuario por email. */
    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
