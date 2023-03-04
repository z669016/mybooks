package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.*;

import java.util.List;

public interface BookInquiryRepository {
    List<Author> findAuthors();
    List<Author> findAuthorsByName(String name);
    Author findAuthorById(AuthorId authorId);
    List<Book> findBooks();
    List<Book> findBooksByTitle(String title);
    Book findBookById(BookId bookId);
    List<Book> findBooksByAuthorId(AuthorId authorId);
}
