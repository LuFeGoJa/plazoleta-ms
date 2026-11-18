package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;

public interface IOrderServicePort {

    /** Crea un pedido PENDIENTE para el cliente autenticado y lo devuelve ya guardado. */
    OrderModel createOrder(OrderModel order);

    /**
     * Lista los pedidos de un estado del restaurante donde trabaja el empleado autenticado,
     * del más antiguo al más reciente; page empieza en 0.
     */
    PageModel<OrderModel> listOrdersByStatus(OrderStatus status, int page, int size);

    /**
     * El empleado autenticado se asigna un pedido PENDIENTE de su restaurante: el pedido pasa a
     * EN_PREPARACION y queda con ese empleado como responsable.
     */
    OrderModel assignOrderToEmployee(Long orderId);

    /**
     * El empleado autenticado marca como LISTO un pedido EN_PREPARACION de su restaurante: se genera un PIN
     * de seguridad y se le envía por mensaje de texto al cliente. Si el mensaje no sale, el pedido no cambia.
     */
    OrderModel markOrderReady(Long orderId);

    /**
     * El empleado autenticado entrega un pedido LISTO de su restaurante: debe indicar el PIN de seguridad
     * que el cliente recibió por mensaje; si coincide, el pedido pasa a ENTREGADO.
     */
    OrderModel deliverOrder(Long orderId, String securityPin);

    /**
     * El cliente autenticado cancela uno de sus pedidos. Solo se puede mientras esté PENDIENTE; si ya está
     * en preparación (o más adelante) se rechaza con un mensaje que lo explica.
     */
    OrderModel cancelOrder(Long orderId);
}
