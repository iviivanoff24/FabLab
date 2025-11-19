package com.uex.fablab.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MachinesController {

    private static final String MARKER = "<!-- ADMIN_ONLY_MARKER -->";

    private static final String ADMIN_CARD = "" +
            "<div class=\"card text-bg-warning mb-3 mx-auto\" style=\"max-width: 18rem; max-height: 10rem;\">" +
            "  <a href=\"admin/add-machine.html\" class=\"text-decoration-none text-dark\">" +
            "    <div class=\"card-body d-flex flex-column align-items-center justify-content-center py-3\">" +
            "      <img src=\"img/add.png\" alt=\"Añadir máquina\" width=\"48\" height=\"48\" class=\"mb-2\" />" +
            "      <h5 class=\"card-title text-dark m-0\">Nueva Máquina</h5>" +
            "    </div>" +
            "  </a>" +
            "</div>";

    @GetMapping({"/machines", "/machines.html"})
    public ResponseEntity<String> machines(HttpSession session) throws java.io.IOException {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        var resource = new ClassPathResource("templates/machines.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (isAdmin) {
            html = html.replace(MARKER, ADMIN_CARD);
        } else {
            html = html.replace(MARKER, "");
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
