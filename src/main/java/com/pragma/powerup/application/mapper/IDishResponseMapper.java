package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.DishListItemResponseDto;
import com.pragma.powerup.domain.model.DishModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IDishResponseMapper {

    DishListItemResponseDto toListItem(DishModel dishModel);

    List<DishListItemResponseDto> toListItems(List<DishModel> dishModels);
}
