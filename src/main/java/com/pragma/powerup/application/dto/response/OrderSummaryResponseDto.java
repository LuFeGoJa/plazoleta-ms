package com.pragma.powerup.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Un pedido tal como lo ve el empleado en su lista. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponseDto {
    private Long id;
    private Long clientId;
    private String status;
    private LocalDateTime date;
    /** Empleado asignado; null mientras nadie haya tomado el pedido. */
    private Long chefId;
    private List<OrderDishResponseDto> dishes;
}
