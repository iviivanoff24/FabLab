package com.uex.fablab.data.model;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad Reserva: asociación de un {@link User} con un {@link Shift}. Incluye fecha
 * de creación y estado de la reserva. La lógica de negocio asegura que sólo se
 * pueda reservar turnos futuros y cancelar antes de su inicio.
 */
@Entity
@Table(name = "Reserva")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @jakarta.persistence.Column(name = "id_reserva")
    private Long id;

    @JsonBackReference(value = "user-bookings")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_u", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @JsonBackReference(value = "shift-bookings")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_turno", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Shift shift;

    @jakarta.persistence.Column(name = "fecha_reserva", nullable = false)
    private java.time.LocalDate fechaReserva;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @jakarta.persistence.Column(name = "estado_reserva", nullable = false)
    private BookingStatus estado = BookingStatus.Pendiente;

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

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
        if (shift != null && !shift.getBookings().contains(this)) {
            shift.getBookings().add(this);
        }
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public BookingStatus getEstado() {
        return estado;
    }

    public void setEstado(BookingStatus estado) {
        this.estado = estado;
    }
}
