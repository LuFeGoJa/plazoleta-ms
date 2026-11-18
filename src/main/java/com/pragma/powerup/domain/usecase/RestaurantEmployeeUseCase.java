package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantEmployeeServicePort;
import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.EmployeeAlreadyAssignedException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.RoleNames;
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.ICurrentUserPort;
import com.pragma.powerup.domain.spi.IRestaurantEmployeePersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;

public class RestaurantEmployeeUseCase implements IRestaurantEmployeeServicePort {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IRestaurantEmployeePersistencePort employeePersistencePort;
    private final IUserClientPort userClientPort;
    private final ICurrentUserPort currentUserPort;

    public RestaurantEmployeeUseCase(IRestaurantPersistencePort restaurantPersistencePort,
                                     IRestaurantEmployeePersistencePort employeePersistencePort,
                                     IUserClientPort userClientPort,
                                     ICurrentUserPort currentUserPort) {
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.employeePersistencePort = employeePersistencePort;
        this.userClientPort = userClientPort;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public void assignEmployee(Long restaurantId, Long employeeId) {
        if (restaurantId == null || employeeId == null) {
            throw new DomainException("El restaurante y el empleado son obligatorios");
        }
        RestaurantModel restaurant = restaurantPersistencePort.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        Long currentUserId = currentUserPort.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(restaurant.getOwnerId())) {
            throw new NotRestaurantOwnerException(
                    "Solo el propietario del restaurante puede vincular empleados");
        }

        UserInfoModel employee = userClientPort.findUserById(employeeId)
                .orElseThrow(() -> new DomainException("No existe un usuario con id " + employeeId));
        if (!RoleNames.EMPLOYEE.equals(employee.getRole())) {
            throw new DomainException("El usuario " + employeeId + " no tiene el rol de empleado");
        }
        if (employeePersistencePort.existsByEmployeeId(employeeId)) {
            throw new EmployeeAlreadyAssignedException(employeeId);
        }
        employeePersistencePort.save(restaurantId, employeeId);
    }
}
