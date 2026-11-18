package com.pragma.powerup.domain.exception;

public class OrderNotOfClientException extends DomainException {
    public OrderNotOfClientException() {
        super("El pedido no pertenece a tu cuenta");
    }
}
