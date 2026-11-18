package com.pragma.powerup.application.dto.response;

import lombok.Getter;
import lombok.Setter;

/** Un restaurante tal como lo ve el cliente: solo nombre y logo. */
@Getter
@Setter
public class RestaurantListItemResponseDto {
    private String name;
    private String logoUrl;
}
