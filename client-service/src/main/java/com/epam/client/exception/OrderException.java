package com.epam.client.exception;

import com.epam.error.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OrderException extends BaseException {

    public OrderException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }

}
