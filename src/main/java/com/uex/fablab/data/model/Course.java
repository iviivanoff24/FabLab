package com.uex.fablab.data.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad Curso: actividad formativa con fechas de inicio/fin, capacidad y estado.
 * Las inscripciones se modelan mediante {@link Inscription}. El estado controla
 * visibilidad y disponibilidad (activo, cancelado, etc.).
 */
@Entity
@Table(name = "Curso")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_c")
    private Long id;

    @NotBlank
    @Column(name = "nombre_c", nullable = false)
    private String name;

    @Lob
    @Column(name = "descripcion_c")
    private String description;

    @Column(name = "capacidad_c", nullable = false)
    private Integer capacity = 0;

    @NotNull
    @Column(name = "fecha_ini", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate endDate;

    @Column(name = "precio", nullable = false)
    private Double precio = 0.0;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "estado_c", nullable = false)
    private CourseStatus estado = CourseStatus.Activo;

    @JsonManagedReference(value = "course-inscriptions")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscription> inscriptions = new ArrayList<>();

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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public CourseStatus getEstado() {
        return estado;
    }

    public void setEstado(CourseStatus estado) {
        this.estado = estado;
    }

    public List<Inscription> getInscriptions() {
        return inscriptions;
    }
}
