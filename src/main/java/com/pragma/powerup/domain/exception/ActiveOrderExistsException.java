package com.pragma.powerup.domain.exception;

public class ActiveOrderExistsException extends DomainException {
    public ActiveOrderExistsException() {
        super("Ya tienes un pedido en proceso (pendiente, en preparación o listo). "
                + "Debes esperar a que termine para hacer otro");
    }
}
