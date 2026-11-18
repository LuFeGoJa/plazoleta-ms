package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.exception.OrderNotFoundException;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderDishEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderJpaAdapter implements IOrderPersistencePort {

    private final IOrderRepository orderRepository;

    @Override
    public OrderModel saveOrder(OrderModel order) {
        return toModel(orderRepository.save(toEntity(order)));
    }

    @Override
    public Optional<OrderModel> findById(Long orderId) {
        return orderRepository.findById(orderId).map(this::toModel);
    }

    @Override
    public OrderModel updateStatusAndChef(Long orderId, OrderStatus status, Long chefId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        entity.setStatus(status);
        entity.setChefId(chefId);
        return toModel(orderRepository.save(entity));
    }

    @Override
    public OrderModel updateStatusAndPin(Long orderId, OrderStatus status, String securityPin) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        entity.setStatus(status);
        entity.setSecurityPin(securityPin);
        return toModel(orderRepository.save(entity));
    }

    @Override
    public OrderModel updateStatus(Long orderId, OrderStatus status) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        entity.setStatus(status);
        return toModel(orderRepository.save(entity));
    }

    @Override
    public boolean existsByClientIdAndStatusIn(Long clientId, Collection<OrderStatus> statuses) {
        return orderRepository.existsByClientIdAndStatusIn(clientId, statuses);
    }

    @Override
    public PageModel<OrderModel> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, int page, int size) {
        Page<OrderEntity> result = orderRepository.findByRestaurantIdAndStatus(restaurantId, status,
                PageRequest.of(page, size, Sort.by("orderDate").ascending()));
        List<OrderModel> content = result.getContent().stream().map(this::toModel).collect(Collectors.toList());
        return new PageModel<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private OrderEntity toEntity(OrderModel model) {
        OrderEntity entity = new OrderEntity();
        entity.setId(model.getId());
        entity.setClientId(model.getClientId());
        entity.setRestaurantId(model.getRestaurantId());
        entity.setOrderDate(model.getDate());
        entity.setStatus(model.getStatus());
        entity.setChefId(model.getChefId());
        entity.setSecurityPin(model.getSecurityPin());
        for (OrderDishModel item : model.getDishes()) {
            OrderDishEntity line = new OrderDishEntity();
            line.setOrder(entity);
            line.setDishId(item.getDishId());
            line.setQuantity(item.getQuantity());
            entity.getDishes().add(line);
        }
        return entity;
    }

    private OrderModel toModel(OrderEntity entity) {
        List<OrderDishModel> dishes = entity.getDishes().stream()
                .map(line -> new OrderDishModel(line.getDishId(), line.getQuantity()))
                .collect(Collectors.toList());
        return new OrderModel(entity.getId(), entity.getClientId(), entity.getRestaurantId(),
                entity.getOrderDate(), entity.getStatus(), entity.getChefId(), dishes, entity.getSecurityPin());
    }
}
