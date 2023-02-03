package com.epam.client.controller.advice;

import com.epam.client.dto.exception.BadRequestExceptionResponse;
import com.epam.client.dto.exception.BaseExceptionResponse;
import com.epam.client.exception.BaseException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@Slf4j
@RestControllerAdvice
public class ReactiveExceptionHandling {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE =
            "Internal server error occurred, contact server administrator";

    private static final String BAD_REQUEST_MESSAGE = "Invalid request parameters";

    private static final String INTERNAL_SERVER_ERROR_CODE = "ISE-0";

    private static final String BAD_REQUEST_ERROR_CODE = "BRE-0";

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseExceptionResponse> handle(BaseException e, ServerWebExchange serverWebExchange) {
        final var httpStatus = e.getHttpStatus();
        final var errorCode = e.getErrorCode();
        final var message = e.getMessage();
        return new ResponseEntity<>(
                buildBaseExceptionResponse(serverWebExchange, errorCode, message, httpStatus), httpStatus);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<BadRequestExceptionResponse> handle(
            WebExchangeBindException e, ServerWebExchange serverWebExchange) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(
                buildBadRequestErrorResponse(
                        serverWebExchange, BAD_REQUEST_ERROR_CODE, httpStatus, BAD_REQUEST_MESSAGE, e),
                httpStatus);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<BaseExceptionResponse> handle(
            ServerWebInputException e, ServerWebExchange serverWebExchange) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var message = e.getReason();
        return new ResponseEntity<>(
                buildBaseExceptionResponse(serverWebExchange, BAD_REQUEST_ERROR_CODE, message, httpStatus), httpStatus);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BaseExceptionResponse> handle(Throwable e, ServerWebExchange serverWebExchange) {
        log.error("Error occurred while processing request", e);
        final var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(
                buildBaseExceptionResponse(
                        serverWebExchange, INTERNAL_SERVER_ERROR_CODE, INTERNAL_SERVER_ERROR_MESSAGE, httpStatus),
                httpStatus);
    }

    private BaseExceptionResponse buildBaseExceptionResponse(
            ServerWebExchange exchange, String errorCode, String message, HttpStatus status) {
        return BaseExceptionResponse.builder()
                .timestamp(Instant.now())
                .error(status.getReasonPhrase())
                .code(errorCode)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();
    }

    private BadRequestExceptionResponse buildBadRequestErrorResponse(
            ServerWebExchange exchange, String code, HttpStatus status, String message, WebExchangeBindException e) {
        final var validationDetails = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> BadRequestExceptionResponse.ValidationDetail.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .toList();
        return BadRequestExceptionResponse.builder()
                .timestamp(Instant.now())
                .error(status.getReasonPhrase())
                .code(code)
                .path(exchange.getRequest().getPath().value())
                .message(message)
                .details(validationDetails)
                .build();
    }
}
