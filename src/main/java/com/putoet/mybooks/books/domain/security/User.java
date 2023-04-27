package com.putoet.mybooks.books.domain.security;

public record User(String id, String name, String password, AccessRole accessRole) {
}
