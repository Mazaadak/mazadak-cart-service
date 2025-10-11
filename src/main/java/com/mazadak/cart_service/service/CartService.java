package com.mazadak.cart_service.service;

import com.mazadak.cart_service.dto.request.AddItemRequest;
import com.mazadak.cart_service.dto.request.UpdateItemRequest;
import com.mazadak.cart_service.dto.response.CartItemResponseDTO;
import com.mazadak.cart_service.dto.response.CartResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CartService {

    /**
     * Retrieves the active cart for a user
     * Creates a new cart if one doesn't exist.
     * @param userId
     */
    CartResponseDTO getActiveCart(UUID userId);

    /**
     * Retrieves all items in the active cart for a user.
     * @param userId
     */
    List<CartItemResponseDTO> getCartItems(UUID userId);

    /**
     * Adds an item to the active cart for a user.
     * If the item already exists, its quantity is increased.
     * @param userId
     * @param request
     */
    CartItemResponseDTO addItem(UUID userId, AddItemRequest request);

    /**
     * Updates the quantity of an existing cart item.
     * @param userId
     * @param request
     */
    CartItemResponseDTO updateItemQuantity(UUID userId, UpdateItemRequest request);


    /**
     * Decreases the quantity by 1 of an existing cart item.
     * @param userId
     * @param productId
     */
    CartItemResponseDTO reduceItemQuantity(UUID userId, UUID productId);

    /**
     * Removes an item from the active cart for a user.
     * @param userId
     * @param request
     */
    void removeItem(UUID userId, UUID request);

    /**
     * Clears the active cart for a user.
     * @param userId
     */
    void clearCart(UUID userId);
}