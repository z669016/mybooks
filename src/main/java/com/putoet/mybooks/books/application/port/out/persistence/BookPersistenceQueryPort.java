package com.putoet.mybooks.books.application.port.out.persistence;

import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;

import java.util.Set;

public interface BookPersistenceQueryPort {
    Set<Author> findAuthors();
    Set<Author> findAuthorsByName(String name);
    Author findAuthorById(AuthorId authorId);
    Set<Book> findBooks();
    Set<Book> findBooksByTitle(String title);
    Book findBookById(BookId bookId);
    Set<Book> findBooksByAuthorId(AuthorId authorId);
}
