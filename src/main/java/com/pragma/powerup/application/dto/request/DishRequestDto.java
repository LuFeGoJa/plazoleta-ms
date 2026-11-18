package com.pragma.powerup.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishRequestDto {
    @Schema(example = "Bandeja paisa")
    private String name;
    @Schema(example = "32000", description = "Entero positivo mayor a 0")
    private Integer price;
    @Schema(example = "Frijoles, arroz, carne molida, chicharrón, huevo y arepa")
    private String description;
    @Schema(example = "https://miplazoleta.com/platos/bandeja-paisa.jpg")
    private String imageUrl;
    @Schema(example = "2", description = "Id de la categoría (1 Entradas, 2 Platos fuertes, 3 Postres, 4 Bebidas, 5 Acompañamientos)")
    private Long categoryId;
    @Schema(example = "1", description = "Id de un restaurante del que el usuario es propietario")
    private Long restaurantId;
}
