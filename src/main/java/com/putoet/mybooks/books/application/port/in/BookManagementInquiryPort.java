package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;

import java.util.Optional;
import java.util.Set;

public interface BookManagementInquiryPort {
    Optional<Author> authorById(AuthorId id);
    Set<Author> authors();
    Set<Author> authorsByName(String name);
    Set<String> authorSiteTypes();
    Set<Book> books();
    Optional<Book> bookById(BookId id);
    Set<Book> booksByAuthorName(String name);
    Set<Book> booksByTitle(String title);
}
