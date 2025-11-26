package com.uex.fablab.data.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad Turno (slot horario de una máquina). Representa un intervalo de una hora
 * en una fecha concreta asociado a una {@link Machine}. Se crea sólo cuando existe
 * una reserva sobre ese intervalo. El estado indica disponibilidad general del slot.
 */
@Entity
@Table(name = "Turno")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long id;

    @JsonBackReference(value = "machine-shifts")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_m", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Machine machine;

    @NotNull
    @Column(name = "fecha_turno", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "hora_ini", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_turno", nullable = false)
    private ShiftStatus status = ShiftStatus.Disponible;

    @JsonManagedReference(value = "shift-bookings")
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
        if (machine != null && !machine.getShifts().contains(this)) {
            machine.getShifts().add(this);
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public List<Booking> getBookings() {
        return bookings;
    }
}
