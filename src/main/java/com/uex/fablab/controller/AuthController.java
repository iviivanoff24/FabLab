package com.uex.fablab.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/login", "/login.html"})
    public ResponseEntity<Resource> loginPage() {
        Resource resource = new ClassPathResource("templates/login.html");
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("email") String email,
                          @RequestParam("password") String password,
                          HttpSession session) {
        User user = userService.findByEmail(email);
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USER_NAME", user.getName());
            session.setAttribute("USER_EMAIL", user.getEmail());
            session.setAttribute("USER_ADMIN", user.isAdmin());
            return "redirect:/";
        }
        return "redirect:/login?error=1";
    }

    @GetMapping({"/register", "/register.html"})
    public ResponseEntity<Resource> registerPage() {
        Resource resource = new ClassPathResource("templates/register.html");
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam(value = "telefono", required = false) String telefono) {
        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            if (telefono != null && !telefono.isBlank()) user.setTelefono(telefono);
            // por defecto rol USER en @PrePersist
            userService.create(user);
            return "redirect:/login?registered=1";
        } catch (IllegalArgumentException ex) {
            return "redirect:/register?error=1";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/api/session/me")
    @ResponseBody
    public java.util.Map<String, Object> sessionMe(HttpSession session) {
        Object id = session.getAttribute("USER_ID");
        boolean logged = id != null;
        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("logged", logged);
        if (logged) {
            out.put("id", id);
            out.put("name", session.getAttribute("USER_NAME"));
            out.put("email", session.getAttribute("USER_EMAIL"));
            out.put("admin", session.getAttribute("USER_ADMIN"));
        }
        return out;
    }
}
