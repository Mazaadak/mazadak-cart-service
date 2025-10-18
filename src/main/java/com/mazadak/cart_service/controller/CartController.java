package com.mazadak.cart_service.controller;

import com.mazadak.cart_service.dto.request.AddItemRequest;
import com.mazadak.cart_service.dto.request.UpdateItemRequest;
import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;
import com.mazadak.cart_service.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j
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

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartItemResponseDTO> updateItemQuantity(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId,
            @PathVariable @NotNull(message = "Product ID is required") UUID productId,
            @Valid @RequestBody UpdateItemRequest updateItemRequest) {

        return ResponseEntity.ok(cartService.updateItemQuantity(userId,productId, updateItemRequest));
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponseDTO>> getCartItems(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PatchMapping("/items/reduce/{productId}")
    public ResponseEntity<CartItemResponseDTO> reduceItemQuantity(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId,
            @Valid @PathVariable UUID productId ,
            @Min(value = 1, message = "Quantity must be at least 1") @RequestParam(defaultValue = "1") int quantity) {

        return ResponseEntity.ok(cartService.reduceItemQuantity(userId, productId, quantity));
    }
    @PostMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {
        log.info("Clearing cart for user from controller {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateCart(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        cartService.activateCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivateCart(
            @RequestHeader("user-id") @NotNull(message = "User ID is required") UUID userId) {

        cartService.deactivateCart(userId);
        return ResponseEntity.noContent().build();
    }
}
