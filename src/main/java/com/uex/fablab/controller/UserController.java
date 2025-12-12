package com.uex.fablab.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador de vistas para usuarios (perfil).
 */
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/profile","/profile.html"})
    public String profile(HttpSession session, Model model) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) {
            return "redirect:/login";
        }
        Long id;
        try {
            if (idObj instanceof Number) id = ((Number) idObj).longValue(); else id = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            session.invalidate();
            return "redirect:/login";
        }

        Optional<User> u = userService.findById(id);
        if (u.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        model.addAttribute("user", u.get());
        return "user/profile";
    }

    // ---- API endpoints (prefixed with /users) ----
    @GetMapping("/users")
    @ResponseBody
    public List<User> all() {
        return userService.listAll();
    }

    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        try {
            User saved = userService.create(user);
            return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<User> one(@PathVariable Long id) {
        return userService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User input) {
        return userService.update(id, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
