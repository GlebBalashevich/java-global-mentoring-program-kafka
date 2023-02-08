package com.epam.courier.exception;

import org.springframework.http.HttpStatus;

import com.epam.error.exception.BaseException;

public class CourierException extends BaseException {

    public CourierException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }

}
