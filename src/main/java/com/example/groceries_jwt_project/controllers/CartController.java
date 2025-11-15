package com.example.groceries_jwt_project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.groceries_jwt_project.entity.Cart;
import com.example.groceries_jwt_project.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Manage user cart")
public class CartController {

	private final CartService cartService;

	@Operation(summary = "Add item to cart", description = "Adds a product to the user's cart")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Item added", content = @Content(schema = @Schema(implementation = Cart.class))) })

	@PostMapping("/add")
	public ResponseEntity<String> addToCart(@RequestParam Long productId, @RequestParam int quantity) {
		cartService.addItem(productId, quantity);
		return ResponseEntity.ok("Item added to cart successfully");
	}

	@Operation(summary = "View cart items", description = "Returns all items in the user's cart")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Cart items", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cart.class)))) })

	@GetMapping
	public ResponseEntity<Object> viewCart() {
		return ResponseEntity.ok(cartService.getCartItems());
	}

	@Operation(summary = "Remove item from cart", description = "Removes an item from the cart")
	@DeleteMapping("/remove/{itemId}")
	public ResponseEntity<String> removeItem(@PathVariable Long itemId) {
		cartService.removeItem(itemId);
		return ResponseEntity.ok("Item removed from cart");
	}

	@Operation(summary = "Checkout", description = "Completes the purchase and clears the cart")
	@PostMapping("/checkout")
	public ResponseEntity<String> checkout() {
		boolean success = cartService.checkout();
		return success ? ResponseEntity.ok("Order placed successfully")
				: ResponseEntity.badRequest().body("Checkout failed");
	}
}