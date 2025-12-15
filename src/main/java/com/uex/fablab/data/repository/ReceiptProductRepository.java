package com.uex.fablab.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.model.ReceiptProduct;
import com.uex.fablab.data.model.ReceiptProductKey;

public interface ReceiptProductRepository extends JpaRepository<ReceiptProduct, ReceiptProductKey> {
    List<ReceiptProduct> findByReceipt(Receipt receipt);
}
