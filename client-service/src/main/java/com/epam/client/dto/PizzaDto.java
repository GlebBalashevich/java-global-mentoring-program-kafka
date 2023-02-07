package com.epam.client.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PizzaDto {

    @NotEmpty
    private String name;

    @NotNull
    private Size size;

    @NotNull
    private Integer amount;

    public enum Size {
        S, L, XL
    }

}
