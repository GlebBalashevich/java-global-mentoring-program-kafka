package com.epam.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderDto {

    private String id;

    private List<PizzaDto> pizzas;

    private Status status;

}
