package com.uex.fablab.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Resource> index() {
        Resource resource = new ClassPathResource("com/uex/fablab/xml/index.html");
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
