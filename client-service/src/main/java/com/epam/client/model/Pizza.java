package com.epam.client.model;

import lombok.Data;

@Data
public class Pizza {

    private String name;

    private Size size;

    private Integer amount;

    public enum Size {
        S,
        L,
        XL
    }
}
