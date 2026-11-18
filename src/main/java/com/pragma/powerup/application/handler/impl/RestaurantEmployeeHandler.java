package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.EmployeeAssignRequestDto;
import com.pragma.powerup.application.handler.IRestaurantEmployeeHandler;
import com.pragma.powerup.domain.api.IRestaurantEmployeeServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantEmployeeHandler implements IRestaurantEmployeeHandler {

    private final IRestaurantEmployeeServicePort employeeServicePort;

    @Override
    public void assignEmployee(Long restaurantId, EmployeeAssignRequestDto request) {
        employeeServicePort.assignEmployee(restaurantId, request.getEmployeeId());
    }
}
