package com.epam.client.service;

import com.epam.client.dto.OrderDto;
import com.epam.client.dto.OrderStatusDto;
import com.epam.client.dto.PlaceOrderRequestDto;
import com.epam.client.exception.OrderException;
import com.epam.client.mapper.OrderMapper;
import com.epam.client.repository.OrderRepository;
import com.epam.client.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    public Mono<OrderDto> placeOrder(PlaceOrderRequestDto orderRequestDto) {
        final var order = orderMapper.toOrder(orderRequestDto);
        return orderRepository.save(order).map(orderMapper::toOrderDto);
    }

    public Mono<OrderStatusDto> retrieveOrderStatus(String orderId) {
        return orderRepository
                .findById(orderId)
                .map(order -> orderMapper.toOrderStatusDto(order.getStatus()))
                .switchIfEmpty(notFoundError(String.format("Order with id: %s not found", orderId)));
    }

    private <T> Mono<T> notFoundError(String message) {
        log.error(message);
        return Mono.error(() -> new OrderException(message, HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));
    }
}
