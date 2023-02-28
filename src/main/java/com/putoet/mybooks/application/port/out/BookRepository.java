package com.putoet.mybooks.application.port.out;

import com.putoet.mybooks.domain.Author;

public interface BookRepository extends BookInquiryRepository {
    Author createAuthor(Author author);
//    Author updateAuthor(Author author);
//    Author deleteAuthor(AuthorId authorId);
}
