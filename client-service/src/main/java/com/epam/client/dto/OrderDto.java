package com.epam.client.dto;

import java.util.List;

import lombok.Data;

import com.epam.client.model.OrderStatus;

@Data
public class OrderDto {

    private String id;

    private List<PizzaDto> pizzas;

    private OrderStatus status;

}
