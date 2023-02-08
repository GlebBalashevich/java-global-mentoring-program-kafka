package com.epam.courier.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import com.epam.api.dto.OrderStatusDto;
import com.epam.courier.service.CourierService;

@Slf4j
@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PutMapping("/delivery/{id}")
    public Mono<OrderStatusDto> updateOrderStatus(@PathVariable String id, @RequestBody OrderStatusDto orderStatusDto) {
        log.debug("Order: {} requested status updating: {}", id, orderStatusDto.getOrderStatus());
        return courierService.updateDeliveryOrderStatus(id, orderStatusDto);
    }

}
