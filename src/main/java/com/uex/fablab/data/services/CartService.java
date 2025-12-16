package com.uex.fablab.data.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.CartItem;
import com.uex.fablab.data.model.CartItemKey;
import com.uex.fablab.data.model.SubProduct;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.repository.CartItemRepository;
import com.uex.fablab.data.repository.CartRepository;
import com.uex.fablab.data.repository.SubProductRepository;
import com.uex.fablab.data.repository.UserRepository;

/**
 * Servicio para la gestión del carrito de compras.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final SubProductRepository subProductRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Constructor.
     * @param cartRepository repositorio de carritos
     * @param subProductRepository repositorio de subproductos
     * @param userRepository repositorio de usuarios
     * @param cartItemRepository repositorio de items del carrito
     */
    public CartService(CartRepository cartRepository, SubProductRepository subProductRepository, UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.subProductRepository = subProductRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Obtiene el carrito de un usuario, creándolo si no existe.
     * @param user usuario
     * @return carrito del usuario
     */
    @Transactional
    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    /**
     * Añade un producto al carrito.
     * @param userId id del usuario
     * @param subProductId id del subproducto
     * @param quantity cantidad a añadir
     */
    @Transactional
    public void addToCart(Long userId, Long subProductId, int quantity) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = getCartByUser(user);
        SubProduct subProduct = subProductRepository.findById(subProductId).orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSubProduct().getId().equals(subProductId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setId(new CartItemKey(cart.getId(), subProductId));
            newItem.setCart(cart);
            newItem.setSubProduct(subProduct);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
        }
        cartRepository.save(cart);
    }

    /**
     * Actualiza la cantidad de un producto en el carrito.
     * Si la cantidad es 0 o menor, elimina el producto.
     * @param userId id del usuario
     * @param subProductId id del subproducto
     * @param quantity nueva cantidad
     */
    @Transactional
    public void updateQuantity(Long userId, Long subProductId, int quantity) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = getCartByUser(user);

        cart.getItems().stream()
                .filter(item -> item.getSubProduct().getId().equals(subProductId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity > 0) {
                        item.setQuantity(quantity);
                    } else {
                        cart.removeItem(item);
                    }
                });
        cartRepository.save(cart);
    }

    /**
     * Elimina un producto del carrito.
     * @param userId id del usuario
     * @param subProductId id del subproducto
     */
    @Transactional
    public void removeFromCart(Long userId, Long subProductId) {
        updateQuantity(userId, subProductId, 0);
    }

    /**
     * Vacía el carrito de un usuario por su id.
     * @param userId id del usuario
     */
    @Transactional
    public void clearCart(Long userId) {
    /**
     * Vacía el carrito de un usuario.
     * @param user usuario
     */
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        clearCart(user);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        
        // 1. Limpiar la lista en memoria (esto activa orphanRemoval=true)
        cart.getItems().clear();
        
        // 2. Guardar el cambio
        cartRepository.save(cart);
        
        // 3. Forzar el borrado físico en BD ahora mismo
        cartItemRepository.deleteByCartId(cart.getId());
    }
}
