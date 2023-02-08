package com.epam.courier.dispatcher;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.courier.service.CourierService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final CourierService courierService;

    /*
     * Was made not as reactive b'cos reactive doesn't support multi consumers - "Concurrency > 1 is not supported by
     * reactive consumer, given that project reactor maintains its own concurrency mechanism"
     */
    @Bean
    public Consumer<Message<OrderStatusDto>> notificationListener() {
        return message -> {
            if (message.getPayload().getOrderStatus() == Status.READY_FOR_DELIVERY) {
                takeDeliveryOrder(message).subscribe();
            }
        };
    }

    private Mono<OrderStatusDto> takeDeliveryOrder(Message<OrderStatusDto> message) {
        final var correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        final var orderStatus = new OrderStatusDto(Status.DELIVERY_IN_PROGRESS);
        log.info("Order: {} retrieved new notification event with status: {}", correlationId,
                message.getPayload().getOrderStatus());
        return courierService.updateDeliveryOrderStatus(correlationId, orderStatus);
    }

}
