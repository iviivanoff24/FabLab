package com.uex.fablab.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.User;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.Cart}.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {
    /** Busca carrito por usuario. */
    Optional<Cart> findByUser(User user);
}
