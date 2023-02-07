package com.epam.courier.dispatcher;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.epam.courier.dto.OrderStatusDto;
import com.epam.courier.model.OrderStatus;
import com.epam.courier.service.CourierService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final CourierService courierService;

    @Bean
    public Consumer<Flux<Message<OrderStatusDto>>> notificationListener() {
        return mono -> mono
                .filter(message -> message.getPayload().getOrderStatus() == OrderStatus.READY_FOR_DELIVERY)
                .flatMap(this::takeDeliveryOrder)
                .subscribe();
    }

    private Mono<OrderStatusDto> takeDeliveryOrder(Message<OrderStatusDto> message) {
        final var correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        final var orderStatus = new OrderStatusDto(OrderStatus.DELIVERY_IN_PROGRESS);
        log.info("Order: {} retrieved new notification event with status: {}", correlationId,
                message.getPayload().getOrderStatus());
        return courierService.updateDeliveryOrderStatus(correlationId, orderStatus);
    }

}
