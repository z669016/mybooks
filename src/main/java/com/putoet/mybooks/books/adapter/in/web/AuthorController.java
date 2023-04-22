package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.application.BookService;
import com.putoet.mybooks.books.domain.Author;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuthorController {
    private final BookService bookService;

    public AuthorController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/authors")
    public List<Author> getAuthors() {
        return bookService.authors();
    }
}
