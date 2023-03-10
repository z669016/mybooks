package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.domain.BookId;
import com.putoet.mybooks.domain.FormatType;

import java.util.List;

public interface RegisterBook {
    Book registerBook(BookId bookId, String title, List<Author> authors, String description, List<FormatType> formats);
}
