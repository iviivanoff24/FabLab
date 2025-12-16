package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Product;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.Product}.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
