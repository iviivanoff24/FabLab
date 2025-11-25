package com.uex.fablab.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controlador de autenticación y registro.
 * Gestiona login, registro, logout y un endpoint para consultar el estado de sesión.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Renderiza la página de inicio de sesión.
     * @return recurso HTML de la página de login
     */
    @GetMapping({"/login", "/login.html"})
    public ResponseEntity<Resource> loginPage() {
        Resource resource = new ClassPathResource("templates/login.html");
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    /**
     * Procesa el formulario de login. Crea atributos de sesión y cookie de "remember me" si se solicita.
     *
     * @param email correo del usuario
     * @param password contraseña
     * @param remember marca para recordar sesión
     * @param session sesión HTTP
     * @param response respuesta HTTP para setear cookies
     * @return redirección a la página principal o de error
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam("email") String email,
                          @RequestParam("password") String password,
                          @RequestParam(value = "remember", required = false) String remember,
                          HttpSession session,
                          HttpServletResponse response) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return redirectLoginError("Usuario no encontrado");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return redirectLoginError("Contraseña no establecida");
        }
        if (!user.getPassword().equals(password)) {
            return redirectLoginError("Contraseña incorrecta");
        }
        // OK
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USER_NAME", user.getName());
        session.setAttribute("USER_EMAIL", user.getEmail());
        session.setAttribute("USER_ADMIN", user.isAdmin());

        if (remember != null) {
            String secret = System.getProperty("auth.remember.secret", "defaultSecretValue");
            String tokenData = user.getId() + ":" + user.getEmail();
            String signature = sha256(tokenData + secret);
            String cookieValue = tokenData + ":" + signature;
            Cookie cookie = new Cookie("REMEMBER_ME", cookieValue);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(30 * 24 * 3600); // 30 días
            response.addCookie(cookie);
        }
        return "redirect:/";
    }

    private String redirectLoginError(String msg) {
        String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:/login?error=" + encoded;
    }

    /**
     * Renderiza la página de registro.
     * @return recurso HTML de la página de registro
     */
    @GetMapping({"/register", "/register.html"})
    public ResponseEntity<Resource> registerPage() {
        Resource resource = new ClassPathResource("templates/register.html");
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    /**
     * Procesa el formulario de registro de usuario.
     * @param name nombre
     * @param email correo
     * @param password contraseña
     * @param telefono teléfono opcional
     * @return redirección a inicio o a error
     */
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
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            return "redirect:/register?error=1";
        }
    }

    /**
     * Cierra la sesión del usuario, borra la cookie de "remember me" y redirige.
     * Intenta volver a la página previa indicada por el header Referer.
     *
     * @param session sesión HTTP
     * @param response respuesta HTTP
     * @param request petición HTTP
     * @return redirección a la página previa o al inicio
     */
    @PostMapping("/logout")
    public String logout(HttpSession session,
                         HttpServletResponse response,
                         jakarta.servlet.http.HttpServletRequest request) {
        if (session != null) {
            session.invalidate();
        }
        Cookie cookie = new Cookie("REMEMBER_ME", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // Intentar volver a la página previa (Referer) de forma segura
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                java.net.URI uri = java.net.URI.create(referer);
                String path = uri.getPath();
                // Evitar redirecciones externas o vacías
                if (path != null && path.startsWith("/") && !path.equals("/logout")) {
                    return "redirect:" + path + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
                }
            } catch (IllegalArgumentException ignored) {
                // Si el referer es inválido, caemos al fallback
            }
        }
        return "redirect:/";
    }

    /**
     * Endpoint JSON con el estado de la sesión actual.
     * @param session sesión HTTP
     * @return mapa con indicadores de login y datos básicos del usuario
     */
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

    /**
     * Calcula el hash SHA-256 para el texto dado.
     * @param input texto de entrada
     * @return representación hexadecimal del hash
     */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
