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
 * Interceptor que protege rutas que requieren un usuario autenticado.
 *
 * <p>Se usa para impedir el acceso a páginas de reserva y a contenidos bajo
 * rutas protegidas (p. ej. <code>/user/**</code> o las rutas de reserva de
 * máquinas). Si la petición no pertenece a ninguna de las rutas protegidas
 * se delega normalmente; si pertenece y el usuario no está en sesión, se
 * redirige a la página de login con un mensaje codificado.</p>
 *
 * <p>La comprobación se realiza sobre la URI relativa al <code>contextPath</code>
 * para que el interceptor funcione correctamente cuando la aplicación se
 * despliegue con un prefijo de contexto.</p>
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher matcher = new AntPathMatcher();

    private static final List<String> USER_PATTERNS = List.of(
            "/reservar/**",
            "/machines/*/reserve/**",
            "/machines/*/reserve",
            "/user/**",
            "/profile"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Obtener URI relativa al context path para que los patrones funcionen
        // igual cuando la app tiene prefijo de contexto.
        final String context = request.getContextPath() != null ? request.getContextPath() : "";
        final String uri = request.getRequestURI() != null ? request.getRequestURI() : "";
        final String relative = uri.startsWith(context) ? uri.substring(context.length()) : uri;

        boolean requiresUser = USER_PATTERNS.stream().anyMatch(p -> matcher.match(p, relative));
        if (!requiresUser) {
            return true; // ruta no protegida
        }

        // Evitar crear sesión si no existía
        var session = request.getSession(false);
        Object userId = session != null ? session.getAttribute("USER_ID") : null;
        if (userId != null) {
            return true; // autenticado
        }

        String msg = URLEncoder.encode("Debes iniciar sesión", StandardCharsets.UTF_8);
        // Redirigir incluyendo el context path si existe
        response.sendRedirect(context + "/login?error=" + msg);
        return false;
    }
}
