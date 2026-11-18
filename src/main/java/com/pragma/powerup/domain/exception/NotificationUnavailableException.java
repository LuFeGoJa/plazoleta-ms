package com.pragma.powerup.domain.exception;

public class NotificationUnavailableException extends RuntimeException {
    public NotificationUnavailableException() {
        super("No fue posible enviar el mensaje al cliente, intenta de nuevo más tarde");
    }
}
