package com.mazadak.cart_service.dto.response;


import java.util.UUID;

public record CartItemResponseDTO(
        UUID itemId,
        UUID productId,
        int quantity
) { }
