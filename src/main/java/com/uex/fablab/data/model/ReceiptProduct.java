package com.uex.fablab.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Recibo_Producto")
public class ReceiptProduct {

    @EmbeddedId
    private ReceiptProductKey id;

    @ManyToOne
    @MapsId("receiptId")
    @JoinColumn(name = "id_recibo")
    private Receipt receipt;

    @ManyToOne
    @MapsId("subProductId")
    @JoinColumn(name = "id_subp")
    private SubProduct subProduct;

    @NotNull
    @Column(name = "cantidad", nullable = false)
    private Integer quantity = 1;

    @NotNull
    @Column(name = "precio_unitario", nullable = false)
    private Double unitPrice;

    public ReceiptProductKey getId() {
        return id;
    }

    public void setId(ReceiptProductKey id) {
        this.id = id;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public SubProduct getSubProduct() {
        return subProduct;
    }

    public void setSubProduct(SubProduct subProduct) {
        this.subProduct = subProduct;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
