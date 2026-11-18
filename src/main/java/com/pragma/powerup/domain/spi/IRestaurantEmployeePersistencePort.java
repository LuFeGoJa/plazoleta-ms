package com.pragma.powerup.domain.spi;

import java.util.Optional;

public interface IRestaurantEmployeePersistencePort {

    void save(Long restaurantId, Long employeeId);

    boolean existsByEmployeeId(Long employeeId);

    /** Restaurante donde trabaja el empleado, o vacío si no está vinculado. */
    Optional<Long> findRestaurantIdByEmployeeId(Long employeeId);
}
