package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.UserRepository;

/**
 * Servicio de administración.
 * Operaciones para gestionar usuarios y roles por parte de administradores.
 */
@Service
@Transactional
public class AdminService {
    private final UserRepository repo;

    public AdminService(UserRepository repo) {
        this.repo = repo;
    }

    /** Lista todos los usuarios. */
    public List<User> listAllUsers() {
        return repo.findAll();
    }

    /** Cambia el rol admin de un usuario. */
    public Optional<User> changeRole(Long id, boolean admin) {
        return repo.findById(id).map(u -> {
            u.setAdmin(admin);
            return repo.save(u);
        });
    }

    /** Elimina un usuario directamente por id. */
    public boolean deleteByAdmin(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
