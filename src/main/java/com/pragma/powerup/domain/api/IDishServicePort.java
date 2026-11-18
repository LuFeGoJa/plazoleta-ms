package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;

public interface IDishServicePort {

    void saveDish(DishModel dishModel);

    /** Modifica solo el precio y/o la descripción de un plato; los campos null no se tocan. */
    void updateDish(Long dishId, Integer price, String description);

    /** Habilita (true) o deshabilita (false) un plato del menú. */
    void changeDishStatus(Long dishId, Boolean active);

    /**
     * Lista los platos ACTIVOS de un restaurante, ordenados por categoría y nombre.
     * categoryId es opcional (null = todas las categorías); page empieza en 0.
     */
    PageModel<DishModel> listDishes(Long restaurantId, Long categoryId, int page, int size);
}
