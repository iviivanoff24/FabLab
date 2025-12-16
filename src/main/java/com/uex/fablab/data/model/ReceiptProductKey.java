package com.uex.fablab.data.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Clave compuesta para {@link ReceiptProduct}.
 */
@Embeddable
public class ReceiptProductKey implements Serializable {

    @Column(name = "id_recibo")
    private Long receiptId;

    @Column(name = "id_subp")
    private Long subProductId;

    public ReceiptProductKey() {
    }

    public ReceiptProductKey(Long receiptId, Long subProductId) {
        this.receiptId = receiptId;
        this.subProductId = subProductId;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public Long getSubProductId() {
        return subProductId;
    }

    public void setSubProductId(Long subProductId) {
        this.subProductId = subProductId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReceiptProductKey that = (ReceiptProductKey) o;
        return Objects.equals(receiptId, that.receiptId) && Objects.equals(subProductId, that.subProductId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId, subProductId);
    }
}
