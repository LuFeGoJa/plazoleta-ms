package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.exception.ActiveOrderExistsException;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.DomainException;
import com.pragma.powerup.domain.exception.EmployeeWithoutRestaurantException;
import com.pragma.powerup.domain.exception.InvalidSecurityPinException;
import com.pragma.powerup.domain.exception.OrderNotFoundException;
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
import com.pragma.powerup.domain.model.UserInfoModel;
import com.pragma.powerup.domain.spi.ICurrentUserPort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantEmployeePersistencePort;
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityPinPort;
import com.pragma.powerup.domain.spi.ITracePort;
import com.pragma.powerup.domain.spi.IUserClientPort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderUseCase implements IOrderServicePort {

    private static final int MAX_PAGE_SIZE = 100;

    /** Mientras un pedido esté en alguno de estos estados, el cliente no puede hacer otro. */
    private static final List<OrderStatus> IN_PROGRESS =
            Arrays.asList(OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION, OrderStatus.LISTO);

    private final IOrderPersistencePort orderPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final ICurrentUserPort currentUserPort;
    private final IRestaurantEmployeePersistencePort employeePersistencePort;
    private final IUserClientPort userClientPort;
    private final INotificationPort notificationPort;
    private final ISecurityPinPort securityPinPort;
    private final ITracePort tracePort;

    public OrderUseCase(IOrderPersistencePort orderPersistencePort,
                        IRestaurantPersistencePort restaurantPersistencePort,
                        IDishPersistencePort dishPersistencePort,
                        ICurrentUserPort currentUserPort,
                        IRestaurantEmployeePersistencePort employeePersistencePort,
                        IUserClientPort userClientPort,
                        INotificationPort notificationPort,
                        ISecurityPinPort securityPinPort,
                        ITracePort tracePort) {
        this.orderPersistencePort = orderPersistencePort;
        this.restaurantPersistencePort = restaurantPersistencePort;
        this.dishPersistencePort = dishPersistencePort;
        this.currentUserPort = currentUserPort;
        this.employeePersistencePort = employeePersistencePort;
        this.userClientPort = userClientPort;
        this.notificationPort = notificationPort;
        this.securityPinPort = securityPinPort;
        this.tracePort = tracePort;
    }

    @Override
    public OrderModel createOrder(OrderModel order) {
        Long clientId = currentUserPort.getCurrentUserId();
        if (clientId == null) {
            throw new DomainException("No se pudo identificar al cliente que hace el pedido");
        }
        validateRequest(order);

        Long restaurantId = order.getRestaurantId();
        if (restaurantPersistencePort.findById(restaurantId).isEmpty()) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        if (orderPersistencePort.existsByClientIdAndStatusIn(clientId, IN_PROGRESS)) {
            throw new ActiveOrderExistsException();
        }
        for (OrderDishModel item : order.getDishes()) {
            validateDishBelongsToRestaurant(item.getDishId(), restaurantId);
        }

        order.setId(null);
        order.setClientId(clientId);
        order.setDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDIENTE);
        order.setChefId(null);
        OrderModel saved = orderPersistencePort.saveOrder(order);
        recordTrace(saved, null, OrderStatus.PENDIENTE, null);
        return saved;
    }

    @Override
    public PageModel<OrderModel> listOrdersByStatus(OrderStatus status, int page, int size) {
        if (status == null) {
            throw new DomainException("El estado es obligatorio");
        }
        if (page < 0) {
            throw new DomainException("El número de página no puede ser negativo");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new DomainException("La cantidad de elementos por página debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        return orderPersistencePort.findByRestaurantAndStatus(currentEmployeeRestaurantId(), status, page, size);
    }

    @Override
    public OrderModel assignOrderToEmployee(Long orderId) {
        if (orderId == null) {
            throw new DomainException("El pedido es obligatorio");
        }
        OrderModel order = findOrderOfEmployeeRestaurant(orderId);
        if (order.getStatus() != OrderStatus.PENDIENTE) {
            throw new OrderStatusConflictException("Solo se puede tomar un pedido en estado PENDIENTE; "
                    + "este pedido está " + order.getStatus());
        }
        Long employeeId = currentUserPort.getCurrentUserId();
        OrderModel updated = orderPersistencePort.updateStatusAndChef(orderId, OrderStatus.EN_PREPARACION, employeeId);
        recordTrace(order, OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION, employeeId);
        return updated;
    }

    @Override
    public OrderModel markOrderReady(Long orderId) {
        if (orderId == null) {
            throw new DomainException("El pedido es obligatorio");
        }
        OrderModel order = findOrderOfEmployeeRestaurant(orderId);
        if (order.getStatus() != OrderStatus.EN_PREPARACION) {
            throw new OrderStatusConflictException("Solo se puede marcar como listo un pedido EN_PREPARACION; "
                    + "este pedido está " + order.getStatus());
        }
        UserInfoModel client = userClientPort.findUserById(order.getClientId())
                .orElseThrow(() -> new DomainException("No se encontró al cliente del pedido " + orderId));
        if (client.getPhone() == null || client.getPhone().trim().isEmpty()) {
            throw new DomainException("El cliente del pedido " + orderId + " no tiene un celular registrado");
        }

        String pin = securityPinPort.generate();
        OrderModel updated = orderPersistencePort.updateStatusAndPin(orderId, OrderStatus.LISTO, pin);
        // Si el mensaje falla se lanza una excepción y la transacción deshace el cambio de estado.
        notificationPort.sendSms(client.getPhone().trim(), "Plazoleta: tu pedido #" + orderId
                + " esta listo. Tu PIN de seguridad es " + pin + ". Presentalo al recoger tu pedido.");
        // La trazabilidad va al final: si falla, la transacción deshace el cambio de estado.
        recordTrace(order, OrderStatus.EN_PREPARACION, OrderStatus.LISTO, currentUserPort.getCurrentUserId());
        return updated;
    }

    @Override
    public OrderModel deliverOrder(Long orderId, String securityPin) {
        if (orderId == null) {
            throw new DomainException("El pedido es obligatorio");
        }
        if (securityPin == null || securityPin.trim().isEmpty()) {
            throw new DomainException("El PIN de seguridad es obligatorio");
        }
        OrderModel order = findOrderOfEmployeeRestaurant(orderId);
        if (order.getStatus() != OrderStatus.LISTO) {
            throw new OrderStatusConflictException("Solo se puede entregar un pedido LISTO; "
                    + "este pedido está " + order.getStatus());
        }
        if (order.getSecurityPin() == null || !order.getSecurityPin().equals(securityPin.trim())) {
            throw new InvalidSecurityPinException();
        }
        OrderModel updated = orderPersistencePort.updateStatus(orderId, OrderStatus.ENTREGADO);
        recordTrace(order, OrderStatus.LISTO, OrderStatus.ENTREGADO, currentUserPort.getCurrentUserId());
        return updated;
    }

    @Override
    public OrderModel cancelOrder(Long orderId) {
        if (orderId == null) {
            throw new DomainException("El pedido es obligatorio");
        }
        Long clientId = currentUserPort.getCurrentUserId();
        if (clientId == null) {
            throw new DomainException("No se pudo identificar al cliente");
        }
        OrderModel order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!clientId.equals(order.getClientId())) {
            throw new OrderNotOfClientException();
        }
        if (order.getStatus() != OrderStatus.PENDIENTE) {
            throw new OrderStatusConflictException(cannotCancelMessage(order.getStatus()));
        }
        OrderModel updated = orderPersistencePort.updateStatus(orderId, OrderStatus.CANCELADO);
        recordTrace(order, OrderStatus.PENDIENTE, OrderStatus.CANCELADO, null);
        return updated;
    }

    /**
     * Envía a trazabilidad-ms el cambio de estado. Se llama siempre al final de cada operación: si trazabilidad
     * no responde se lanza una excepción y la transacción deshace el cambio, así no quedan pedidos sin historial.
     */
    private void recordTrace(OrderModel order, OrderStatus previous, OrderStatus current, Long employeeId) {
        Long ownerId = order.getRestaurantId() == null ? null
                : restaurantPersistencePort.findById(order.getRestaurantId()).map(RestaurantModel::getOwnerId).orElse(null);
        tracePort.record(new OrderTraceModel(order.getId(), order.getClientId(), emailOf(order.getClientId()),
                order.getRestaurantId(), ownerId,
                previous == null ? null : previous.name(), current.name(), employeeId, emailOf(employeeId)));
    }

    private String emailOf(Long userId) {
        if (userId == null) {
            return null;
        }
        return userClientPort.findUserById(userId).map(UserInfoModel::getEmail).orElse(null);
    }

    private String cannotCancelMessage(OrderStatus status) {
        if (status == OrderStatus.EN_PREPARACION) {
            return "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";
        }
        if (status == OrderStatus.CANCELADO) {
            return "Tu pedido ya fue cancelado";
        }
        return "Lo sentimos, tu pedido ya está " + status + " y no puede cancelarse";
    }

    /** Busca un pedido y verifica que sea del restaurante donde trabaja el empleado autenticado. */
    private OrderModel findOrderOfEmployeeRestaurant(Long orderId) {
        Long restaurantId = currentEmployeeRestaurantId();
        OrderModel order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!restaurantId.equals(order.getRestaurantId())) {
            throw new OrderNotInRestaurantException();
        }
        return order;
    }

    /** Restaurante donde trabaja el empleado autenticado; falla si no está vinculado a ninguno. */
    private Long currentEmployeeRestaurantId() {
        Long employeeId = currentUserPort.getCurrentUserId();
        Long restaurantId = employeeId == null ? null
                : employeePersistencePort.findRestaurantIdByEmployeeId(employeeId).orElse(null);
        if (restaurantId == null) {
            throw new EmployeeWithoutRestaurantException();
        }
        return restaurantId;
    }

    private void validateDishBelongsToRestaurant(Long dishId, Long restaurantId) {
        DishModel dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(dishId));
        if (!restaurantId.equals(dish.getRestaurantId())) {
            throw new DomainException("El plato " + dishId + " no pertenece al restaurante " + restaurantId);
        }
        if (!Boolean.TRUE.equals(dish.getActive())) {
            throw new DomainException("El plato " + dishId + " no está disponible");
        }
    }

    private void validateRequest(OrderModel order) {
        if (order == null || order.getRestaurantId() == null) {
            throw new DomainException("El restaurante es obligatorio");
        }
        if (order.getDishes() == null || order.getDishes().isEmpty()) {
            throw new DomainException("El pedido debe incluir al menos un plato");
        }
        Set<Long> seen = new HashSet<>();
        for (OrderDishModel item : order.getDishes()) {
            if (item == null || item.getDishId() == null) {
                throw new DomainException("Cada plato del pedido debe indicar su id");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new DomainException("La cantidad del plato " + item.getDishId() + " debe ser un entero mayor a 0");
            }
            if (!seen.add(item.getDishId())) {
                throw new DomainException("El plato " + item.getDishId()
                        + " está repetido: indica una sola vez cada plato con su cantidad");
            }
        }
    }
}
