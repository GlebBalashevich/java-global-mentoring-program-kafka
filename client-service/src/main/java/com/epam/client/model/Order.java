package com.epam.client.model;

import java.util.List;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Order {

    private String id;

    private List<Pizza> pizzas;

    private OrderStatus status;

}
