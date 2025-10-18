package com.uex.fablab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
