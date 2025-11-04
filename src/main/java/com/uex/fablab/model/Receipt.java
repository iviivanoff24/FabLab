package com.uex.fablab.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

@Entity
@Table(name = "Recibo")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_u", nullable = false)
    private User user;

    @NotNull
    @Column(name = "importe_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "fecha_emision", nullable = false)
    private java.time.LocalDateTime fechaEmision = java.time.LocalDateTime.now();

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "metodo_pago")
    private PaymentMethod metodoPago;

    @Column(name = "concepto")
    private String concepto;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "estado_recibo")
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getDate() {
        return fechaEmision;
    }

    public void setDate(LocalDateTime date) {
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
