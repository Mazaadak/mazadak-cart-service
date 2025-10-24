package com.mazadak.cart_service.dto.response;

import com.mazadak.cart_service.dto.entity.ProductImageDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DetailedCartItemResponseDTO (
    UUID productId,
    int quantity,
    String title,
    String description,
    BigDecimal price,
    String primaryImage
 ) { }
