package com.pragma.powerup.domain.exception;

public class InvalidSecurityPinException extends DomainException {
    public InvalidSecurityPinException() {
        super("El PIN de seguridad no coincide con el del pedido");
    }
}
