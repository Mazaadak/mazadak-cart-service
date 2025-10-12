package com.mazadak.cart_service.controller;

import com.mazadak.cart_service.dto.request.AddItemRequest;
import com.mazadak.cart_service.dto.request.UpdateItemRequest;
import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;
import com.mazadak.cart_service.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartItemResponseDTO> addItem(
            @RequestHeader("user-id")  @NotNull(message = "User ID is required") UUID userId,
            @Valid @RequestBody AddItemRequest addItemRequest) {

        return ResponseEntity.ok(cartService.addItem(userId, addItemRequest));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId,
            @PathVariable @NotNull(message = "Product ID is required") UUID productId) {

        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items")
    public ResponseEntity<CartItemResponseDTO> updateItemQuantity(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId,
            @Valid @RequestBody UpdateItemRequest updateItemRequest) {

        return ResponseEntity.ok(cartService.updateItemQuantity(userId, updateItemRequest));
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponseDTO>> getCartItems(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/items/reduce/{productId}")
    public ResponseEntity<CartItemResponseDTO> reduceItemQuantity(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId,
            @Valid @PathVariable UUID productId ) {

        return ResponseEntity.ok(cartService.reduceItemQuantity(userId, productId));
    }
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }
}
