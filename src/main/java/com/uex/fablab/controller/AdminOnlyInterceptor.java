package com.uex.fablab.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminOnlyInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher matcher = new AntPathMatcher();

    // Ajusta aquí los patrones que quieres restringir a ADMIN
    // Por defecto protegemos APIs sensibles y rutas bajo /admin/**
    private static final List<String> ADMIN_PATTERNS = List.of(
            "/admin/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        boolean requiresAdmin = ADMIN_PATTERNS.stream().anyMatch(p -> matcher.match(p, path));
        if (!requiresAdmin) {
            return true;
        }

        Object adminAttr = request.getSession(false) != null ? request.getSession(false).getAttribute("USER_ADMIN") : null;
        boolean isAdmin = adminAttr instanceof Boolean && ((Boolean) adminAttr);
        if (isAdmin) {
            return true;
        }

        String msg = URLEncoder.encode("Acceso restringido para administradores", StandardCharsets.UTF_8);
        // Redirige a login si no autenticado o sin rol adecuado
        response.sendRedirect(request.getContextPath() + "/login?error=" + msg);
        return false;
    }
}
