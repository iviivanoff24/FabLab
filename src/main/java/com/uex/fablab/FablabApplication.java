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

    public static void main(String[] args) {
        SpringApplication.run(FablabApplication.class, args);
    }

    // Comprueba la conexión a la base de datos al arrancar
    @Bean
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
