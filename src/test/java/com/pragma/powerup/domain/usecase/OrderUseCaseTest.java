package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ActiveOrderExistsException;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.EmployeeWithoutRestaurantException;
import com.pragma.powerup.domain.exception.InvalidSecurityPinException;
import com.pragma.powerup.domain.exception.NotificationUnavailableException;
import com.pragma.powerup.domain.exception.OrderNotFoundException;
import com.pragma.powerup.domain.exception.TraceUnavailableException;
import com.pragma.powerup.domain.exception.OrderNotInRestaurantException;
import com.pragma.powerup.domain.exception.OrderNotOfClientException;
import com.pragma.powerup.domain.exception.OrderStatusConflictException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.OrderTraceModel;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.RoleNames;
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.ICurrentUserPort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.domain.spi.IRestaurantEmployeePersistencePort;
import com.pragma.powerup.domain.spi.ISecurityPinPort;
import com.pragma.powerup.domain.spi.ITracePort;
import com.pragma.powerup.domain.spi.IUserClientPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    private static final Long CLIENT_ID = 4L;
    private static final Long RESTAURANT_ID = 1L;

    @Mock
    private IOrderPersistencePort orderPersistencePort;
    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;
    @Mock
    private IDishPersistencePort dishPersistencePort;
    @Mock
    private ICurrentUserPort currentUserPort;
    @Mock
    private IRestaurantEmployeePersistencePort employeePersistencePort;
    @Mock
    private IUserClientPort userClientPort;
    @Mock
    private INotificationPort notificationPort;
    @Mock
    private ISecurityPinPort securityPinPort;
    @Mock
    private ITracePort tracePort;
    @InjectMocks
    private OrderUseCase orderUseCase;

    private OrderModel order;

    @BeforeEach
    void setUp() {
        order = new OrderModel();
        order.setRestaurantId(RESTAURANT_ID);
        order.setDishes(new ArrayList<>(Arrays.asList(new OrderDishModel(1L, 2), new OrderDishModel(2L, 1))));
    }

    private void clientLoggedIn() {
        when(currentUserPort.getCurrentUserId()).thenReturn(CLIENT_ID);
    }

    private void restaurantExists() {
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(new RestaurantModel()));
    }

    private void dish(Long id, Long restaurantId, Boolean active) {
        DishModel dish = new DishModel();
        dish.setId(id);
        dish.setRestaurantId(restaurantId);
        dish.setActive(active);
        when(dishPersistencePort.findById(id)).thenReturn(Optional.of(dish));
    }

    @Test
    void createOrder_valid_savesPendingOrderForCurrentClient() {
        clientLoggedIn();
        restaurantExists();
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(false);
        dish(1L, RESTAURANT_ID, true);
        dish(2L, RESTAURANT_ID, true);
        when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenAnswer(i -> {
            OrderModel saved = i.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        OrderModel result = orderUseCase.createOrder(order);

        ArgumentCaptor<OrderModel> captor = ArgumentCaptor.forClass(OrderModel.class);
        verify(orderPersistencePort).saveOrder(captor.capture());
        OrderModel saved = captor.getValue();
        assertEquals(OrderStatus.PENDIENTE, saved.getStatus());
        assertEquals(CLIENT_ID, saved.getClientId());
        assertEquals(RESTAURANT_ID, saved.getRestaurantId());
        assertNotNull(saved.getDate());
        assertNull(saved.getChefId());
        assertEquals(2, saved.getDishes().size());
        assertEquals(50L, result.getId());
    }

    @Test
    void createOrder_checksActiveOrderAgainstPendingPreparingAndReady() {
        clientLoggedIn();
        restaurantExists();
        dish(1L, RESTAURANT_ID, true);
        dish(2L, RESTAURANT_ID, true);
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(false);
        when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenAnswer(i -> i.getArgument(0));

        orderUseCase.createOrder(order);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<OrderStatus>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(orderPersistencePort).existsByClientIdAndStatusIn(eq(CLIENT_ID), captor.capture());
        assertTrue(captor.getValue().containsAll(
                Arrays.asList(OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION, OrderStatus.LISTO)));
        assertEquals(3, captor.getValue().size());
    }

    @Test
    void createOrder_clientWithOrderInProgress_throwsActiveOrderExists() {
        clientLoggedIn();
        restaurantExists();
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(true);

        assertThrows(ActiveOrderExistsException.class, () -> orderUseCase.createOrder(order));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_unknownRestaurant_throwsRestaurantNotFound() {
        clientLoggedIn();
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> orderUseCase.createOrder(order));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_unknownDish_throwsDishNotFound() {
        clientLoggedIn();
        restaurantExists();
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(false);
        dish(1L, RESTAURANT_ID, true);
        when(dishPersistencePort.findById(2L)).thenReturn(Optional.empty());

        assertThrows(DishNotFoundException.class, () -> orderUseCase.createOrder(order));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_dishFromAnotherRestaurant_throwsDomainException() {
        clientLoggedIn();
        restaurantExists();
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(false);
        dish(1L, 99L, true);

        DomainException ex = assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));

        assertTrue(ex.getMessage().contains("no pertenece al restaurante"));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_inactiveDish_throwsDomainException() {
        clientLoggedIn();
        restaurantExists();
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(false);
        dish(1L, RESTAURANT_ID, false);

        DomainException ex = assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));

        assertTrue(ex.getMessage().contains("no está disponible"));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_noAuthenticatedUser_throwsDomainException() {
        when(currentUserPort.getCurrentUserId()).thenReturn(null);

        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_missingRestaurant_throwsDomainException() {
        clientLoggedIn();
        order.setRestaurantId(null);

        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));
    }

    @Test
    void createOrder_nullOrEmptyDishes_throwsDomainException() {
        clientLoggedIn();
        order.setDishes(Collections.emptyList());
        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));

        order.setDishes(null);
        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));
        verify(restaurantPersistencePort, never()).findById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void createOrder_invalidQuantity_throwsDomainException(int quantity) {
        clientLoggedIn();
        order.setDishes(Collections.singletonList(new OrderDishModel(1L, quantity)));

        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void createOrder_nullQuantityOrDishId_throwsDomainException() {
        clientLoggedIn();
        order.setDishes(Collections.singletonList(new OrderDishModel(1L, null)));
        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));

        order.setDishes(Collections.singletonList(new OrderDishModel(null, 1)));
        assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));
    }

    @Test
    void createOrder_repeatedDish_throwsDomainException() {
        clientLoggedIn();
        order.setDishes(Arrays.asList(new OrderDishModel(1L, 1), new OrderDishModel(1L, 2)));

        DomainException ex = assertThrows(DomainException.class, () -> orderUseCase.createOrder(order));

        assertTrue(ex.getMessage().contains("repetido"));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    // ---------------- HU12: listar pedidos por estado ----------------

    private static final Long EMPLOYEE_ID = 3L;

    @Test
    void listOrdersByStatus_employeeOfRestaurant_listsOnlyThatRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
        when(employeePersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.of(RESTAURANT_ID));
        PageModel<OrderModel> page = new PageModel<>(Collections.singletonList(order), 0, 10, 1L, 1);
        when(orderPersistencePort.findByRestaurantAndStatus(RESTAURANT_ID, OrderStatus.PENDIENTE, 0, 10))
                .thenReturn(page);

        PageModel<OrderModel> result = orderUseCase.listOrdersByStatus(OrderStatus.PENDIENTE, 0, 10);

        assertEquals(page, result);
    }

    @Test
    void listOrdersByStatus_employeeWithoutRestaurant_throwsEmployeeWithoutRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
        when(employeePersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(EmployeeWithoutRestaurantException.class,
                () -> orderUseCase.listOrdersByStatus(OrderStatus.PENDIENTE, 0, 10));
        verify(orderPersistencePort, never()).findByRestaurantAndStatus(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void listOrdersByStatus_noAuthenticatedUser_throwsEmployeeWithoutRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(null);

        assertThrows(EmployeeWithoutRestaurantException.class,
                () -> orderUseCase.listOrdersByStatus(OrderStatus.LISTO, 0, 10));
    }

    @Test
    void listOrdersByStatus_nullStatus_throwsDomainException() {
        assertThrows(DomainException.class, () -> orderUseCase.listOrdersByStatus(null, 0, 10));
    }

    @Test
    void listOrdersByStatus_negativePage_throwsDomainException() {
        assertThrows(DomainException.class, () -> orderUseCase.listOrdersByStatus(OrderStatus.PENDIENTE, -1, 10));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -2, 101})
    void listOrdersByStatus_sizeOutOfRange_throwsDomainException(int size) {
        assertThrows(DomainException.class, () -> orderUseCase.listOrdersByStatus(OrderStatus.PENDIENTE, 0, size));
        verify(orderPersistencePort, never()).findByRestaurantAndStatus(anyLong(), any(), anyInt(), anyInt());
    }

    // ---------------- HU13: asignarse a un pedido ----------------

    private OrderModel orderInState(Long id, Long restaurantId, OrderStatus status) {
        OrderModel o = new OrderModel();
        o.setId(id);
        o.setRestaurantId(restaurantId);
        o.setStatus(status);
        o.setDishes(new ArrayList<>());
        return o;
    }

    private void employeeOfRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
        when(employeePersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.of(RESTAURANT_ID));
    }

    @Test
    void assignOrder_pendingOrderOfOwnRestaurant_movesToPreparationWithEmployee() {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(orderInState(10L, RESTAURANT_ID, OrderStatus.PENDIENTE)));
        OrderModel updated = orderInState(10L, RESTAURANT_ID, OrderStatus.EN_PREPARACION);
        updated.setChefId(EMPLOYEE_ID);
        when(orderPersistencePort.updateStatusAndChef(10L, OrderStatus.EN_PREPARACION, EMPLOYEE_ID))
                .thenReturn(updated);

        OrderModel result = orderUseCase.assignOrderToEmployee(10L);

        assertEquals(OrderStatus.EN_PREPARACION, result.getStatus());
        assertEquals(EMPLOYEE_ID, result.getChefId());
    }

    @Test
    void assignOrder_employeeWithoutRestaurant_throwsEmployeeWithoutRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
        when(employeePersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(EmployeeWithoutRestaurantException.class, () -> orderUseCase.assignOrderToEmployee(10L));
        verify(orderPersistencePort, never()).updateStatusAndChef(anyLong(), any(), any());
    }

    @Test
    void assignOrder_unknownOrder_throwsOrderNotFound() {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderUseCase.assignOrderToEmployee(99L));
        verify(orderPersistencePort, never()).updateStatusAndChef(anyLong(), any(), any());
    }

    @Test
    void assignOrder_orderOfAnotherRestaurant_throwsOrderNotInRestaurant() {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(orderInState(10L, 77L, OrderStatus.PENDIENTE)));

        assertThrows(OrderNotInRestaurantException.class, () -> orderUseCase.assignOrderToEmployee(10L));
        verify(orderPersistencePort, never()).updateStatusAndChef(anyLong(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"})
    void assignOrder_orderNotPending_throwsOrderStatusConflict(String status) {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(orderInState(10L, RESTAURANT_ID, OrderStatus.valueOf(status))));

        OrderStatusConflictException ex = assertThrows(OrderStatusConflictException.class,
                () -> orderUseCase.assignOrderToEmployee(10L));

        assertTrue(ex.getMessage().contains(status));
        verify(orderPersistencePort, never()).updateStatusAndChef(anyLong(), any(), any());
    }

    @Test
    void assignOrder_nullId_throwsDomainException() {
        assertThrows(DomainException.class, () -> orderUseCase.assignOrderToEmployee(null));
    }

    // ---------------- HU14: pedido listo y PIN ----------------

    private void clientWithPhone(String phone) {
        when(userClientPort.findUserById(CLIENT_ID))
                .thenReturn(Optional.of(new UserInfoModel(CLIENT_ID, RoleNames.CLIENT, phone)));
    }

    private OrderModel preparingOrder() {
        OrderModel o = orderInState(10L, RESTAURANT_ID, OrderStatus.EN_PREPARACION);
        o.setClientId(CLIENT_ID);
        when(orderPersistencePort.findById(10L)).thenReturn(Optional.of(o));
        return o;
    }

    @Test
    void markReady_preparingOrder_savesPinAndSendsItToClient() {
        employeeOfRestaurant();
        preparingOrder();
        clientWithPhone("+573001112233");
        when(securityPinPort.generate()).thenReturn("048213");
        OrderModel updated = orderInState(10L, RESTAURANT_ID, OrderStatus.LISTO);
        when(orderPersistencePort.updateStatusAndPin(10L, OrderStatus.LISTO, "048213")).thenReturn(updated);

        OrderModel result = orderUseCase.markOrderReady(10L);

        assertEquals(OrderStatus.LISTO, result.getStatus());
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        InOrder inOrder = inOrder(orderPersistencePort, notificationPort);
        inOrder.verify(orderPersistencePort).updateStatusAndPin(10L, OrderStatus.LISTO, "048213");
        inOrder.verify(notificationPort).sendSms(eq("+573001112233"), message.capture());
        assertTrue(message.getValue().contains("048213"));
        assertTrue(message.getValue().contains("#10"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDIENTE", "LISTO", "ENTREGADO", "CANCELADO"})
    void markReady_orderNotPreparing_throwsOrderStatusConflict(String status) {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(orderInState(10L, RESTAURANT_ID, OrderStatus.valueOf(status))));

        assertThrows(OrderStatusConflictException.class, () -> orderUseCase.markOrderReady(10L));
        verify(orderPersistencePort, never()).updateStatusAndPin(anyLong(), any(), any());
        verify(notificationPort, never()).sendSms(any(), any());
    }

    @Test
    void markReady_orderOfAnotherRestaurant_throwsOrderNotInRestaurant() {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(orderInState(10L, 77L, OrderStatus.EN_PREPARACION)));

        assertThrows(OrderNotInRestaurantException.class, () -> orderUseCase.markOrderReady(10L));
        verify(notificationPort, never()).sendSms(any(), any());
    }

    @Test
    void markReady_unknownOrder_throwsOrderNotFound() {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderUseCase.markOrderReady(99L));
    }

    @Test
    void markReady_employeeWithoutRestaurant_throwsEmployeeWithoutRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
        when(employeePersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(EmployeeWithoutRestaurantException.class, () -> orderUseCase.markOrderReady(10L));
    }

    @Test
    void markReady_nullId_throwsDomainException() {
        assertThrows(DomainException.class, () -> orderUseCase.markOrderReady(null));
    }

    @Test
    void markReady_clientNotFound_throwsDomainExceptionAndKeepsStatus() {
        employeeOfRestaurant();
        preparingOrder();
        when(userClientPort.findUserById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> orderUseCase.markOrderReady(10L));
        verify(orderPersistencePort, never()).updateStatusAndPin(anyLong(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void markReady_clientWithoutPhone_throwsDomainExceptionAndKeepsStatus(String phone) {
        employeeOfRestaurant();
        preparingOrder();
        clientWithPhone(phone);

        assertThrows(DomainException.class, () -> orderUseCase.markOrderReady(10L));
        verify(orderPersistencePort, never()).updateStatusAndPin(anyLong(), any(), any());
        verify(notificationPort, never()).sendSms(any(), any());
    }

    @Test
    void markReady_clientPhoneNull_throwsDomainException() {
        employeeOfRestaurant();
        preparingOrder();
        clientWithPhone(null);

        assertThrows(DomainException.class, () -> orderUseCase.markOrderReady(10L));
    }

    @Test
    void markReady_messageFails_propagatesSoTransactionRollsBack() {
        employeeOfRestaurant();
        preparingOrder();
        clientWithPhone("+573001112233");
        when(securityPinPort.generate()).thenReturn("111111");
        when(orderPersistencePort.updateStatusAndPin(10L, OrderStatus.LISTO, "111111"))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.LISTO));
        doThrow(new NotificationUnavailableException()).when(notificationPort).sendSms(any(), any());

        assertThrows(NotificationUnavailableException.class, () -> orderUseCase.markOrderReady(10L));
    }

    // ---------------- HU15: entregar pedido con PIN ----------------

    private void readyOrderWithPin(String pin) {
        OrderModel o = orderInState(10L, RESTAURANT_ID, OrderStatus.LISTO);
        o.setSecurityPin(pin);
        when(orderPersistencePort.findById(10L)).thenReturn(Optional.of(o));
    }

    @Test
    void deliver_correctPin_marksOrderDelivered() {
        employeeOfRestaurant();
        readyOrderWithPin("048213");
        when(orderPersistencePort.updateStatus(10L, OrderStatus.ENTREGADO))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.ENTREGADO));

        OrderModel result = orderUseCase.deliverOrder(10L, "048213");

        assertEquals(OrderStatus.ENTREGADO, result.getStatus());
    }

    @Test
    void deliver_pinWithSpaces_isTrimmed() {
        employeeOfRestaurant();
        readyOrderWithPin("048213");
        when(orderPersistencePort.updateStatus(10L, OrderStatus.ENTREGADO))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.ENTREGADO));

        assertEquals(OrderStatus.ENTREGADO, orderUseCase.deliverOrder(10L, " 048213 ").getStatus());
    }

    @Test
    void deliver_wrongPin_throwsInvalidPinAndKeepsStatus() {
        employeeOfRestaurant();
        readyOrderWithPin("048213");

        assertThrows(InvalidSecurityPinException.class, () -> orderUseCase.deliverOrder(10L, "999999"));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    @Test
    void deliver_orderWithoutStoredPin_throwsInvalidPin() {
        employeeOfRestaurant();
        readyOrderWithPin(null);

        assertThrows(InvalidSecurityPinException.class, () -> orderUseCase.deliverOrder(10L, "000000"));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDIENTE", "EN_PREPARACION", "ENTREGADO", "CANCELADO"})
    void deliver_orderNotReady_throwsOrderStatusConflict(String status) {
        employeeOfRestaurant();
        OrderModel o = orderInState(10L, RESTAURANT_ID, OrderStatus.valueOf(status));
        o.setSecurityPin("048213");
        when(orderPersistencePort.findById(10L)).thenReturn(Optional.of(o));

        assertThrows(OrderStatusConflictException.class, () -> orderUseCase.deliverOrder(10L, "048213"));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    @Test
    void deliver_orderOfAnotherRestaurant_throwsOrderNotInRestaurant() {
        employeeOfRestaurant();
        OrderModel o = orderInState(10L, 77L, OrderStatus.LISTO);
        o.setSecurityPin("048213");
        when(orderPersistencePort.findById(10L)).thenReturn(Optional.of(o));

        assertThrows(OrderNotInRestaurantException.class, () -> orderUseCase.deliverOrder(10L, "048213"));
    }

    @Test
    void deliver_unknownOrder_throwsOrderNotFound() {
        employeeOfRestaurant();
        when(orderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderUseCase.deliverOrder(99L, "048213"));
    }

    @Test
    void deliver_employeeWithoutRestaurant_throwsEmployeeWithoutRestaurant() {
        when(currentUserPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
        when(employeePersistencePort.findRestaurantIdByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(EmployeeWithoutRestaurantException.class, () -> orderUseCase.deliverOrder(10L, "048213"));
    }

    @Test
    void deliver_nullIdOrBlankPin_throwsDomainException() {
        assertThrows(DomainException.class, () -> orderUseCase.deliverOrder(null, "048213"));
        assertThrows(DomainException.class, () -> orderUseCase.deliverOrder(10L, null));
        assertThrows(DomainException.class, () -> orderUseCase.deliverOrder(10L, "   "));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    // ---------------- HU16: cancelar pedido ----------------

    private OrderModel clientOrder(Long id, Long clientId, OrderStatus status) {
        OrderModel o = orderInState(id, RESTAURANT_ID, status);
        o.setClientId(clientId);
        return o;
    }

    @Test
    void cancel_ownPendingOrder_marksCancelled() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.PENDIENTE)));
        when(orderPersistencePort.updateStatus(10L, OrderStatus.CANCELADO))
                .thenReturn(clientOrder(10L, CLIENT_ID, OrderStatus.CANCELADO));

        assertEquals(OrderStatus.CANCELADO, orderUseCase.cancelOrder(10L).getStatus());
    }

    @Test
    void cancel_orderInPreparation_throwsConflictWithExplanatoryMessage() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.EN_PREPARACION)));

        OrderStatusConflictException ex = assertThrows(OrderStatusConflictException.class,
                () -> orderUseCase.cancelOrder(10L));

        assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", ex.getMessage());
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LISTO", "ENTREGADO"})
    void cancel_orderReadyOrDelivered_throwsConflictMentioningStatus(String status) {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.valueOf(status))));

        OrderStatusConflictException ex = assertThrows(OrderStatusConflictException.class,
                () -> orderUseCase.cancelOrder(10L));

        assertTrue(ex.getMessage().contains(status));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    @Test
    void cancel_alreadyCancelledOrder_throwsConflict() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.CANCELADO)));

        OrderStatusConflictException ex = assertThrows(OrderStatusConflictException.class,
                () -> orderUseCase.cancelOrder(10L));

        assertEquals("Tu pedido ya fue cancelado", ex.getMessage());
    }

    @Test
    void cancel_orderOfAnotherClient_throwsOrderNotOfClient() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, 99L, OrderStatus.PENDIENTE)));

        assertThrows(OrderNotOfClientException.class, () -> orderUseCase.cancelOrder(10L));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    @Test
    void cancel_unknownOrder_throwsOrderNotFound() {
        clientLoggedIn();
        when(orderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderUseCase.cancelOrder(99L));
    }

    @Test
    void cancel_nullIdOrNoAuthenticatedUser_throwsDomainException() {
        assertThrows(DomainException.class, () -> orderUseCase.cancelOrder(null));

        when(currentUserPort.getCurrentUserId()).thenReturn(null);
        assertThrows(DomainException.class, () -> orderUseCase.cancelOrder(10L));
        verify(orderPersistencePort, never()).updateStatus(anyLong(), any());
    }

    // ---------------- HU17: trazabilidad de cada cambio de estado ----------------

    private OrderTraceModel capturedTrace() {
        ArgumentCaptor<OrderTraceModel> captor = ArgumentCaptor.forClass(OrderTraceModel.class);
        verify(tracePort).record(captor.capture());
        return captor.getValue();
    }

    private void userWithEmail(Long id, String role, String email) {
        when(userClientPort.findUserById(id)).thenReturn(Optional.of(new UserInfoModel(id, role, null, email)));
    }

    @Test
    void trace_createOrder_recordsPendingWithoutPreviousStatusNorEmployee() {
        clientLoggedIn();
        restaurantExists();
        dish(1L, RESTAURANT_ID, true);
        dish(2L, RESTAURANT_ID, true);
        when(orderPersistencePort.existsByClientIdAndStatusIn(eq(CLIENT_ID), anyCollection())).thenReturn(false);
        when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenAnswer(i -> {
            OrderModel saved = i.getArgument(0);
            saved.setId(50L);
            return saved;
        });
        userWithEmail(CLIENT_ID, RoleNames.CLIENT, "maria@correo.com");

        orderUseCase.createOrder(order);

        OrderTraceModel trace = capturedTrace();
        assertEquals(50L, trace.getOrderId());
        assertEquals(CLIENT_ID, trace.getClientId());
        assertEquals("maria@correo.com", trace.getClientEmail());
        assertNull(trace.getPreviousStatus());
        assertEquals("PENDIENTE", trace.getNewStatus());
        assertNull(trace.getEmployeeId());
        assertNull(trace.getEmployeeEmail());
    }

    @Test
    void trace_sendsRestaurantAndItsOwner() {
        clientLoggedIn();
        RestaurantModel restaurant = new RestaurantModel();
        restaurant.setOwnerId(2L);
        when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.PENDIENTE)));
        when(orderPersistencePort.updateStatus(10L, OrderStatus.CANCELADO))
                .thenReturn(clientOrder(10L, CLIENT_ID, OrderStatus.CANCELADO));

        orderUseCase.cancelOrder(10L);

        OrderTraceModel trace = capturedTrace();
        assertEquals(RESTAURANT_ID, trace.getRestaurantId());
        assertEquals(2L, trace.getRestaurantOwnerId());
    }

    @Test
    void trace_assignOrder_recordsPendingToPreparationWithEmployee() {
        employeeOfRestaurant();
        OrderModel pending = orderInState(10L, RESTAURANT_ID, OrderStatus.PENDIENTE);
        pending.setClientId(CLIENT_ID);
        when(orderPersistencePort.findById(10L)).thenReturn(Optional.of(pending));
        when(orderPersistencePort.updateStatusAndChef(10L, OrderStatus.EN_PREPARACION, EMPLOYEE_ID))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.EN_PREPARACION));
        userWithEmail(CLIENT_ID, RoleNames.CLIENT, "maria@correo.com");
        userWithEmail(EMPLOYEE_ID, RoleNames.EMPLOYEE, "carlos.perez@correo.com");

        orderUseCase.assignOrderToEmployee(10L);

        OrderTraceModel trace = capturedTrace();
        assertEquals("PENDIENTE", trace.getPreviousStatus());
        assertEquals("EN_PREPARACION", trace.getNewStatus());
        assertEquals(EMPLOYEE_ID, trace.getEmployeeId());
        assertEquals("carlos.perez@correo.com", trace.getEmployeeEmail());
        assertEquals("maria@correo.com", trace.getClientEmail());
    }

    @Test
    void trace_markReady_recordsPreparationToReadyAfterSendingMessage() {
        employeeOfRestaurant();
        preparingOrder();
        clientWithPhone("+573001112233");
        when(securityPinPort.generate()).thenReturn("123456");
        when(orderPersistencePort.updateStatusAndPin(10L, OrderStatus.LISTO, "123456"))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.LISTO));

        orderUseCase.markOrderReady(10L);

        InOrder inOrder = inOrder(notificationPort, tracePort);
        inOrder.verify(notificationPort).sendSms(any(), any());
        inOrder.verify(tracePort).record(any(OrderTraceModel.class));
        OrderTraceModel trace = capturedTrace();
        assertEquals("EN_PREPARACION", trace.getPreviousStatus());
        assertEquals("LISTO", trace.getNewStatus());
        assertEquals(EMPLOYEE_ID, trace.getEmployeeId());
    }

    @Test
    void trace_markReady_messageFails_doesNotRecordTrace() {
        employeeOfRestaurant();
        preparingOrder();
        clientWithPhone("+573001112233");
        when(securityPinPort.generate()).thenReturn("123456");
        when(orderPersistencePort.updateStatusAndPin(10L, OrderStatus.LISTO, "123456"))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.LISTO));
        doThrow(new NotificationUnavailableException()).when(notificationPort).sendSms(any(), any());

        assertThrows(NotificationUnavailableException.class, () -> orderUseCase.markOrderReady(10L));
        verify(tracePort, never()).record(any());
    }

    @Test
    void trace_deliver_recordsReadyToDelivered() {
        employeeOfRestaurant();
        readyOrderWithPin("048213");
        when(orderPersistencePort.updateStatus(10L, OrderStatus.ENTREGADO))
                .thenReturn(orderInState(10L, RESTAURANT_ID, OrderStatus.ENTREGADO));

        orderUseCase.deliverOrder(10L, "048213");

        OrderTraceModel trace = capturedTrace();
        assertEquals("LISTO", trace.getPreviousStatus());
        assertEquals("ENTREGADO", trace.getNewStatus());
        assertEquals(EMPLOYEE_ID, trace.getEmployeeId());
    }

    @Test
    void trace_cancel_recordsPendingToCancelledWithoutEmployee() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.PENDIENTE)));
        when(orderPersistencePort.updateStatus(10L, OrderStatus.CANCELADO))
                .thenReturn(clientOrder(10L, CLIENT_ID, OrderStatus.CANCELADO));

        orderUseCase.cancelOrder(10L);

        OrderTraceModel trace = capturedTrace();
        assertEquals("PENDIENTE", trace.getPreviousStatus());
        assertEquals("CANCELADO", trace.getNewStatus());
        assertNull(trace.getEmployeeId());
        assertNull(trace.getEmployeeEmail());
    }

    @Test
    void trace_serviceUnavailable_propagatesSoTransactionRollsBack() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.PENDIENTE)));
        when(orderPersistencePort.updateStatus(10L, OrderStatus.CANCELADO))
                .thenReturn(clientOrder(10L, CLIENT_ID, OrderStatus.CANCELADO));
        doThrow(new TraceUnavailableException()).when(tracePort).record(any());

        assertThrows(TraceUnavailableException.class, () -> orderUseCase.cancelOrder(10L));
    }

    @Test
    void trace_rejectedOperations_doNotRecordAnything() {
        clientLoggedIn();
        when(orderPersistencePort.findById(10L))
                .thenReturn(Optional.of(clientOrder(10L, CLIENT_ID, OrderStatus.EN_PREPARACION)));

        assertThrows(OrderStatusConflictException.class, () -> orderUseCase.cancelOrder(10L));
        verify(tracePort, never()).record(any());
    }
}
