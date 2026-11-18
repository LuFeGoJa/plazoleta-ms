package com.pragma.powerup.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDto {
    @Schema(example = "1", description = "Id del restaurante donde se hace el pedido")
    private Long restaurantId;
    @Schema(description = "Platos del pedido; no se puede repetir un plato, se indica su cantidad")
    private List<OrderDishRequestDto> dishes;
}
