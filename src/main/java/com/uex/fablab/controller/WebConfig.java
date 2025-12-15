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
        // Recursos estáticos estándar (Spring Boot busca en static/ por defecto, pero aquí forzamos/añadimos)
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/", "classpath:/templates/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/", "classpath:/templates/js/");
        
        // Imágenes estáticas generales
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/", "classpath:/templates/img/");

        // Configuración para servir imágenes subidas dinámicamente desde el sistema de archivos (src)
        // Esto permite ver las imágenes recién subidas sin reiniciar
        String userDir = System.getProperty("user.dir");
        Path projectRoot = Path.of(userDir);
        if (Files.exists(projectRoot.resolve("ProyectoMDAI"))) {
            projectRoot = projectRoot.resolve("ProyectoMDAI");
        }
        
        Path uploadsPath = projectRoot.resolve("src/main/resources/uploads");
        String uploadsUrl = "file:///" + uploadsPath.toAbsolutePath().toString().replace('\\', '/') + "/";

        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadsUrl);

        Path staticImgPath = projectRoot.resolve("src/main/resources/static/img");
        String fileUrl = "file:///" + staticImgPath.toAbsolutePath().toString().replace('\\', '/') + "/";

        // Handlers específicos para cada tipo de entidad (Legacy / Static)
        registry.addResourceHandler("/img/products/**").addResourceLocations(fileUrl + "products/");
        registry.addResourceHandler("/img/machines/**").addResourceLocations(fileUrl + "machines/");
        registry.addResourceHandler("/img/courses/**").addResourceLocations(fileUrl + "courses/");
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
