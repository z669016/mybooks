package com.putoet.mybooks.application;

import com.putoet.mybooks.domain.*;
import com.putoet.mybooks.framework.H2BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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
    void registerAuthor() throws MalformedURLException {
        final Author author = service.registerAuthor("Author, Name",
                Map.of(SiteType.HOMEPAGE,  new URL("https://nu.nl")));
        assertNotNull(author);
        assertNotNull(author.id());

        final List<Author> authors = service.authors();
        assertEquals(2, authors.size());
    }
}