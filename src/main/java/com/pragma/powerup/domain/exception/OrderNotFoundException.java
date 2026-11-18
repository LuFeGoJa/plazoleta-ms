package com.pragma.powerup.domain.exception;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(Long id) {
        super("No existe un pedido con id " + id);
    }
}
