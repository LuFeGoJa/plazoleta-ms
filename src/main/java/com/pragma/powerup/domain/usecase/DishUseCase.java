package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.ICurrentUserPort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;

public class DishUseCase implements IDishServicePort {

    private static final int MAX_PAGE_SIZE = 100;

    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ICategoryPersistencePort categoryPersistencePort;
    private final ICurrentUserPort currentUserPort;

    public DishUseCase(IDishPersistencePort dishPersistencePort,
                       IRestaurantPersistencePort restaurantPersistencePort,
                       ICategoryPersistencePort categoryPersistencePort,
                       ICurrentUserPort currentUserPort) {
        this.dishPersistencePort = dishPersistencePort;
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public PageModel<DishModel> listDishes(Long restaurantId, Long categoryId, int page, int size) {
        if (restaurantId == null) {
            throw new DomainException("El restaurante es obligatorio");
        }
        if (page < 0) {
            throw new DomainException("El número de página no puede ser negativo");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new DomainException("La cantidad de elementos por página debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        if (restaurantPersistencePort.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        if (categoryId != null && !categoryPersistencePort.existsById(categoryId)) {
            throw new DomainException("No existe una categoría con id " + categoryId);
        }
        return dishPersistencePort.findActiveByRestaurant(restaurantId, categoryId, page, size);
    }

    @Override
    public void saveDish(DishModel dish) {
        validateRequiredFields(dish);
        validatePrice(dish.getPrice());

        RestaurantModel restaurant = restaurantPersistencePort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(dish.getRestaurantId()));
        validateOwnership(restaurant);

        if (!categoryPersistencePort.existsById(dish.getCategoryId())) {
            throw new DomainException("No existe una categoría con id " + dish.getCategoryId());
        }

        dish.setActive(true);
        dishPersistencePort.saveDish(dish);
    }

    @Override
    public void updateDish(Long dishId, Integer price, String description) {
        if (price == null && description == null) {
            throw new DomainException("Debes enviar al menos el precio o la descripción del plato");
        }
        if (price != null) {
            validatePrice(price);
        }
        if (description != null && description.trim().isEmpty()) {
            throw new DomainException("La descripción no puede estar vacía");
        }

        DishModel dish = findDishOwnedByCurrentUser(dishId);

        if (price != null) {
            dish.setPrice(price);
        }
        if (description != null) {
            dish.setDescription(description);
        }
        dishPersistencePort.saveDish(dish);
    }

    @Override
    public void changeDishStatus(Long dishId, Boolean active) {
        if (active == null) {
            throw new DomainException("El campo active es obligatorio (true o false)");
        }
        DishModel dish = findDishOwnedByCurrentUser(dishId);
        dish.setActive(active);
        dishPersistencePort.saveDish(dish);
    }

    /** Busca el plato y verifica que pertenezca a un restaurante del usuario autenticado. */
    private DishModel findDishOwnedByCurrentUser(Long dishId) {
        DishModel dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(dishId));
        RestaurantModel restaurant = restaurantPersistencePort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(dish.getRestaurantId()));
        validateOwnership(restaurant);
        return dish;
    }

    private void validateOwnership(RestaurantModel restaurant) {
        Long currentUserId = currentUserPort.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(restaurant.getOwnerId())) {
            throw new NotRestaurantOwnerException();
        }
    }

    private void validateRequiredFields(DishModel dish) {
        requireText(dish.getName(), "nombre");
        requireText(dish.getDescription(), "descripción");
        requireText(dish.getImageUrl(), "url de la imagen");
        if (dish.getPrice() == null) {
            throw new DomainException("El campo precio es obligatorio");
        }
        if (dish.getCategoryId() == null) {
            throw new DomainException("El campo categoría es obligatorio");
        }
        if (dish.getRestaurantId() == null) {
            throw new DomainException("El campo restaurante es obligatorio");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException("El campo " + fieldName + " es obligatorio");
        }
    }

    private void validatePrice(Integer price) {
        if (price <= 0) {
            throw new DomainException("El precio debe ser un número entero positivo mayor a 0");
        }
    }
}
