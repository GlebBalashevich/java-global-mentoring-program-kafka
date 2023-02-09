package com.epam.palmetto;

import java.util.List;

import com.epam.api.dto.OrderDto;
import com.epam.api.dto.PizzaDto;

public class TestDataProvider {

    private TestDataProvider() {

    }

    public static OrderDto getOrderDtoStub(String orderId) {
        OrderDto orderDto = new OrderDto();
        PizzaDto pizzaDto = new PizzaDto();
        pizzaDto.setAmount(1);
        pizzaDto.setName("Margarita");
        pizzaDto.setSize(PizzaDto.Size.XL);
        orderDto.setPizzas(List.of(pizzaDto));
        orderDto.setStatus(com.epam.api.dto.Status.CREATED);
        orderDto.setId(orderId);
        return orderDto;
    }

}
