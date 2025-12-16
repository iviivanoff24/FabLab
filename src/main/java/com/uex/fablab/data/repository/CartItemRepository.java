package com.uex.fablab.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.CartItem;
import com.uex.fablab.data.model.CartItemKey;

/**
 * Repositorio JPA para {@link com.uex.fablab.data.model.CartItem}.
 */
public interface CartItemRepository extends JpaRepository<CartItem, CartItemKey> {
    
    /** Busca items por carrito. */
    List<CartItem> findByCart(Cart cart);

    /** Elimina items por id de carrito. */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
