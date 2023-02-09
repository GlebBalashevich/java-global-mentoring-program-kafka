package com.epam.client.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.epam.client.TestDataProvider;
import com.epam.client.model.Status;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void init() {
        orderMapper = new OrderMapperImpl();
    }

    @Test
    void testToOrder() {
        final var placeOrderRequestDto = TestDataProvider.getPlaceOrderRequestDtoStub();

        final var actual = orderMapper.toOrder(placeOrderRequestDto);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getPizzas()).hasSize(1);
        assertThat(actual.getStatus()).isEqualTo(Status.CREATED);
    }

}
