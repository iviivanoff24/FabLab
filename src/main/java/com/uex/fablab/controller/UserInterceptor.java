package com.uex.fablab.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor para rutas que requieren simplemente un usuario autenticado (no necesariamente admin).
 * Protege páginas de reserva y cualquier futuro contenido bajo /user/.
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher matcher = new AntPathMatcher();

    private static final List<String> USER_PATTERNS = List.of(
            "/reservar/**",
            "/machines/*/reserve/**",
            "/machines/*/reserve",
            "/user/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        boolean requiresUser = USER_PATTERNS.stream().anyMatch(p -> matcher.match(p, path));
        if (!requiresUser) {
            return true; // no protegido
        }
        Object userId = request.getSession(false) != null ? request.getSession(false).getAttribute("USER_ID") : null;
        if (userId != null) {
            return true; // autenticado
        }
        String msg = URLEncoder.encode("Debes iniciar sesión", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?error=" + msg);
        return false;
    }
}
