package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un plato dentro de un pedido, con su cantidad. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDishModel {
    private Long dishId;
    private Integer quantity;
}
