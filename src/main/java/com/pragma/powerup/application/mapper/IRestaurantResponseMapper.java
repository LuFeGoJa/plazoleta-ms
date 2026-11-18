package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.RestaurantListItemResponseDto;
import com.pragma.powerup.domain.model.RestaurantModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IRestaurantResponseMapper {

    RestaurantListItemResponseDto toListItem(RestaurantModel restaurantModel);

    List<RestaurantListItemResponseDto> toListItems(List<RestaurantModel> restaurantModels);
}
