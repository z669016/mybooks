package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;

import java.util.List;

public interface AuthorsByName {
    List<Author> authorsByName(String name);
}
