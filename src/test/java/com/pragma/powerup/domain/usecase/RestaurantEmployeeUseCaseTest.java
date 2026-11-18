package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.EmployeeAlreadyAssignedException;
import com.pragma.powerup.domain.exception.NotRestaurantOwnerException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.RoleNames;
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.ICurrentUserPort;
import com.pragma.powerup.domain.spi.IRestaurantEmployeePersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantEmployeeUseCaseTest {

    private static final Long OWNER_ID = 2L;
    private static final Long EMPLOYEE_ID = 3L;
    private static final Long RESTAURANT_ID = 1L;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;
    @Mock
    private IRestaurantEmployeePersistencePort employeePersistencePort;
    @Mock
    private IUserClientPort userClientPort;
    @Mock
    private ICurrentUserPort currentUserPort;
    @InjectMocks
    private RestaurantEmployeeUseCase useCase;

    private RestaurantModel restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantModel();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setOwnerId(OWNER_ID);
    }

    private void ownerCalls() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);
    }

    @Test
    void assignEmployee_valid_savesLink() {
        ownerCalls();
        when(userClientPort.findUserById(EMPLOYEE_ID))
                .thenReturn(Optional.of(new UserInfoModel(EMPLOYEE_ID, RoleNames.EMPLOYEE)));
        when(employeePersistencePort.existsByEmployeeId(EMPLOYEE_ID)).thenReturn(false);

        useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID);

        verify(employeePersistencePort).save(RESTAURANT_ID, EMPLOYEE_ID);
    }

    @Test
    void assignEmployee_nullIds_throwsDomainException() {
        assertThrows(DomainException.class, () -> useCase.assignEmployee(null, EMPLOYEE_ID));
        assertThrows(DomainException.class, () -> useCase.assignEmployee(RESTAURANT_ID, null));
    }

    @Test
    void assignEmployee_unknownRestaurant_throwsRestaurantNotFound() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID));
        verify(employeePersistencePort, never()).save(anyLong(), anyLong());
    }

    @Test
    void assignEmployee_callerIsNotTheOwner_throwsNotRestaurantOwner() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(currentUserPort.getCurrentUserId()).thenReturn(99L);

        assertThrows(NotRestaurantOwnerException.class, () -> useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID));
        verify(employeePersistencePort, never()).save(anyLong(), anyLong());
    }

    @Test
    void assignEmployee_unknownUser_throwsDomainException() {
        ownerCalls();
        when(userClientPort.findUserById(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID));
        verify(employeePersistencePort, never()).save(anyLong(), anyLong());
    }

    @Test
    void assignEmployee_userIsNotEmployee_throwsDomainException() {
        ownerCalls();
        when(userClientPort.findUserById(EMPLOYEE_ID))
                .thenReturn(Optional.of(new UserInfoModel(EMPLOYEE_ID, RoleNames.CLIENT)));

        assertThrows(DomainException.class, () -> useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID));
        verify(employeePersistencePort, never()).save(anyLong(), anyLong());
    }

    @Test
    void assignEmployee_alreadyLinked_throwsEmployeeAlreadyAssigned() {
        ownerCalls();
        when(userClientPort.findUserById(EMPLOYEE_ID))
                .thenReturn(Optional.of(new UserInfoModel(EMPLOYEE_ID, RoleNames.EMPLOYEE)));
        when(employeePersistencePort.existsByEmployeeId(EMPLOYEE_ID)).thenReturn(true);

        assertThrows(EmployeeAlreadyAssignedException.class,
                () -> useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID));
        verify(employeePersistencePort, never()).save(anyLong(), anyLong());
    }

    @Test
    void assignEmployee_usersServiceDown_propagatesUnavailable() {
        ownerCalls();
        when(userClientPort.findUserById(EMPLOYEE_ID)).thenThrow(new UserServiceUnavailableException());

        assertThrows(UserServiceUnavailableException.class,
                () -> useCase.assignEmployee(RESTAURANT_ID, EMPLOYEE_ID));
    }
}
