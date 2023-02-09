package com.epam.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import com.epam.api.dto.OrderDto;
import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.PlaceOrderRequestDto;
import com.epam.client.dispatcher.OrderHandler;
import com.epam.client.exception.OrderException;
import com.epam.client.mapper.OrderMapper;
import com.epam.client.model.Status;
import com.epam.client.repository.OrderRepository;
import com.epam.client.util.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderHandler orderHandler;

    private final OrderMapper orderMapper;

    @Transactional
    public Mono<OrderDto> placeOrder(PlaceOrderRequestDto orderRequestDto) {
        final var order = orderMapper.toOrder(orderRequestDto);
        return orderRepository.save(order).map(orderMapper::toOrderDto).flatMap(orderHandler::sendOrder);
    }

    public Mono<OrderStatusDto> retrieveOrderStatus(String orderId) {
        return orderRepository
                .findById(orderId)
                .map(order -> orderMapper.toOrderStatusDto(order.getStatus()))
                .switchIfEmpty(Mono.defer(() -> notFoundError(orderId)));
    }

    public Mono<OrderDto> updateOrderStatus(String orderId, OrderStatusDto orderStatus) {
        return orderRepository
                .findById(orderId)
                .doOnNext(order -> order.setStatus(Status.valueOf(orderStatus.getOrderStatus().name())))
                .flatMap(orderRepository::save)
                .map(orderMapper::toOrderDto)
                .switchIfEmpty(Mono.defer(() -> notFoundError(orderId)));
    }

    private <T> Mono<T> notFoundError(String orderId) {
        final var message = String.format("Order with id: %s not found", orderId);
        log.error(message);
        return Mono.error(() -> new OrderException(message, HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));
    }

}
