package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uex.fablab.data.model.Course;
import com.uex.fablab.data.model.PaymentMethod;
import com.uex.fablab.data.model.Receipt;
import com.uex.fablab.data.model.ReceiptStatus;
import com.uex.fablab.data.model.User;

/**
 * Repositorio para {@link com.uex.fablab.data.model.Receipt}.
 */
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
	boolean existsByUserAndCourseAndMetodoPagoAndEstadoRecibo(User user, Course course, PaymentMethod metodoPago, ReceiptStatus estadoRecibo);
	boolean existsByUserAndMetodoPagoAndEstadoReciboAndConceptoContaining(User user, PaymentMethod metodoPago, ReceiptStatus estadoRecibo, String conceptoPart);
}
