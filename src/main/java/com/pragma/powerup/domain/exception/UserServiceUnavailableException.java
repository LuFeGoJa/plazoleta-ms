package com.pragma.powerup.domain.exception;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException() {
        super("No fue posible consultar el servicio de usuarios, intenta de nuevo más tarde");
    }
}
