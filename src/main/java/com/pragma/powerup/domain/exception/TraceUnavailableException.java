package com.pragma.powerup.domain.exception;

public class TraceUnavailableException extends RuntimeException {
    public TraceUnavailableException() {
        super("No fue posible registrar la trazabilidad del pedido, intenta de nuevo más tarde");
    }
}
