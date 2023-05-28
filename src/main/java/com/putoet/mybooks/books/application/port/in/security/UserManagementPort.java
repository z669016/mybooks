package com.putoet.mybooks.books.application.port.in.security;

import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;

import java.util.Optional;
import java.util.Set;

public interface UserManagementPort {
    void forgetUser(String id);
    User registerUser(String id, String name, String password, AccessRole accessRole);
    Optional<User> userById(String id);
    Set<User> users();
}
