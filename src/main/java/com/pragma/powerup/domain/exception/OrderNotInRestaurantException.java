package com.pragma.powerup.domain.exception;

public class OrderNotInRestaurantException extends DomainException {
    public OrderNotInRestaurantException() {
        super("El pedido no pertenece al restaurante donde trabajas");
    }
}
