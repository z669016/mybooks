package com.putoet.mybooks.books.application.port.in.security;

import com.putoet.mybooks.books.domain.security.User;

import java.util.Optional;

public interface UserById {
    Optional<User> userById(String id);
}
