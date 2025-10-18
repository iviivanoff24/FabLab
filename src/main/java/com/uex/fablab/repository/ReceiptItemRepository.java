package com.uex.fablab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.model.ReceiptItem;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {
}
