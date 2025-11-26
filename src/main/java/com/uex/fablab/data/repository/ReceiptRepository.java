package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Receipt;

/**
 * Repositorio para {@link com.uex.fablab.data.model.Receipt}.
 */
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
