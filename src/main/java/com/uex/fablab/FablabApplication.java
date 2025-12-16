package com.uex.fablab;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FablabApplication {

    private static final Logger log = LoggerFactory.getLogger(FablabApplication.class);

    /**
     * Punto de entrada de la aplicación FabLab basada en Spring Boot.
     * Arranca el contexto de la aplicación.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(FablabApplication.class, args);
    }

    @Bean
    /**
     * Runner que valida la conectividad con la base de datos al iniciar la aplicación.
     * Registra en el log la URL y el usuario conectados o el error si falla.
     *
     * @param dataSource el {@link javax.sql.DataSource} configurado para la aplicación
     * @return un {@link CommandLineRunner} que realiza la comprobación al arrancar
     */
    public CommandLineRunner comprobarConexion(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                String url = conn.getMetaData().getURL();
                String user = conn.getMetaData().getUserName();
                log.info("Conexión establecida a BD -> URL: {}  usuario: {}", url, user);

            } catch (SQLException e) {
                log.error("No se pudo conectar a la BD: {}", e.getMessage());
                // opcional: lanzar la excepción para detener la app
                // throw e;
            }
        };
    }
}
