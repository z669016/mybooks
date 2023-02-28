package com.putoet.mybooks.application;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.framework.H2BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookInquiryServiceWithDBTest {

    @Autowired
    private JdbcTemplate template;

    private BookInquiryService service;

    @BeforeEach
    void setup() {
        H2BookRepository repository = new H2BookRepository(template);
        service = new BookInquiryService(repository);
    }

    @Test
    void authorsByName() {
        final List<Author> authors = service.authorsByName("tom");
        assertEquals(1, authors.size());
        assertTrue(authors.get(0).name().toLowerCase().contains("tom"));
    }

    @Test
    void authorById() {
        final List<Author> authors = service.authorsByName("tom");
        final Optional<Author> tom = service.authorById(authors.get(0).id());
        assertTrue(tom.isPresent());
        assertEquals(authors.get(0), tom.get());
    }

    @Test
    void authors() {
        final List<Author> authors = service.authors();
        assertNotNull(authors);
        assertTrue(authors.size() > 0);
    }

    @Test
    void books() {
        // TODO
    }

    @Test
    void bookByTitle() {
        // TODO
    }

    @Test
    void bookById() {
        // TODO
    }

    @Test
    void bookByAuthorName() {
        // TODO
    }
}