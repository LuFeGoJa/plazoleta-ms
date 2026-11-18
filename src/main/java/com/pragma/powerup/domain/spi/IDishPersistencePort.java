package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;

import java.util.Optional;

public interface IDishPersistencePort {

    DishModel saveDish(DishModel dishModel);

    Optional<DishModel> findById(Long id);

    PageModel<DishModel> findActiveByRestaurant(Long restaurantId, Long categoryId, int page, int size);
}
