package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.adapter.in.web.security.validation.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public record UserLoginRequest(@Email @NotNull String id, @PasswordConstraint String password) {
}
