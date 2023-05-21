package com.putoet.mybooks.books.adapter.in.web;

import java.util.Map;

public record ApiErrorResponse(String method, String path, String status, Map<String,String> errors, String message, String timestamp) {
    public static ApiErrorResponse from(ApiError error) {
        return new ApiErrorResponse(
                error.method(),
                error.path(),
                String.valueOf(error.status().value()),
                error.errors(),
                error.message(),
                error.timestamp().toString()
        );
    }
}
