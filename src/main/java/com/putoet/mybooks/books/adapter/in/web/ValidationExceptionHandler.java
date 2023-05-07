package com.putoet.mybooks.books.adapter.in.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ValidationExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String,String> handleConstraintViolation(ServletRequest request, ConstraintViolationException exc) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "message", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "error", exc.getMessage(),
                "method", ((HttpServletRequest) request).getMethod(),
                "path", ((HttpServletRequest) request).getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,Object> handleConstraintViolation(ServletRequest request, MethodArgumentNotValidException exc) {
        final Map<String,Object> messages = exc.getBindingResult().getAllErrors().stream()
            .map(error -> error instanceof FieldError ?
                    Pair.of(((FieldError) error).getField(), error.getDefaultMessage())
                    : Pair.of("parameters", error.getDefaultMessage())
                    )
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "message", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "error", messages,
                "method", ((HttpServletRequest) request).getMethod(),
                "path", ((HttpServletRequest) request).getRequestURI()
        );
    }
}
