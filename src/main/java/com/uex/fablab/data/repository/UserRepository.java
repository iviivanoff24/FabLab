package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.User;

/**
 * Repositorio JPA para la entidad {@link com.uex.fablab.data.model.User}.
 * Incluye consultas derivadas por email.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /** Verifica si existe un usuario con el email dado. */
    boolean existsByEmail(String email);
    /** Busca un usuario por email. */
    User findByEmail(String email);
    //User findByEmailAndPassword(String email, String password);
}
