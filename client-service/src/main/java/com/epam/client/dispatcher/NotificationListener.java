package com.epam.client.dispatcher;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import com.epam.api.dto.OrderDto;
import com.epam.api.dto.OrderStatusDto;
import com.epam.client.exception.OrderException;
import com.epam.client.service.OrderService;
import com.epam.client.util.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final OrderService orderService;

    /*
     * Was made not as reactive b'cos reactive doesn't support multi consumers - "Concurrency > 1 is not supported by
     * reactive consumer, given that project reactor maintains its own concurrency mechanism"
     */
    @Bean
    public Consumer<Message<OrderStatusDto>> notificationConsumer() {
        return message -> {
            final var orderDto = updateOrderStatus(message).block();
            if (orderDto != null) {
                log.info("Order: {} status was successfully updated", orderDto.getId());
            }
        };
    }

    private Mono<OrderDto> updateOrderStatus(Message<OrderStatusDto> message) {
        final var correlationId = extractCorrelationId(message);
        log.info("Order: {} retrieved new notification event with status: {}", correlationId,
                message.getPayload().getOrderStatus());
        return orderService.updateOrderStatus(correlationId, message.getPayload());
    }

    private String extractCorrelationId(Message<OrderStatusDto> message) {
        final var correlationIdHeader = message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
        if (correlationIdHeader == null) {
            log.error("Correlation id is null");
            throw new OrderException("Correlation Id must not be null", HttpStatus.BAD_REQUEST,
                    ErrorCode.ORDER_BAD_REQUEST);
        }
        if (correlationIdHeader instanceof String) {
            return message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        } else {
            return new String((byte[]) correlationIdHeader, StandardCharsets.UTF_8);
        }
    }

}
