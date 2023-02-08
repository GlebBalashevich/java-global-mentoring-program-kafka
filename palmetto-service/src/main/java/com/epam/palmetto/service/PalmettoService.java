package com.epam.palmetto.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.palmetto.dispatcher.OrderNotificationProducer;
import com.epam.palmetto.exception.PalmettoException;
import com.epam.palmetto.util.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PalmettoService {

    private static final String ORDER_STATUS_ERROR_MESSAGE = "Wrong order status %s for cooking operation";

    private final OrderNotificationProducer orderNotificationProducer;

    public Mono<OrderStatusDto> updateCookingOrderStatus(String orderId, OrderStatusDto orderStatusDto) {
        validateOrderStatus(orderStatusDto.getOrderStatus());
        return orderNotificationProducer.sendNotification(orderId, orderStatusDto)
                .doOnNext(
                        orderStatus -> log.info("Order: {} status updated: {}", orderId, orderStatus.getOrderStatus()));
    }

    private void validateOrderStatus(Status orderStatus) {
        if (orderStatus != Status.COOKING && orderStatus != Status.READY_FOR_DELIVERY) {
            log.error(String.format(ORDER_STATUS_ERROR_MESSAGE, orderStatus));
            throw new PalmettoException(String.format(ORDER_STATUS_ERROR_MESSAGE, orderStatus), HttpStatus.BAD_REQUEST,
                    ErrorCode.PALMETTO_BAD_REQUEST);
        }
    }

}
