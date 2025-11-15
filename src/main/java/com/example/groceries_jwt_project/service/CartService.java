package com.example.groceries_jwt_project.service;

import com.example.groceries_jwt_project.entity.Cart;
import com.example.groceries_jwt_project.entity.Groceries;
import com.example.groceries_jwt_project.repository.CartRepository;
import com.example.groceries_jwt_project.repository.GroceriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final GroceriesRepository groceriesRepository;

    // Add item to cart
    public void addItem(Long productId, int quantity) {
        Groceries product = groceriesRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        double totalPrice = product.getPrice() * quantity;

        Cart cart = Cart.builder()
                .userId(getCurrentUserId()) // ✅ Extract from JWT later
                .productId(productId)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .build();

        cartRepository.save(cart);
    }

    // Get all cart items for current user
    public List<Cart> getCartItems() {
        return cartRepository.findByUserId(getCurrentUserId());
    }

    // Remove item from cart
    public void removeItem(Long itemId) {
        if (!cartRepository.existsById(itemId)) {
            throw new RuntimeException("Cart item not found");
        }
        cartRepository.deleteById(itemId);
    }

    // Checkout logic (reduce stock + clear cart)
    public boolean checkout() {
        List<Cart> cartItems = getCartItems();
        if (cartItems.isEmpty()) return false;

        for (Cart item : cartItems) {
            Groceries product = groceriesRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (product.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setQuantity(product.getQuantity() - item.getQuantity());
            groceriesRepository.save(product);
        }

        cartRepository.deleteAll(cartItems);
        return true;
    }

    // ✅ Placeholder for JWT-based user extraction
    private Long getCurrentUserId() {
        // TODO: Extract userId from JWT token using SecurityContext
        return 1L; // Hardcoded for now
    }
}