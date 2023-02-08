package com.epam.courier.dispatcher;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import com.epam.api.dto.OrderStatusDto;
import com.epam.courier.exception.CourierException;
import com.epam.courier.util.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationProducer {

    private static final String PROCESSING_ERROR_MESSAGE = "Order: %s, updating order status was produced unsuccessfully";

    private final Sinks.Many<Message<OrderStatusDto>> processor = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Supplier<Flux<Message<OrderStatusDto>>> notificationSender() {
        return processor::asFlux;
    }

    public Mono<OrderStatusDto> sendNotification(String orderId, OrderStatusDto orderStatus) {
        final var message = MessageBuilder.withPayload(orderStatus)
                .setHeader(KafkaHeaders.KEY, orderId.getBytes())
                .setHeader(KafkaHeaders.CORRELATION_ID, orderId)
                .build();
        return Mono.just(processor.tryEmitNext(message))
                .doOnNext(emitResult -> log.debug("Processing result: {}", emitResult))
                .filter(Sinks.EmitResult::isSuccess)
                .doOnNext(emitResult -> log.info("Order: {} new order status: {} was sent", orderId,
                        orderStatus.getOrderStatus()))
                .switchIfEmpty(Mono.defer(() -> processingError(orderId)))
                .map(emitResult -> orderStatus);
    }

    private <T> Mono<T> processingError(String orderId) {
        final var message = String.format(PROCESSING_ERROR_MESSAGE, orderId);
        log.error(message);
        return Mono.error(
                () -> new CourierException(message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COURIER_SERVER_ERROR));
    }

}
