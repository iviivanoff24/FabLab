package com.uex.fablab.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Interceptor de "remember me" que recrea la sesión de usuario si existe una cookie válida.
 * No interrumpe el flujo si la cookie no está presente o es inválida.
 */
@Component
public class RememberMeInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final String secret;

    public RememberMeInterceptor(UserService userService,
                                 @Value("${auth.remember.secret:defaultSecretValue}") String secret) {
        this.userService = userService;
        this.secret = secret;
    }

    /**
     * Antes de atender la petición, intenta autenticar mediante cookie REMEMBER_ME.
     * @param request petición HTTP
     * @param response respuesta HTTP
     * @param handler handler
     * @return siempre true para continuar la cadena
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("USER_ID") != null) {
            return true; // ya autenticado
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return true;
        for (Cookie c : cookies) {
            if ("REMEMBER_ME".equals(c.getName())) {
                String val = c.getValue();
                String[] parts = val.split(":");
                if (parts.length != 3) return true;
                try {
                    Long userId = Long.valueOf(parts[0]);
                    String email = parts[1];
                    String sig = parts[2];
                    String expected = sha256(userId + ":" + email + secret);
                    if (!expected.equals(sig)) return true; // firma inválida
                    User user = userService.findById(userId).orElse(null);
                    if (user == null || !email.equals(user.getEmail())) return true;
                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("USER_ID", user.getId());
                    newSession.setAttribute("USER_NAME", user.getName());
                    newSession.setAttribute("USER_EMAIL", user.getEmail());
                    newSession.setAttribute("USER_ADMIN", user.isAdmin());
                } catch (NumberFormatException ex) {
                    return true;
                }
                return true;
            }
        }
        return true;
    }

    /**
     * Calcula SHA-256 en hexadecimal.
     * @param input texto
     * @return hash hexadecimal
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