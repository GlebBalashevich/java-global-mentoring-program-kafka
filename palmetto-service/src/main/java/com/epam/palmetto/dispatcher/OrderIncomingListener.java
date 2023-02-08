package com.epam.palmetto.dispatcher;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.epam.palmetto.exception.PalmettoException;
import com.epam.palmetto.util.ErrorCode;
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
import com.epam.api.dto.Status;
import com.epam.palmetto.service.PalmettoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderIncomingListener {

    private final PalmettoService palmettoService;

    /*
     * Was made not as reactive b'cos reactive doesn't support multi consumers - "Concurrency > 1 is not supported by
     * reactive consumer, given that project reactor maintains its own concurrency mechanism"
     */
    @Bean
    public Consumer<Message<OrderDto>> orderListener() {
        return message -> {
            if (message.getPayload().getStatus() == Status.CREATED) {
                takeOrderInProcessing(message).subscribe();
            }
        };
    }

    private Mono<OrderStatusDto> takeOrderInProcessing(Message<OrderDto> message) {
        final var correlationId = extractCorrelationId(message);
        final var orderStatus = new OrderStatusDto(Status.COOKING);
        log.info("Order: {} retrieved event", correlationId);
        return palmettoService.updateCookingOrderStatus(correlationId, orderStatus);
    }

    private String extractCorrelationId(Message<OrderDto> message) {
        final var correlationIdHeader = message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
        if (correlationIdHeader == null) {
            log.error("Correlation id is null");
            throw new PalmettoException("Correlation Id must not be null", HttpStatus.BAD_REQUEST,
                    ErrorCode.PALMETTO_BAD_REQUEST);
        }
        if (correlationIdHeader instanceof String) {
            return message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        } else {
            return new String((byte[]) correlationIdHeader, StandardCharsets.UTF_8);
        }
    }

}
