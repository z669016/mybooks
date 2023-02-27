package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.Book;

import java.util.List;

public interface Books {
    List<Book> books();
}
