package com.epam.client.dispatcher;

import java.util.function.Consumer;

import com.epam.client.dto.OrderDto;
import com.epam.client.dto.OrderStatusDto;
import com.epam.client.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final OrderService orderService;

    /*
    Was made not as reactive b'cos reactive doesn't support multi consumers - "Concurrency > 1 is
    not supported by reactive consumer, given that project reactor maintains its own concurrency mechanism"
     */
    @Bean
    public Consumer<Message<OrderStatusDto>> notificationListener() {
        return message -> {
            final var orderDto = updateOrderStatus(message).block();
            if (orderDto != null) {
                log.info("Order: {} status was successfully updated", orderDto.getId());
            }
        };
    }

    private Mono<OrderDto> updateOrderStatus(Message<OrderStatusDto> message) {
        final var correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        log.info("Order: {} retrieved new notification event with status: {}", correlationId,
                message.getPayload().getOrderStatus());
        return orderService.updateOrderStatus(correlationId, message.getPayload().getOrderStatus());
    }

}
