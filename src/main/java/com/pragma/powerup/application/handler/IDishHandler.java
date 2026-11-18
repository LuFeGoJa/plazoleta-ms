package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishListItemResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;

public interface IDishHandler {

    void saveDish(DishRequestDto dishRequestDto);

    void updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto);

    void changeDishStatus(Long dishId, DishStatusRequestDto dishStatusRequestDto);

    PageResponseDto<DishListItemResponseDto> listDishes(Long restaurantId, Long categoryId, int page, int size);
}
