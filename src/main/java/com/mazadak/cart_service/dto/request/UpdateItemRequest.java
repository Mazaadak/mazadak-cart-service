package com.mazadak.cart_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record UpdateItemRequest(

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) { }
