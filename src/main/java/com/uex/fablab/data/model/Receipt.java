package com.uex.fablab.data.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @JsonBackReference(value = "user-receipts")
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

    // Asociación muchos-a-muchos con Turno mediante tabla Recibo_Turno
    @ManyToMany
    @JoinTable(
        name = "Recibo_Turno",
        joinColumns = @JoinColumn(name = "id_recibo"),
        inverseJoinColumns = @JoinColumn(name = "id_turno")
    )
    private Set<Shift> shifts = new HashSet<>();

    // Asociación opcional con Máquina (id_m)
    @ManyToOne(optional = true)
    @JoinColumn(name = "id_m", nullable = true)
    private Machine machine;

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

    public Set<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(Set<Shift> shifts) {
        this.shifts = shifts != null ? shifts : new HashSet<>();
    }

    public void addShift(Shift s) {
        if (s != null) {
            this.shifts.add(s);
        }
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }
}
