package com.pragma.powerup.domain.usecase;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishUseCaseTest {

    private static final Long OWNER_ID = 2L;
    private static final Long RESTAURANT_ID = 10L;

    @Mock
    private IDishPersistencePort dishPersistencePort;
    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;
    @Mock
    private ICategoryPersistencePort categoryPersistencePort;
    @Mock
    private ICurrentUserPort currentUserPort;
    @InjectMocks
    private DishUseCase dishUseCase;

    private DishModel dish;

    @BeforeEach
    void setUp() {
        dish = new DishModel(null, "Bandeja paisa", 32000, "Frijoles, arroz y carne",
                "https://img.com/bandeja.jpg", 2L, RESTAURANT_ID, null);
    }

    private void restaurantOwnedBy(Long ownerId) {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(
                new RestaurantModel(RESTAURANT_ID, "Sabor", "900123456", "Calle 1", "+573001112233",
                        "https://logo.png", ownerId)));
    }

    @Test
    void saveDish_ownerOfRestaurant_savesDishActiveByDefault() {
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);
        when(categoryPersistencePort.existsById(2L)).thenReturn(true);

        dishUseCase.saveDish(dish);

        ArgumentCaptor<DishModel> captor = ArgumentCaptor.forClass(DishModel.class);
        verify(dishPersistencePort).saveDish(captor.capture());
        assertTrue(captor.getValue().getActive());
        assertEquals(RESTAURANT_ID, captor.getValue().getRestaurantId());
    }

    @Test
    void saveDish_activeSentAsFalse_isStillCreatedActive() {
        dish.setActive(false);
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);
        when(categoryPersistencePort.existsById(2L)).thenReturn(true);

        dishUseCase.saveDish(dish);

        verify(dishPersistencePort).saveDish(dish);
        assertTrue(dish.getActive());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -32000})
    void saveDish_priceZeroOrNegative_throwsDomainException(int price) {
        dish.setPrice(price);

        DomainException ex = assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));

        assertEquals("El precio debe ser un número entero positivo mayor a 0", ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_priceOfOne_isAccepted() {
        dish.setPrice(1);
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);
        when(categoryPersistencePort.existsById(2L)).thenReturn(true);

        dishUseCase.saveDish(dish);

        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void saveDish_missingPrice_throwsDomainException() {
        dish.setPrice(null);
        assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));
    }

    @Test
    void saveDish_missingName_throwsDomainException() {
        dish.setName(" ");
        assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));
    }

    @Test
    void saveDish_missingDescription_throwsDomainException() {
        dish.setDescription(null);
        assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));
    }

    @Test
    void saveDish_missingImageUrl_throwsDomainException() {
        dish.setImageUrl("");
        assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));
    }

    @Test
    void saveDish_missingCategory_throwsDomainException() {
        dish.setCategoryId(null);
        assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));
    }

    @Test
    void saveDish_missingRestaurant_throwsDomainException() {
        dish.setRestaurantId(null);
        assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));
    }

    @Test
    void saveDish_restaurantDoesNotExist_throwsRestaurantNotFound() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> dishUseCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_ownerOfAnotherRestaurant_throwsNotRestaurantOwner() {
        restaurantOwnedBy(99L);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);

        assertThrows(NotRestaurantOwnerException.class, () -> dishUseCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_noAuthenticatedUser_throwsNotRestaurantOwner() {
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(null);

        assertThrows(NotRestaurantOwnerException.class, () -> dishUseCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_categoryDoesNotExist_throwsDomainException() {
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);
        when(categoryPersistencePort.existsById(2L)).thenReturn(false);

        DomainException ex = assertThrows(DomainException.class, () -> dishUseCase.saveDish(dish));

        assertEquals("No existe una categoría con id 2", ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    // ---------------- HU4: modificar plato ----------------

    private DishModel existingDish() {
        return new DishModel(5L, "Bandeja paisa", 32000, "Descripción vieja",
                "https://img.com/bandeja.jpg", 2L, RESTAURANT_ID, true);
    }

    private void existingDishOfOwner() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.of(existingDish()));
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);
    }

    @Test
    void updateDish_priceAndDescription_updatesOnlyThoseFields() {
        existingDishOfOwner();

        dishUseCase.updateDish(5L, 35000, "Descripción nueva");

        ArgumentCaptor<DishModel> captor = ArgumentCaptor.forClass(DishModel.class);
        verify(dishPersistencePort).saveDish(captor.capture());
        DishModel saved = captor.getValue();
        assertEquals(35000, saved.getPrice());
        assertEquals("Descripción nueva", saved.getDescription());
        assertEquals("Bandeja paisa", saved.getName());
        assertEquals(2L, saved.getCategoryId());
        assertEquals(RESTAURANT_ID, saved.getRestaurantId());
        assertTrue(saved.getActive());
    }

    @Test
    void updateDish_onlyPrice_keepsDescription() {
        existingDishOfOwner();

        dishUseCase.updateDish(5L, 40000, null);

        ArgumentCaptor<DishModel> captor = ArgumentCaptor.forClass(DishModel.class);
        verify(dishPersistencePort).saveDish(captor.capture());
        assertEquals(40000, captor.getValue().getPrice());
        assertEquals("Descripción vieja", captor.getValue().getDescription());
    }

    @Test
    void updateDish_onlyDescription_keepsPrice() {
        existingDishOfOwner();

        dishUseCase.updateDish(5L, null, "Solo cambia la descripción");

        ArgumentCaptor<DishModel> captor = ArgumentCaptor.forClass(DishModel.class);
        verify(dishPersistencePort).saveDish(captor.capture());
        assertEquals(32000, captor.getValue().getPrice());
        assertEquals("Solo cambia la descripción", captor.getValue().getDescription());
    }

    @Test
    void updateDish_noFields_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class, () -> dishUseCase.updateDish(5L, null, null));

        assertEquals("Debes enviar al menos el precio o la descripción del plato", ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -500})
    void updateDish_priceZeroOrNegative_throwsDomainException(int price) {
        assertThrows(DomainException.class, () -> dishUseCase.updateDish(5L, price, "ok"));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDish_blankDescription_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class, () -> dishUseCase.updateDish(5L, 1000, "   "));

        assertEquals("La descripción no puede estar vacía", ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDish_dishDoesNotExist_throwsDishNotFound() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.empty());

        assertThrows(DishNotFoundException.class, () -> dishUseCase.updateDish(5L, 1000, null));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDish_restaurantOfDishMissing_throwsRestaurantNotFound() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.of(existingDish()));
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> dishUseCase.updateDish(5L, 1000, null));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDish_dishOfAnotherOwnersRestaurant_throwsNotRestaurantOwner() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.of(existingDish()));
        restaurantOwnedBy(99L);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);

        assertThrows(NotRestaurantOwnerException.class, () -> dishUseCase.updateDish(5L, 1000, null));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    // ---------------- HU7: habilitar / deshabilitar plato ----------------

    @Test
    void changeDishStatus_disable_savesInactiveAndKeepsOtherFields() {
        existingDishOfOwner();

        dishUseCase.changeDishStatus(5L, false);

        ArgumentCaptor<DishModel> captor = ArgumentCaptor.forClass(DishModel.class);
        verify(dishPersistencePort).saveDish(captor.capture());
        DishModel saved = captor.getValue();
        assertEquals(false, saved.getActive());
        assertEquals("Bandeja paisa", saved.getName());
        assertEquals(32000, saved.getPrice());
        assertEquals(RESTAURANT_ID, saved.getRestaurantId());
    }

    @Test
    void changeDishStatus_enable_savesActive() {
        DishModel inactive = existingDish();
        inactive.setActive(false);
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.of(inactive));
        restaurantOwnedBy(OWNER_ID);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);

        dishUseCase.changeDishStatus(5L, true);

        ArgumentCaptor<DishModel> captor = ArgumentCaptor.forClass(DishModel.class);
        verify(dishPersistencePort).saveDish(captor.capture());
        assertTrue(captor.getValue().getActive());
    }

    @Test
    void changeDishStatus_nullActive_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class, () -> dishUseCase.changeDishStatus(5L, null));

        assertEquals("El campo active es obligatorio (true o false)", ex.getMessage());
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void changeDishStatus_dishDoesNotExist_throwsDishNotFound() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.empty());

        assertThrows(DishNotFoundException.class, () -> dishUseCase.changeDishStatus(5L, false));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void changeDishStatus_restaurantMissing_throwsRestaurantNotFound() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.of(existingDish()));
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> dishUseCase.changeDishStatus(5L, false));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void changeDishStatus_dishOfAnotherOwnersRestaurant_throwsNotRestaurantOwner() {
        when(dishPersistencePort.findById(5L)).thenReturn(Optional.of(existingDish()));
        restaurantOwnedBy(99L);
        when(currentUserPort.getCurrentUserId()).thenReturn(OWNER_ID);

        assertThrows(NotRestaurantOwnerException.class, () -> dishUseCase.changeDishStatus(5L, false));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    // ---------------- HU10: listar platos de un restaurante ----------------

    private PageModel<DishModel> onePage() {
        return new PageModel<>(Collections.singletonList(dish), 0, 10, 1L, 1);
    }

    private void restaurantExists() {
        restaurantOwnedBy(OWNER_ID);
    }

    @Test
    void listDishes_withoutCategory_returnsActiveDishesOfRestaurant() {
        restaurantExists();
        when(dishPersistencePort.findActiveByRestaurant(RESTAURANT_ID, null, 0, 10)).thenReturn(onePage());

        PageModel<DishModel> result = dishUseCase.listDishes(RESTAURANT_ID, null, 0, 10);

        assertEquals(1, result.getContent().size());
        verify(categoryPersistencePort, never()).existsById(anyLong());
    }

    @Test
    void listDishes_withExistingCategory_filtersByCategory() {
        restaurantExists();
        when(categoryPersistencePort.existsById(2L)).thenReturn(true);
        when(dishPersistencePort.findActiveByRestaurant(RESTAURANT_ID, 2L, 1, 5)).thenReturn(onePage());

        assertEquals(1, dishUseCase.listDishes(RESTAURANT_ID, 2L, 1, 5).getContent().size());
    }

    @Test
    void listDishes_unknownRestaurant_throwsRestaurantNotFound() {
        when(restaurantPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> dishUseCase.listDishes(99L, null, 0, 10));
        verify(dishPersistencePort, never()).findActiveByRestaurant(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void listDishes_unknownCategory_throwsDomainException() {
        restaurantExists();
        when(categoryPersistencePort.existsById(77L)).thenReturn(false);

        assertThrows(DomainException.class, () -> dishUseCase.listDishes(RESTAURANT_ID, 77L, 0, 10));
        verify(dishPersistencePort, never()).findActiveByRestaurant(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void listDishes_nullRestaurant_throwsDomainException() {
        assertThrows(DomainException.class, () -> dishUseCase.listDishes(null, null, 0, 10));
    }

    @Test
    void listDishes_negativePage_throwsDomainException() {
        assertThrows(DomainException.class, () -> dishUseCase.listDishes(RESTAURANT_ID, null, -1, 10));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3, 101})
    void listDishes_sizeOutOfRange_throwsDomainException(int size) {
        assertThrows(DomainException.class, () -> dishUseCase.listDishes(RESTAURANT_ID, null, 0, size));
        verify(dishPersistencePort, never()).findActiveByRestaurant(anyLong(), any(), anyInt(), anyInt());
    }
}
