package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.RoleNames;
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;

import java.util.regex.Pattern;

public class RestaurantUseCase implements IRestaurantServicePort {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d+$");
    private static final int MAX_PHONE_LENGTH = 13;
    private static final int MAX_PAGE_SIZE = 100;

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserClientPort userClientPort;

    public RestaurantUseCase(IRestaurantPersistencePort restaurantPersistencePort,
                             IUserClientPort userClientPort) {
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.userClientPort = userClientPort;
    }

    @Override
    public void saveRestaurant(RestaurantModel restaurant) {
        validateRequiredFields(restaurant);
        validateFormats(restaurant);
        validateOwner(restaurant.getOwnerId());

        if (restaurantPersistencePort.existsByNit(restaurant.getNit())) {
            throw new RestaurantAlreadyExistsException(
                    "Ya existe un restaurante con el NIT " + restaurant.getNit());
        }
        restaurantPersistencePort.saveRestaurant(restaurant);
    }

    @Override
    public PageModel<RestaurantModel> listRestaurants(int page, int size) {
        if (page < 0) {
            throw new DomainException("El número de página no puede ser negativo");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new DomainException("La cantidad de elementos por página debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        return restaurantPersistencePort.findAllOrderedByName(page, size);
    }

    private void validateRequiredFields(RestaurantModel restaurant) {
        requireText(restaurant.getName(), "nombre");
        requireText(restaurant.getNit(), "NIT");
        requireText(restaurant.getAddress(), "dirección");
        requireText(restaurant.getPhone(), "teléfono");
        requireText(restaurant.getLogoUrl(), "url del logo");
        if (restaurant.getOwnerId() == null) {
            throw new DomainException("El campo id del propietario es obligatorio");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException("El campo " + fieldName + " es obligatorio");
        }
    }

    private void validateFormats(RestaurantModel restaurant) {
        if (!NUMERIC_PATTERN.matcher(restaurant.getNit()).matches()) {
            throw new DomainException("El NIT debe ser únicamente numérico");
        }
        if (restaurant.getPhone().length() > MAX_PHONE_LENGTH
                || !PHONE_PATTERN.matcher(restaurant.getPhone()).matches()) {
            throw new DomainException(
                    "El teléfono debe tener máximo 13 caracteres y solo puede contener números y el símbolo +");
        }
        if (NUMERIC_PATTERN.matcher(restaurant.getName().trim()).matches()) {
            throw new DomainException("El nombre del restaurante no puede contener solo números");
        }
    }

    private void validateOwner(Long ownerId) {
        UserInfoModel owner = userClientPort.findUserById(ownerId)
                .orElseThrow(() -> new DomainException("No existe un usuario con id " + ownerId));
        if (!RoleNames.OWNER.equals(owner.getRole())) {
            throw new DomainException("El usuario con id " + ownerId + " no tiene el rol de propietario");
        }
    }
}
