package com.epam.courier.exception;

import com.epam.error.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CourierException extends BaseException {

    public CourierException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }

}
