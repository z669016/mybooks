package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;

import java.util.List;

public interface AuthorByName {
    List<Author> authorByName(String name);
}
