package com.epam.client.controller;

import com.epam.client.dto.OrderDto;
import com.epam.client.dto.OrderStatusDto;
import com.epam.client.dto.PlaceOrderRequestDto;
import com.epam.client.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> placeOrder(@Validated @RequestBody PlaceOrderRequestDto orderRequestDto) {
        log.debug("Requested placing new order: {}", orderRequestDto);
        return orderService.placeOrder(orderRequestDto);
    }

    @GetMapping("/{id}/status")
    public Mono<OrderStatusDto> retrieveOrderStatus(@PathVariable String id) {
        log.debug("Requested order status for order: {}", id);
        return orderService.retrieveOrderStatus(id);
    }
}
