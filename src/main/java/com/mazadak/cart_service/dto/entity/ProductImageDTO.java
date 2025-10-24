package com.mazadak.cart_service.dto.entity;

public record ProductImageDTO (
      Long imageId,
      String imageUri,
      Boolean isPrimary,
      Integer position
){ }
