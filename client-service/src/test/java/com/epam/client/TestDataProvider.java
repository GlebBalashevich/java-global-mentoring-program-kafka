package com.epam.client;

import java.util.List;

import com.epam.api.dto.OrderDto;
import com.epam.api.dto.PizzaDto;
import com.epam.api.dto.PlaceOrderRequestDto;
import com.epam.client.model.Order;
import com.epam.client.model.Pizza;
import com.epam.client.model.Status;

public class TestDataProvider {

    private TestDataProvider() {
    }

    public static Order getOrderStub(String orderId) {
        Pizza pizza = new Pizza();
        pizza.setAmount(1);
        pizza.setName("Margarita");
        pizza.setSize(Pizza.Size.XL);
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(Status.CREATED);
        order.setPizzas(List.of(pizza));
        return order;
    }

    public static PlaceOrderRequestDto getPlaceOrderRequestDtoStub() {
        PlaceOrderRequestDto placeOrderRequestDto = new PlaceOrderRequestDto();
        PizzaDto pizzaDto = new PizzaDto();
        pizzaDto.setAmount(1);
        pizzaDto.setName("Margarita");
        pizzaDto.setSize(PizzaDto.Size.XL);
        placeOrderRequestDto.setPizzas(List.of(pizzaDto));
        return placeOrderRequestDto;
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
