package com.uex.fablab.data.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Clave compuesta para {@link CartItem}.
 */
@Embeddable
public class CartItemKey implements Serializable {

    @Column(name = "id_carrito")
    private Long cartId;

    @Column(name = "id_subp")
    private Long subProductId;

    public CartItemKey() {
    }

    public CartItemKey(Long cartId, Long subProductId) {
        this.cartId = cartId;
        this.subProductId = subProductId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getSubProductId() {
        return subProductId;
    }

    public void setSubProductId(Long subProductId) {
        this.subProductId = subProductId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemKey that = (CartItemKey) o;
        return Objects.equals(cartId, that.cartId) && Objects.equals(subProductId, that.subProductId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, subProductId);
    }
}
