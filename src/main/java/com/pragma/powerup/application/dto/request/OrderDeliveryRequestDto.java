package com.pragma.powerup.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDeliveryRequestDto {
    @Schema(example = "048213", description = "PIN de 6 dígitos que el cliente recibió por mensaje")
    private String securityPin;
}
