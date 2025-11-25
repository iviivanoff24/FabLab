package com.uex.fablab.data.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "Maquina")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_m")
    private Long id;

    @NotBlank
    @Column(name = "nombre_m", nullable = false)
    private String name;

    @Lob
    @Column(name = "descripcion_m")
    private String description;

    @Column(name = "ubicacion")
    private String location;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "estado_m", nullable = false)
    private MachineStatus status = MachineStatus.Disponible;

    // Precio por hora en euros (ej. 12.50). Usamos BigDecimal para precisión.
    @Column(name = "precio_hora")
    private java.math.BigDecimal hourlyPrice;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shift> shifts = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public java.math.BigDecimal getHourlyPrice() {
        return hourlyPrice;
    }

    public void setHourlyPrice(java.math.BigDecimal hourlyPrice) {
        this.hourlyPrice = hourlyPrice;
    }
}
