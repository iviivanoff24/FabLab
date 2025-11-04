package com.uex.fablab.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "Usuario")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "nombre_u", nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(name = "email_u", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "contrasena_u", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // El esquema original tiene "rol" varchar. Guardamos el rol y exponemos isAdmin()/setAdmin(boolean)
    @Column(name = "rol")
    private String role;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "fecha_registro")
    private java.time.LocalDate fechaRegistro;

    public User() {}

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public void setAdmin(boolean admin) {
        this.role = admin ? "ADMIN" : "USER";
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public java.time.LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(java.time.LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
