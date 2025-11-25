package com.uex.fablab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de la página de inicio.
 */
@Controller
public class HomeController {

    /**
     * Renderiza la página principal del sitio.
     * @return recurso HTML de la home
     */
    @GetMapping("/")
    public String index() {
        return "index"; // será resuelta por Thymeleaf (templates/index.html)
    }
}
