package com.putoet.mybooks.books.application.port.in.security;

import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;

public interface RegisterUser {
    User registerUser(String id, String name, String password, AccessRole accessRole);
}
