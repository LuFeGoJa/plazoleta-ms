package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishListItemResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DishHandler implements IDishHandler {

    private final IDishServicePort dishServicePort;
    private final IDishRequestMapper dishRequestMapper;
    private final IDishResponseMapper dishResponseMapper;

    @Override
    public void saveDish(DishRequestDto dishRequestDto) {
        dishServicePort.saveDish(dishRequestMapper.toDish(dishRequestDto));
    }

    @Override
    public void updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto) {
        dishServicePort.updateDish(dishId, dishUpdateRequestDto.getPrice(), dishUpdateRequestDto.getDescription());
    }

    @Override
    public void changeDishStatus(Long dishId, DishStatusRequestDto dishStatusRequestDto) {
        dishServicePort.changeDishStatus(dishId, dishStatusRequestDto.getActive());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<DishListItemResponseDto> listDishes(Long restaurantId, Long categoryId, int page, int size) {
        PageModel<DishModel> result = dishServicePort.listDishes(restaurantId, categoryId, page, size);
        return new PageResponseDto<>(dishResponseMapper.toListItems(result.getContent()),
                result.getPage(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
