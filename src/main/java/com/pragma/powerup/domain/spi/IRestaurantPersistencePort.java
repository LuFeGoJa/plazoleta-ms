package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;

import java.util.Optional;

public interface IRestaurantPersistencePort {

    RestaurantModel saveRestaurant(RestaurantModel restaurantModel);

    Optional<RestaurantModel> findById(Long id);

    PageModel<RestaurantModel> findAllOrderedByName(int page, int size);

    boolean existsByNit(String nit);
}
