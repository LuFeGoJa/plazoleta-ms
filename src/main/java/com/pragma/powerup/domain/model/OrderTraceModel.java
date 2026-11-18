package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un cambio de estado de un pedido, tal como se envía a trazabilidad-ms. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderTraceModel {
    private Long orderId;
    private Long clientId;
    private String clientEmail;
    private Long restaurantId;
    /** Propietario del restaurante; trazabilidad-ms lo usa para que cada dueño vea solo su eficiencia. */
    private Long restaurantOwnerId;
    /** null cuando el pedido se acaba de crear. */
    private String previousStatus;
    private String newStatus;
    /** null cuando el cambio lo hizo el cliente (crear o cancelar). */
    private Long employeeId;
    private String employeeEmail;
}
