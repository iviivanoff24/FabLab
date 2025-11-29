package com.uex.fablab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReadmeController {

    @GetMapping("/readme")
    public String readme() {
        return "readme"; // corresponde a src/main/resources/templates/readme.html
    }

}
