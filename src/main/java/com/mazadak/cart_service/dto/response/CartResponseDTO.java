package com.mazadak.cart_service.dto.response;


import java.util.List;
import java.util.UUID;

public record CartResponseDTO(
        UUID cartId,
        UUID userId,
        List<CartItemResponseDTO> cartItems
){ }
