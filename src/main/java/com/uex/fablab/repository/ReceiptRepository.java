package com.uex.fablab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
