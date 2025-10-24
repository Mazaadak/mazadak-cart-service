package com.mazadak.cart_service.dto.response;

import com.mazadak.cart_service.dto.entity.ProductImageDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponseDTO(
        UUID productId,
        UUID sellerId,
        String title,
        String description,
        BigDecimal price,
        List<ProductImageDTO>images
){  }
