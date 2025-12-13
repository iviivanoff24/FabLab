package com.uex.fablab.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.CartService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final UserService userService;

    public GlobalControllerAdvice(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @ModelAttribute("cartCount")
    public int populateCartCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId != null) {
            User user = userService.findById(userId).orElse(null);
            if (user != null) {
                Cart cart = cartService.getCartByUser(user);
                if (cart != null && cart.getItems() != null) {
                    return cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
                }
            }
        }
        return 0;
    }
}
