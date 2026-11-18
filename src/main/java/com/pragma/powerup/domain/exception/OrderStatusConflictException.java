package com.pragma.powerup.domain.exception;

public class OrderStatusConflictException extends DomainException {
    public OrderStatusConflictException(String message) {
        super(message);
    }
}
