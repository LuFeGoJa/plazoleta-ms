package com.pragma.powerup.domain.api;

public interface IRestaurantEmployeeServicePort {

    /** El propietario del restaurante vincula a un empleado (rol EMPLEADO) con su restaurante. */
    void assignEmployee(Long restaurantId, Long employeeId);
}
