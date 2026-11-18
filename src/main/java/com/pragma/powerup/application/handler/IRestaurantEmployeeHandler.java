package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.EmployeeAssignRequestDto;

public interface IRestaurantEmployeeHandler {

    void assignEmployee(Long restaurantId, EmployeeAssignRequestDto request);
}
