package com.uex.fablab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para mostrar el archivo README.
 */
@Controller
public class ReadmeController {

    /**
     * Muestra la vista del README.
     * @return nombre de la vista readme
     */
    @GetMapping("/readme")
    public String readme() {
        return "readme"; // corresponde a src/main/resources/templates/readme.html
    }

}
