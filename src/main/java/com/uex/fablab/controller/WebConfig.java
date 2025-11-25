package com.uex.fablab.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración Web MVC.
 * Define handlers de recursos estáticos y registra interceptores de sesión/seguridad.
 */
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

    /**
     * Mapeo de rutas estáticas para CSS, JS e imágenes, incluyendo subidas en disco.
     * @param registry registro de handlers de recursos
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mantener mapeos actuales para recursos estáticos mientras siguen en 'templates',
        // pero se elimina el handler de HTML para no interferir con ThymeleafViewResolver.
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
    }

    /**
     * Registra interceptores en orden: remember-me, usuario autenticado y administrador.
     * @param registry registro de interceptores
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rememberMeInterceptor).addPathPatterns("/**");
        // Primero exigir login para rutas protegidas de usuario
        registry.addInterceptor(userInterceptor).addPathPatterns("/**");
        // Luego validar si además debe ser admin
        registry.addInterceptor(adminOnlyInterceptor).addPathPatterns("/**");
    }
}
