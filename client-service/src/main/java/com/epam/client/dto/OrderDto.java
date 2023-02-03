package com.epam.client.dto;

import com.epam.client.model.OrderStatus;
import java.util.List;
import lombok.Data;

@Data
public class OrderDto {

    private String id;

    private List<PizzaDto> pizzas;

    private OrderStatus status;
}
