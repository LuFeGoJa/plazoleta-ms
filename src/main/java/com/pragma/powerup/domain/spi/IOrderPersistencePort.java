package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;

import java.util.Collection;
import java.util.Optional;

public interface IOrderPersistencePort {

    OrderModel saveOrder(OrderModel order);

    boolean existsByClientIdAndStatusIn(Long clientId, Collection<OrderStatus> statuses);

    PageModel<OrderModel> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, int page, int size);

    Optional<OrderModel> findById(Long orderId);

    /** Cambia el estado de un pedido existente y su empleado responsable (null = ninguno). */
    OrderModel updateStatusAndChef(Long orderId, OrderStatus status, Long chefId);

    /** Cambia el estado de un pedido existente y guarda su PIN de seguridad. */
    OrderModel updateStatusAndPin(Long orderId, OrderStatus status, String securityPin);

    /** Cambia solo el estado de un pedido existente. */
    OrderModel updateStatus(Long orderId, OrderStatus status);
}
