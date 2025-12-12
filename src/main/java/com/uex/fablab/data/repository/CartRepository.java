package com.uex.fablab.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
