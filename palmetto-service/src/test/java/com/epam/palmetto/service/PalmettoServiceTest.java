package com.epam.palmetto.service;

import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.palmetto.dispatcher.NotificationHandler;
import com.epam.palmetto.exception.PalmettoException;

import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class })
class PalmettoServiceTest {

    private PalmettoService palmettoService;

    @Mock
    private NotificationHandler notificationHandler;

    @BeforeEach
    void init() {
        palmettoService = new PalmettoService(notificationHandler);
    }

    @Test
    void testUpdateCookingOrderStatus() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.READY_FOR_DELIVERY);

        when(notificationHandler.sendNotification(orderId, orderStatusDto)).thenReturn(Mono.just(orderStatusDto));

        StepVerifier.create(palmettoService.updateCookingOrderStatus(orderId, orderStatusDto))
                .expectNext(orderStatusDto)
                .verifyComplete();
    }

    @Test
    void testUpdateCookingOrderStatusWrongStatus() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.CREATED);

        Assertions.assertThatThrownBy(() -> palmettoService.updateCookingOrderStatus(orderId, orderStatusDto))
                .isInstanceOf(PalmettoException.class);
    }

    @Test
    void testTest() {
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("{'Content-Type':'application/json'}");

        Map map = expression.getValue(Map.class);
        System.out.println(map);
    }

}
