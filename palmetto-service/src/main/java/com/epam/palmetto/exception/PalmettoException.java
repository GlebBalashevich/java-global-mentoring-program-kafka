package com.epam.palmetto.exception;

import org.springframework.http.HttpStatus;

import com.epam.error.exception.BaseException;

public class PalmettoException extends BaseException {

    public PalmettoException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }

}
