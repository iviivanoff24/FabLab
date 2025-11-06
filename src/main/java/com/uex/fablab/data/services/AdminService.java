package com.uex.fablab.data.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.UserRepository;

@Service
@Transactional
public class AdminService {
    private final UserRepository repo;

    public AdminService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> listAllUsers() {
        return repo.findAll();
    }

    public Optional<User> changeRole(Long id, boolean admin) {
        return repo.findById(id).map(u -> {
            u.setAdmin(admin);
            return repo.save(u);
        });
    }

    public boolean deleteByAdmin(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
