package com.uex.fablab.data.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad Producto.
 * Representa un producto genérico que puede tener variantes (subproductos).
 */
@Entity
@Table(name = "Producto")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_p")
    private Long id;

    @NotNull
    @Column(name = "nombre_p", nullable = false)
    private String name;

    @Column(name = "descripcion_p")
    private String description;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false)
    private ProductType type;

    @OneToMany(mappedBy = "product", fetch = jakarta.persistence.FetchType.EAGER, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<SubProduct> subProducts = new ArrayList<>();

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

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public List<SubProduct> getSubProducts() {
        return subProducts;
    }

    public void setSubProducts(List<SubProduct> subProducts) {
        this.subProducts = subProducts;
    }

    public List<String> getAllImages() {
        List<String> images = new ArrayList<>();
        if (subProducts != null) {
            for (SubProduct sp : subProducts) {
                if (sp.getImage1() != null && !sp.getImage1().isEmpty()) images.add(sp.getImage1());
                if (sp.getImage2() != null && !sp.getImage2().isEmpty()) images.add(sp.getImage2());
                if (sp.getImage3() != null && !sp.getImage3().isEmpty()) images.add(sp.getImage3());
            }
        }
        return images;
    }
}
