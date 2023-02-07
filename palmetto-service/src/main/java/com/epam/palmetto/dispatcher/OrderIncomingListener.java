package com.epam.palmetto.dispatcher;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.epam.palmetto.dto.OrderDto;
import com.epam.palmetto.dto.OrderStatusDto;
import com.epam.palmetto.model.OrderStatus;
import com.epam.palmetto.service.PalmettoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderIncomingListener {

    private final PalmettoService palmettoService;

    @Bean
    public Consumer<Flux<Message<OrderDto>>> orderListener() {
        return mono -> mono
                .filter(message -> message.getPayload().getStatus() == OrderStatus.CREATED)
                .flatMap(this::takeOrderInProcessing)
                .subscribe();
    }

    private Mono<OrderStatusDto> takeOrderInProcessing(Message<OrderDto> message) {
        final var correlationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        final var orderStatus = new OrderStatusDto(OrderStatus.COOKING);
        log.info("Order: {} retrieved event", correlationId);
        return palmettoService.updateCookingOrderStatus(correlationId, orderStatus);
    }

}
