package com.putoet.mybooks.books.adapter.in.web.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public record UserLoginRequest(@NotNull String id, @NotNull String password) {
}
