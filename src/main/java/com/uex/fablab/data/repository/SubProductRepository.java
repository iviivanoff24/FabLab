package com.uex.fablab.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.SubProduct;

public interface SubProductRepository extends JpaRepository<SubProduct, Long> {
    List<SubProduct> findByProductId(Long productId);
}
