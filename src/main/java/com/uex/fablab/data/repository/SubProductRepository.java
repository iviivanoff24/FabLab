package com.uex.fablab.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Product;
import com.uex.fablab.data.model.SubProduct;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.SubProduct}.
 */
public interface SubProductRepository extends JpaRepository<SubProduct, Long> {
    /** Busca subproductos por id de producto. */
    List<SubProduct> findByProductId(Long productId);
    /** Busca subproductos por producto. */
    List<SubProduct> findByProduct(Product product);
}
