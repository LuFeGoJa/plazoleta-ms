package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.request.OrderDishRequestDto;
import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderDishResponseDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.OrderSummaryResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderStatus;
import com.pragma.powerup.domain.model.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderHandler implements IOrderHandler {

    private final IOrderServicePort orderServicePort;

    @Override
    public OrderResponseDto createOrder(OrderRequestDto request) {
        OrderModel order = new OrderModel();
        order.setRestaurantId(request.getRestaurantId());
        order.setDishes(toDishModels(request.getDishes()));

        OrderModel saved = orderServicePort.createOrder(order);

        List<OrderDishResponseDto> dishes = saved.getDishes().stream()
                .map(item -> new OrderDishResponseDto(item.getDishId(), item.getQuantity()))
                .collect(Collectors.toList());
        return new OrderResponseDto(saved.getId(), saved.getRestaurantId(), saved.getStatus().name(),
                saved.getDate(), dishes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderSummaryResponseDto> listOrdersByStatus(OrderStatus status, int page, int size) {
        PageModel<OrderModel> result = orderServicePort.listOrdersByStatus(status, page, size);
        List<OrderSummaryResponseDto> content = result.getContent().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        return new PageResponseDto<>(content, result.getPage(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public OrderSummaryResponseDto assignOrderToEmployee(Long orderId) {
        return toSummary(orderServicePort.assignOrderToEmployee(orderId));
    }

    @Override
    public OrderSummaryResponseDto markOrderReady(Long orderId) {
        return toSummary(orderServicePort.markOrderReady(orderId));
    }

    @Override
    public OrderSummaryResponseDto deliverOrder(Long orderId, OrderDeliveryRequestDto request) {
        return toSummary(orderServicePort.deliverOrder(orderId, request.getSecurityPin()));
    }

    @Override
    public OrderSummaryResponseDto cancelOrder(Long orderId) {
        return toSummary(orderServicePort.cancelOrder(orderId));
    }

    private OrderSummaryResponseDto toSummary(OrderModel order) {
        List<OrderDishResponseDto> dishes = order.getDishes().stream()
                .map(item -> new OrderDishResponseDto(item.getDishId(), item.getQuantity()))
                .collect(Collectors.toList());
        return new OrderSummaryResponseDto(order.getId(), order.getClientId(), order.getStatus().name(),
                order.getDate(), order.getChefId(), dishes);
    }

    private List<OrderDishModel> toDishModels(List<OrderDishRequestDto> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(item -> item == null ? null : new OrderDishModel(item.getDishId(), item.getQuantity()))
                .collect(Collectors.toList());
    }
}
