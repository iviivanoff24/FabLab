package com.uex.fablab.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uex.fablab.data.model.CartItem;
import com.uex.fablab.data.model.CartItemKey;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemKey> {
    
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
