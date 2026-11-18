package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.spi.IRestaurantEmployeePersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEmployeeEntity;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantEmployeeRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RestaurantEmployeeJpaAdapter implements IRestaurantEmployeePersistencePort {

    private final IRestaurantEmployeeRepository employeeRepository;

    @Override
    public void save(Long restaurantId, Long employeeId) {
        employeeRepository.save(new RestaurantEmployeeEntity(null, restaurantId, employeeId));
    }

    @Override
    public boolean existsByEmployeeId(Long employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }

    @Override
    public Optional<Long> findRestaurantIdByEmployeeId(Long employeeId) {
        return employeeRepository.findByEmployeeId(employeeId).map(RestaurantEmployeeEntity::getRestaurantId);
    }
}
