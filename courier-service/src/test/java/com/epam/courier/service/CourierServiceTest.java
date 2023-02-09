package com.epam.courier.service;

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
import com.epam.courier.dispatcher.NotificationHandler;
import com.epam.courier.exception.CourierException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class })
class CourierServiceTest {

    private CourierService courierService;

    @Mock
    private NotificationHandler notificationHandler;

    @BeforeEach
    void init() {
        courierService = new CourierService(notificationHandler);
    }

    @Test
    void testUpdateDeliveryOrderStatus() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.DELIVERY_IN_PROGRESS);

        when(notificationHandler.sendNotification(orderId, orderStatusDto)).thenReturn(Mono.just(orderStatusDto));

        StepVerifier.create(courierService.updateDeliveryOrderStatus(orderId, orderStatusDto))
                .expectNext(orderStatusDto)
                .verifyComplete();
    }

    @Test
    void testUpdateDeliveryOrderStatusWrongStatus() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.CREATED);

        assertThatThrownBy(() -> courierService.updateDeliveryOrderStatus(orderId, orderStatusDto)).isInstanceOf(
                CourierException.class);
    }

}
