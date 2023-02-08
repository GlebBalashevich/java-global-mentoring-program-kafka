package com.epam.client.dispatcher;

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

import com.epam.api.dto.OrderDto;
import com.epam.client.exception.OrderException;
import com.epam.client.util.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {

    private static final String PROCESSING_ERROR_MESSAGE = "Order: %s was produced unsuccessfully";

    private final Sinks.Many<Message<OrderDto>> processor = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Supplier<Flux<Message<OrderDto>>> orderSender() {
        return processor::asFlux;
    }

    public Mono<OrderDto> sendOrder(OrderDto orderDto) {
        final var message = MessageBuilder.withPayload(orderDto)
                .setHeader(KafkaHeaders.KEY, orderDto.getId().getBytes())
                .setHeader(KafkaHeaders.CORRELATION_ID, orderDto.getId())
                .build();
        return Mono.just(processor.tryEmitNext(message))
                .doOnNext(emitResult -> log.debug("Processing result: {}", emitResult))
                .filter(Sinks.EmitResult::isSuccess)
                .doOnNext(emitResult -> log.info("Order: {} was sent", orderDto.getId()))
                .switchIfEmpty(Mono.defer(() -> processingError(orderDto.getId())))
                .map(emitResult -> orderDto);
    }

    private <T> Mono<T> processingError(String orderId) {
        final var message = String.format(PROCESSING_ERROR_MESSAGE, orderId);
        log.error(message);
        return Mono.error(
                () -> new OrderException(message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ORDER_SERVER_ERROR));
    }

}
