package com.uex.fablab.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.uex.fablab.model.User;
import com.uex.fablab.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<User> all() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        if (repo.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        User saved = repo.save(user);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User input) {
        return repo.findById(id)
                .map(u -> {
                    u.setName(input.getName());
                    u.setEmail(input.getEmail());
                    // Si se incluye password en la actualización, se actualiza
                    if (input.getPassword() != null && !input.getPassword().isBlank()) {
                        u.setPassword(input.getPassword());
                    }
                    return ResponseEntity.ok(repo.save(u));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
