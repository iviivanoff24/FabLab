package com.uex.fablab.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = "classpath:/templates/";
        registry.addResourceHandler("/css/**").addResourceLocations(basePath + "css/");
        registry.addResourceHandler("/js/**").addResourceLocations(basePath + "js/");
        registry.addResourceHandler("/img/**").addResourceLocations(basePath + "img/");
        // fallback para otros recursos estáticos directamente en xml/
        registry.addResourceHandler("/xml/**").addResourceLocations(basePath);

        // Permitir servir las páginas HTML directamente desde la raíz, p.ej. /machines.html
        registry.addResourceHandler("/*.html").addResourceLocations(basePath);
        // Y también soportar subrutas si en el futuro hay carpetas
        //registry.addResourceHandler("/**/*.html").addResourceLocations(basePath);
    }
}
