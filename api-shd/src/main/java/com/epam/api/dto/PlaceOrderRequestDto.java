package com.epam.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class PlaceOrderRequestDto {

    @Valid
    private List<PizzaDto> pizzas;

}
