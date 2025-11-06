package com.uex.fablab.data.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.UserRepository;

@Service
@Transactional
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> listAll() {
        return repo.findAll();
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public User create(User user) {
        if (repo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (user.getFechaRegistro() == null) {
            user.setFechaRegistro(LocalDate.now());
        }
        return repo.save(user);
    }

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

    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
