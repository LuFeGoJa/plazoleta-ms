package com.pragma.powerup.domain.exception;

public class NotRestaurantOwnerException extends DomainException {
    public NotRestaurantOwnerException() {
        super("Solo el propietario del restaurante puede gestionar sus platos");
    }

    public NotRestaurantOwnerException(String message) {
        super(message);
    }
}
