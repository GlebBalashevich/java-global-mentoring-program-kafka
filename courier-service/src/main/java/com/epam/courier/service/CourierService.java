package com.epam.courier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.courier.dispatcher.NotificationHandler;
import com.epam.courier.exception.CourierException;
import com.epam.courier.util.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierService {

    private static final String ORDER_STATUS_ERROR_MESSAGE = "Wrong order status %s for delivery operation";

    private final NotificationHandler notificationHandler;

    public Mono<OrderStatusDto> updateDeliveryOrderStatus(String orderId, OrderStatusDto orderStatusDto) {
        validateOrderStatus(orderStatusDto.getOrderStatus());
        return notificationHandler.sendNotification(orderId, orderStatusDto)
                .doOnNext(orderStatus -> log.info("Order: {} status updated: {}", orderId,
                        orderStatusDto.getOrderStatus()));
    }

    private void validateOrderStatus(Status orderStatus) {
        if (orderStatus != Status.DELIVERY_IN_PROGRESS && orderStatus != Status.DELIVERED) {
            log.error(String.format(ORDER_STATUS_ERROR_MESSAGE, orderStatus));
            throw new CourierException(String.format(ORDER_STATUS_ERROR_MESSAGE, orderStatus), HttpStatus.BAD_REQUEST,
                    ErrorCode.COURIER_BAD_REQUEST);
        }
    }

}
