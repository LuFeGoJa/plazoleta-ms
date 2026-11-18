package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;

public interface IRestaurantServicePort {

    void saveRestaurant(RestaurantModel restaurantModel);

    /** Lista los restaurantes por orden alfabético; page empieza en 0. */
    PageModel<RestaurantModel> listRestaurants(int page, int size);
}
