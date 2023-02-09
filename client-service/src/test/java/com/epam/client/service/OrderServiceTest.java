package com.epam.client.service;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.client.TestDataProvider;
import com.epam.client.dispatcher.OrderHandler;
import com.epam.client.exception.OrderException;
import com.epam.client.mapper.OrderMapper;
import com.epam.client.repository.OrderRepository;

import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class })
class OrderServiceTest {

    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderHandler orderHandler;

    @BeforeEach
    void init() {
        orderService = new OrderService(orderRepository, orderHandler, orderMapper);
    }

    @Test
    void testPlaceOrder() {
        final var orderId = UUID.randomUUID().toString();
        final var placeOrderRequestDto = TestDataProvider.getPlaceOrderRequestDtoStub();
        final var order = TestDataProvider.getOrderStub(orderId);
        final var orderDto = TestDataProvider.getOrderDtoStub(orderId);

        when(orderMapper.toOrder(placeOrderRequestDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(Mono.just(order));
        when(orderMapper.toOrderDto(order)).thenReturn(orderDto);
        when(orderHandler.sendOrder(orderDto)).thenReturn(Mono.just(orderDto));

        StepVerifier.create(orderService.placeOrder(placeOrderRequestDto))
                .expectNext(orderDto)
                .verifyComplete();
    }

    @Test
    void testRetrieveOrderStatus() {
        final var orderId = UUID.randomUUID().toString();
        final var order = TestDataProvider.getOrderStub(orderId);
        final var orderStatusDto = new OrderStatusDto(Status.CREATED);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderMapper.toOrderStatusDto(order.getStatus())).thenReturn(orderStatusDto);

        StepVerifier.create(orderService.retrieveOrderStatus(orderId))
                .expectNext(orderStatusDto)
                .verifyComplete();
    }

    @Test
    void testRetrieveOrderStatus_OrderNotFound() {
        final var orderId = UUID.randomUUID().toString();

        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.retrieveOrderStatus(orderId))
                .expectError(OrderException.class)
                .verify();
    }

    @Test
    void testUpdateOrderStatus() {
        final var orderId = UUID.randomUUID().toString();
        final var order = TestDataProvider.getOrderStub(orderId);
        final var updatingOrder = TestDataProvider.getOrderStub(orderId);
        updatingOrder.setStatus(com.epam.client.model.Status.COOKING);
        final var orderDto = TestDataProvider.getOrderDtoStub(orderId);
        orderDto.setStatus(Status.COOKING);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderRepository.save(updatingOrder)).thenReturn(Mono.just(updatingOrder));
        when(orderMapper.toOrderDto(updatingOrder)).thenReturn(orderDto);

        StepVerifier.create(orderService.updateOrderStatus(orderId, new OrderStatusDto(Status.COOKING)))
                .expectNext(orderDto)
                .verifyComplete();
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        final var orderId = UUID.randomUUID().toString();

        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateOrderStatus(orderId, new OrderStatusDto(Status.COOKING)))
                .expectError(OrderException.class)
                .verify();
    }

}
