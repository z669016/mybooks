package com.putoet.mybooks.application;

import com.putoet.mybooks.application.port.in.RegisterAuthorCommand;
import com.putoet.mybooks.domain.*;
import com.putoet.mybooks.framework.H2BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookServiceWithDBTest {
    @Autowired
    private JdbcTemplate template;

    private BookService service;

    @BeforeEach
    void setup() {
        H2BookRepository repository = new H2BookRepository(template);
        service = new BookService(repository);
    }

    @Test
    void registerAuthor() {
        final RegisterAuthorCommand command = RegisterAuthorCommand.withName("Putten, Margot van")
                .withSite(SiteType.LINKEDIN, "https://nl.linkedin.com/in/margot-van-putten-3a115615")
                .build();

        final Author author = service.registerAuthor(command);
        assertNotNull(author);
        assertNotNull(author.id());

        final List<Author> authors = service.authors();
        assertEquals(2, authors.size());
    }
}