package com.uex.fablab.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.UserService;

import jakarta.validation.Valid;

/**
 * API REST de usuarios.
 * Permite listar, crear, consultar, actualizar y eliminar usuarios.
 */
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lista todos los usuarios.
     * @return lista de usuarios
     */
    @GetMapping
    public List<User> all() {
        return userService.listAll();
    }

    /**
     * Crea un nuevo usuario.
     * @param user datos del usuario
     * @return usuario creado o error de validación
     */
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        try {
            User saved = userService.create(user);
            return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Recupera un usuario por id.
     * @param id identificador
     * @return usuario o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> one(@PathVariable Long id) {
        return userService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un usuario por id.
     * @param id identificador
     * @param input datos a actualizar
     * @return usuario actualizado o 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User input) {
        return userService.update(id, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un usuario por id.
     * @param id identificador
     * @return 204 si se eliminó o 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
