package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.domain.BookId;

import java.util.List;

public interface BookReadOnlyRepository {
    List<Author> findAuthors();
    List<Author> findAuthorsByName(String name);
    Author findAuthorById(AuthorId authorId);
    List<Book> findBooks();
    List<Book> findBooksByTitle(String title);
    Book findBookById(BookId bookId);
    List<Book> findBooksByAuthorId(AuthorId authorId);
}
