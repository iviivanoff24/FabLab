package com.uex.fablab.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminOnlyInterceptor adminOnlyInterceptor;
    private final UserInterceptor userInterceptor;
    private final RememberMeInterceptor rememberMeInterceptor;

    public WebConfig(AdminOnlyInterceptor adminOnlyInterceptor,
                     UserInterceptor userInterceptor,
                     RememberMeInterceptor rememberMeInterceptor) {
        this.adminOnlyInterceptor = adminOnlyInterceptor;
        this.userInterceptor = userInterceptor;
        this.rememberMeInterceptor = rememberMeInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = "classpath:/templates/";
        registry.addResourceHandler("/css/**").addResourceLocations(basePath + "css/");
        registry.addResourceHandler("/js/**").addResourceLocations(basePath + "js/");
        registry.addResourceHandler("/img/**").addResourceLocations(basePath + "img/");
        // Carpeta de subidas dentro del proyecto: resolvemos ruta absoluta de forma robusta
        Path moduleUpload = Path.of("ProyectoMDAI", "src", "main", "resources", "templates", "img", "upload");
        Path localUpload = Path.of("src", "main", "resources", "templates", "img", "upload");
        Path chosen = Files.exists(moduleUpload) ? moduleUpload : localUpload;
        String fileUrl = "file:" + chosen.toAbsolutePath().toString().replace('\\', '/') + "/";
        registry.addResourceHandler("/img/upload/**")
            .addResourceLocations(fileUrl);
        // fallback para otros recursos estáticos directamente en xml/
        registry.addResourceHandler("/xml/**").addResourceLocations(basePath);

        // Permitir servir las páginas HTML directamente desde la raíz, p.ej. /machines.html
        registry.addResourceHandler("/*.html").addResourceLocations(basePath);
        // Y también soportar subrutas si en el futuro hay carpetas
        //registry.addResourceHandler("/**/*.html").addResourceLocations(basePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rememberMeInterceptor).addPathPatterns("/**");
        // Primero exigir login para rutas protegidas de usuario
        registry.addInterceptor(userInterceptor).addPathPatterns("/**");
        // Luego validar si además debe ser admin
        registry.addInterceptor(adminOnlyInterceptor).addPathPatterns("/**");
    }
}
