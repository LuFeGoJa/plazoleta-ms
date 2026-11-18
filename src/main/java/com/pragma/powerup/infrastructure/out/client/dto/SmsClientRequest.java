package com.pragma.powerup.infrastructure.out.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmsClientRequest {
    private final String phoneNumber;
    private final String message;
}
