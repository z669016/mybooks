package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.domain.BookId;
import jakarta.activation.MimeType;

import java.util.List;
import java.util.Set;

public interface RegisterBook {
    Book registerBook(BookId bookId, String title, List<Author> authors, List<MimeType> formats, Set<String> keywords);
}
