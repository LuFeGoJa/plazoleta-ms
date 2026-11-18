package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.OrderSummaryResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.domain.model.OrderStatus;

public interface IOrderHandler {

    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);

    PageResponseDto<OrderSummaryResponseDto> listOrdersByStatus(OrderStatus status, int page, int size);

    OrderSummaryResponseDto assignOrderToEmployee(Long orderId);

    OrderSummaryResponseDto markOrderReady(Long orderId);

    OrderSummaryResponseDto deliverOrder(Long orderId, OrderDeliveryRequestDto request);

    OrderSummaryResponseDto cancelOrder(Long orderId);
}
