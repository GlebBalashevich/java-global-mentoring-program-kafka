package com.epam.client.dispatcher;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.epam.client.dto.OrderDto;
import com.epam.client.dto.OrderStatusDto;
import com.epam.client.service.OrderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final OrderService orderService;

    @Bean
    public Consumer<Flux<Message<OrderStatusDto>>> notificationListener() {
        return mono -> mono.flatMap(this::updateOrderStatus)
                .doOnNext(orderDto -> log.info("Order with id: {} was successfully updated", orderDto.getId()))
                .subscribe();
    }

    private Mono<OrderDto> updateOrderStatus(Message<OrderStatusDto> message) {
        final var correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        log.info("Retrieved new notification event for order:{} with status: {}", correlationId,
                message.getPayload().getOrderStatus());
        return orderService.updateOrderStatus(correlationId, message.getPayload().getOrderStatus());
    }

}
