package com.pragma.powerup.infrastructure.out.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraceClientRequest {
    private final Long orderId;
    private final Long clientId;
    private final String clientEmail;
    private final Long restaurantId;
    private final Long restaurantOwnerId;
    private final String previousStatus;
    private final String newStatus;
    private final Long employeeId;
    private final String employeeEmail;
}
