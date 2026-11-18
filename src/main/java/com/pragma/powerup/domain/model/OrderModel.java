package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderModel {
    private Long id;
    private Long clientId;
    private Long restaurantId;
    private LocalDateTime date;
    private OrderStatus status;
    /** Empleado que prepara el pedido; null mientras nadie lo haya tomado. */
    private Long chefId;
    private List<OrderDishModel> dishes;
    /** PIN de 6 dígitos que el cliente debe presentar al recoger; solo existe cuando el pedido está LISTO. */
    private String securityPin;
}
