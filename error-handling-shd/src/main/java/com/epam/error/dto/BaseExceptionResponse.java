package com.epam.error.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BaseExceptionResponse {

    private Instant timestamp;

    private String error;

    private String code;

    private String message;

    private String path;

}
