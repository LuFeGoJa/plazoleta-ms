package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserServiceUnavailableException;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.RoleNames;
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantUseCaseTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;
    @Mock
    private IUserClientPort userClientPort;
    @InjectMocks
    private RestaurantUseCase restaurantUseCase;

    private RestaurantModel restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantModel(null, "Sabor Criollo 54", "900123456", "Calle 10 # 5-20",
                "+573005698325", "https://logos.com/sabor.png", 2L);
    }

    private void ownerExists() {
        when(userClientPort.findUserById(2L)).thenReturn(Optional.of(new UserInfoModel(2L, RoleNames.OWNER)));
    }

    @Test
    void saveRestaurant_validData_savesRestaurant() {
        ownerExists();
        when(restaurantPersistencePort.existsByNit("900123456")).thenReturn(false);

        restaurantUseCase.saveRestaurant(restaurant);

        verify(restaurantPersistencePort).saveRestaurant(restaurant);
    }

    @Test
    void saveRestaurant_nameWithNumbersAndLetters_isAccepted() {
        restaurant.setName("Burger 54");
        ownerExists();

        restaurantUseCase.saveRestaurant(restaurant);

        verify(restaurantPersistencePort).saveRestaurant(restaurant);
    }

    @Test
    void saveRestaurant_nameOnlyDigits_throwsDomainException() {
        restaurant.setName("12345");

        DomainException ex = assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals("El nombre del restaurante no puede contener solo números", ex.getMessage());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"90012345A", "900-123", "+900123", "900 123"})
    void saveRestaurant_nonNumericNit_throwsDomainException(String nit) {
        restaurant.setNit(nit);

        DomainException ex = assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals("El NIT debe ser únicamente numérico", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+5730056983251", "30056983251234", "+57300abc8325", "300-569-8325", "++573005698"})
    void saveRestaurant_invalidPhone_throwsDomainException(String phone) {
        restaurant.setPhone(phone);

        assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurant_phoneWithoutPlus_isAccepted() {
        restaurant.setPhone("3005698325");
        ownerExists();

        restaurantUseCase.saveRestaurant(restaurant);

        verify(restaurantPersistencePort).saveRestaurant(restaurant);
    }

    @Test
    void saveRestaurant_missingName_throwsDomainException() {
        restaurant.setName(" ");
        assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_missingNit_throwsDomainException() {
        restaurant.setNit(null);
        assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_missingAddress_throwsDomainException() {
        restaurant.setAddress("");
        assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_missingPhone_throwsDomainException() {
        restaurant.setPhone(null);
        assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_missingLogoUrl_throwsDomainException() {
        restaurant.setLogoUrl(" ");
        assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_missingOwnerId_throwsDomainException() {
        restaurant.setOwnerId(null);

        DomainException ex = assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals("El campo id del propietario es obligatorio", ex.getMessage());
    }

    @Test
    void saveRestaurant_ownerDoesNotExist_throwsDomainException() {
        when(userClientPort.findUserById(2L)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals("No existe un usuario con id 2", ex.getMessage());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurant_userIsNotOwner_throwsDomainException() {
        when(userClientPort.findUserById(2L)).thenReturn(Optional.of(new UserInfoModel(2L, RoleNames.CLIENT)));

        DomainException ex = assertThrows(DomainException.class, () -> restaurantUseCase.saveRestaurant(restaurant));

        assertEquals("El usuario con id 2 no tiene el rol de propietario", ex.getMessage());
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurant_usersServiceDown_propagatesUnavailable() {
        when(userClientPort.findUserById(2L)).thenThrow(new UserServiceUnavailableException());

        assertThrows(UserServiceUnavailableException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurant_nitAlreadyRegistered_throwsRestaurantAlreadyExists() {
        ownerExists();
        when(restaurantPersistencePort.existsByNit("900123456")).thenReturn(true);

        assertThrows(RestaurantAlreadyExistsException.class, () -> restaurantUseCase.saveRestaurant(restaurant));
        verify(restaurantPersistencePort, never()).saveRestaurant(any());
    }

    // ---------------- HU9: listar restaurantes ----------------

    @Test
    void listRestaurants_validPaging_returnsPageFromPersistence() {
        PageModel<RestaurantModel> page = new PageModel<>(Collections.singletonList(restaurant), 0, 10, 1L, 1);
        when(restaurantPersistencePort.findAllOrderedByName(0, 10)).thenReturn(page);

        PageModel<RestaurantModel> result = restaurantUseCase.listRestaurants(0, 10);

        assertEquals(page, result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listRestaurants_maxPageSize_isAccepted() {
        PageModel<RestaurantModel> empty = new PageModel<>(Collections.emptyList(), 2, 100, 0L, 0);
        when(restaurantPersistencePort.findAllOrderedByName(2, 100)).thenReturn(empty);

        assertEquals(empty, restaurantUseCase.listRestaurants(2, 100));
    }

    @Test
    void listRestaurants_negativePage_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class, () -> restaurantUseCase.listRestaurants(-1, 10));

        assertEquals("El número de página no puede ser negativo", ex.getMessage());
        verify(restaurantPersistencePort, never()).findAllOrderedByName(anyInt(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, 101, 1000})
    void listRestaurants_sizeOutOfRange_throwsDomainException(int size) {
        assertThrows(DomainException.class, () -> restaurantUseCase.listRestaurants(0, size));
        verify(restaurantPersistencePort, never()).findAllOrderedByName(anyInt(), anyInt());
    }
}
