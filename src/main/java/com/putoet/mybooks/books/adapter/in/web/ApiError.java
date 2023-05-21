package com.putoet.mybooks.books.adapter.in.web;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

public record ApiError(String method, String path, HttpStatus status, Map<String,String> errors, String message, Instant timestamp) {
}
