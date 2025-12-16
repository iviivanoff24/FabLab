package com.uex.fablab.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uex.fablab.data.model.Cart;
import com.uex.fablab.data.model.CartItem;
import com.uex.fablab.data.model.User;
import com.uex.fablab.data.services.CartService;
import com.uex.fablab.data.services.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador para la gestión del carrito de compras.
 */
@Controller
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * Constructor.
     * @param cartService servicio de carrito
     * @param userService servicio de usuarios
     */
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/cart")
    /**
     * Muestra la vista del carrito de compras.
     *
     * @param session sesión HTTP
     * @param model modelo para la vista
     * @return nombre de la vista del carrito
     */
    public String viewCart(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId).orElse(null);
        if (user == null) return "redirect:/login";

        Cart cart = cartService.getCartByUser(user);
        
        // Prepare view model
        CartViewModel vm = new CartViewModel();
        double total = 0;
        for (CartItem item : cart.getItems()) {
            CartItemViewModel itemVm = new CartItemViewModel();
            itemVm.setSubProductId(item.getSubProduct().getId());
            itemVm.setProductName(item.getSubProduct().getProduct().getName());
            itemVm.setSubProductName(item.getSubProduct().getSubName());
            itemVm.setPrice(item.getSubProduct().getPrice());
            itemVm.setQuantity(item.getQuantity());
            itemVm.setStock(item.getSubProduct().getStock());
            itemVm.setTotal(item.getSubProduct().getPrice() * item.getQuantity());
            vm.getItems().add(itemVm);
            total += itemVm.getTotal();
        }
        vm.setTotalPrice(total);

        model.addAttribute("cart", vm);
        return "cart";
    }

    @PostMapping("/cart/add")
    /**
     * Añade un producto al carrito (desde formulario).
     *
     * @param subProductId id del subproducto
     * @param quantity cantidad
     * @param session sesión HTTP
     * @return redirección al carrito
     */
    public String addToCart(@RequestParam("subProductId") Long subProductId, 
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/login";
        }
        cartService.addToCart(userId, subProductId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/api/add")
    @ResponseBody
    /**
     * Añade un producto al carrito (API AJAX).
     *
     * @param subProductId id del subproducto
     * @param quantity cantidad
     * @param session sesión HTTP
     * @return "ok" o mensaje de error
     */
    public String addToCartApi(@RequestParam("subProductId") Long subProductId, 
                               @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                               HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "error:login";
        }
        try {
            cartService.addToCart(userId, subProductId, quantity);
            return "ok";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    @PostMapping("/cart/update")
    /**
     * Actualiza la cantidad de un producto en el carrito.
     *
     * @param subProductId id del subproducto
     * @param quantity nueva cantidad
     * @param session sesión HTTP
     * @return redirección al carrito
     */
    public String updateQuantity(@RequestParam("subProductId") Long subProductId, 
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId != null) {
            cartService.updateQuantity(userId, subProductId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("subProductId") Long subProductId, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId != null) {
            cartService.removeFromCart(userId, subProductId);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId != null) {
            cartService.clearCart(userId);
        }
        return "redirect:/cart";
    }

    // View Models
    public static class CartViewModel {
        private List<CartItemViewModel> items = new ArrayList<>();
        private Double totalPrice = 0.0;

        public List<CartItemViewModel> getItems() { return items; }
        public void setItems(List<CartItemViewModel> items) { this.items = items; }
        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    }

    public static class CartItemViewModel {
        private Long subProductId;
        private String productName;
        private String subProductName;
        private Double price;
        private Integer quantity;
        private Integer stock;
        private Double total;

        public Long getSubProductId() { return subProductId; }
        public void setSubProductId(Long subProductId) { this.subProductId = subProductId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSubProductName() { return subProductName; }
        public void setSubProductName(String subProductName) { this.subProductName = subProductName; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }
}
