package com.pragma.powerup.domain.spi;

/** Puerto de salida hacia el microservicio de Mensajería. */
public interface INotificationPort {

    /**
     * Envía un mensaje de texto. Lanza NotificationUnavailableException si el servicio no responde y
     * DomainException si el celular no tiene formato internacional.
     */
    void sendSms(String phoneNumber, String message);
}
