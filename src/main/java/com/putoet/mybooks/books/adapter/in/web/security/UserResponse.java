package com.putoet.mybooks.books.adapter.in.web.security;

import com.putoet.mybooks.books.domain.security.User;

import java.util.List;

public record UserResponse(String id, String name, String accessRole) {
    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), user.accessRole().name());
    }
    public static List<UserResponse> from(List<User> users) {
        return users.stream().map(UserResponse::from).toList();
    }
}
