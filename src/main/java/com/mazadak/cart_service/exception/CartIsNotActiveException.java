package com.mazadak.cart_service.exception;

public class CartIsNotActiveException extends RuntimeException {
    public CartIsNotActiveException(String message) {
        super(message);
    }
}
