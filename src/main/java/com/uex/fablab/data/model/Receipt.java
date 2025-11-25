package com.uex.fablab.data.model;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad Recibo de pago: agrupa importe total, método de pago y estado de
 * procesamiento/confirmación. Se asocia a un {@link User} y puede incluir un
 * concepto libre (texto descriptivo de la transacción).
 */
@Entity
@Table(name = "Recibo")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_u", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @NotNull
    @Column(name = "importe_total", nullable = false)
    private Double totalPrice;

    @Column(name = "fecha_emision", nullable = false)
    private java.time.LocalDate fechaEmision = java.time.LocalDate.now();

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private PaymentMethod metodoPago;

    @Column(name = "concepto")
    private String concepto;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "estado_recibo", nullable = false)
    private ReceiptStatus estadoRecibo = ReceiptStatus.Pendiente;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate date) {
        this.fechaEmision = date;
    }

    public PaymentMethod getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(PaymentMethod metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public ReceiptStatus getEstadoRecibo() {
        return estadoRecibo;
    }

    public void setEstadoRecibo(ReceiptStatus estadoRecibo) {
        this.estadoRecibo = estadoRecibo;
    }
}
