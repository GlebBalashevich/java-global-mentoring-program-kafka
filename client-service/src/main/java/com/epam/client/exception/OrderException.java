package com.epam.client.exception;

import org.springframework.http.HttpStatus;

import com.epam.error.exception.BaseException;

public class OrderException extends BaseException {

    public OrderException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }

}
