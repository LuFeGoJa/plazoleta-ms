package com.pragma.powerup.application.dto.response;

import lombok.Getter;
import lombok.Setter;

/** Un plato tal como lo ve el cliente en el menú. */
@Getter
@Setter
public class DishListItemResponseDto {
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Long categoryId;
}
