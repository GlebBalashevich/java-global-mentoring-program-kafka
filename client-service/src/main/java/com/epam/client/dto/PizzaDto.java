package com.epam.client.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
        S,
        L,
        XL
    }
}
