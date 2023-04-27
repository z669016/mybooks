package com.putoet.mybooks.books.application.port.out.security;

import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;

import java.util.List;

public interface UserPort {
    List<User> findUsers();
    User findUserById(String id);
    void forgetUser(String id);
    User registerUser(String id, String name, String password, AccessRole accessRole);
}
