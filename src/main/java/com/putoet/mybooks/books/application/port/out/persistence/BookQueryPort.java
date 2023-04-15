package com.putoet.mybooks.books.application.port.out.persistence;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;

import java.util.List;

public interface BookQueryPort {
    List<Author> findAuthors();
    List<Author> findAuthorsByName(String name);
    Author findAuthorById(AuthorId authorId);
    List<Book> findBooks();
    List<Book> findBooksByTitle(String title);
    Book findBookById(BookId bookId);
    List<Book> findBooksByAuthorId(AuthorId authorId);
}
