package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.adapter.in.web.security.validation.AccessRoleConstraint;
import com.putoet.mybooks.books.adapter.in.web.security.validation.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public record NewUserRequest(@Email @NotNull String id, @NotBlank String name, @PasswordConstraint String password, @AccessRoleConstraint String accessRole) {
}
