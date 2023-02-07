package com.epam.palmetto.dto;

import java.util.List;

import lombok.Data;

import com.epam.palmetto.model.OrderStatus;

@Data
public class OrderDto {

    private String id;

    private List<PizzaDto> pizzas;

    private OrderStatus status;

}
