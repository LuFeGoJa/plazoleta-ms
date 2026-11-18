package com.pragma.powerup.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantRequestDto {
    @Schema(example = "Sabor Criollo 54", description = "Puede contener números, pero no solo números")
    private String name;
    @Schema(example = "900123456", description = "Solo números")
    private String nit;
    @Schema(example = "Calle 10 # 5-20, Local 3")
    private String address;
    @Schema(example = "+573005698325", description = "Máximo 13 caracteres, puede iniciar con +")
    private String phone;
    @Schema(example = "https://miplazoleta.com/logos/sabor-criollo.png")
    private String logoUrl;
    @Schema(example = "2", description = "Id de un usuario con rol propietario")
    private Long ownerId;
}
