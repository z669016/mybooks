package com.putoet.mybooks.books.adapter.in.web.validation;

import com.putoet.mybooks.books.adapter.in.web.ApiError;
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
    public ApiError handleConstraintViolation(ServletRequest request, ConstraintViolationException exc) {
        return new ApiError(
                ((HttpServletRequest) request).getMethod(),
                ((HttpServletRequest) request).getRequestURI(),
                HttpStatus.BAD_REQUEST,
                Map.of("parameter", exc.getMessage()),
                exc.getMessage(),
                Instant.now()
                );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleConstraintViolation(ServletRequest request, MethodArgumentNotValidException exc) {
        final var errors = exc.getBindingResult().getAllErrors().stream()
            .map(error -> error instanceof FieldError ?
                    Pair.of(((FieldError) error).getField(), error.getDefaultMessage())
                    : Pair.of("parameters", error.getDefaultMessage())
                    )
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return new ApiError(
                ((HttpServletRequest) request).getMethod(),
                ((HttpServletRequest) request).getRequestURI(),
                HttpStatus.BAD_REQUEST,
                errors,
                exc.getMessage(),
                Instant.now()
        );
    }
}
