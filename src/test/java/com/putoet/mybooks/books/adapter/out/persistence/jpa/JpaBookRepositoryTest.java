package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@DataJpaTest
public class JpaBookRepositoryTest {
    @Autowired
    private AuthorJpaRepository authorRepository;

    @Autowired
    private BookJpaRepository bookRepository;

    private final DomainMapper mapper = new DomainMapper();

    private BookPersistenceUpdatePort repository;

    private List<Author> authors;
    private List<Book> books;

    @SneakyThrows
    @BeforeEach
    void setup() {
        repository = new JpaBookRepository(mapper, authorRepository, bookRepository);

        authors = testAuthorData().stream()
                .map(data -> repository.registerAuthor(data.name, data.sites))
                .toList();

        books = testBookData(authors).stream()
                .map(data -> repository.registerBook(new BookId(data.schema, data.id), data.title, data.authors, data.formats, data.keywords))
                .toList();
    }

    @Test
    void test1() {
        System.out.println(repository.findAuthors());
    }

    @Test
    void test2() {
        System.out.println(repository.findBooks());
    }

    @SneakyThrows
    static List<AuthorTestData> testAuthorData() {
        return List.of(
                new AuthorTestData("Author 1", "http://linkedin.com/Author 1"),
                new AuthorTestData("Author 2", "http://linkedin.com/Author 2"),
                new AuthorTestData("Author 3", "http://linkedin.com/Author 3"),
                new AuthorTestData("Author 4", "http://linkedin.com/Author 4"),
                new AuthorTestData("Author 5", "http://linkedin.com/Author 5"),
                new AuthorTestData("Author 6", "http://linkedin.com/Author 6")
        );
    }

    record AuthorTestData(String name, Map<SiteType,URL> sites) {
        AuthorTestData(String name, String url) throws MalformedURLException {
            this(name, Map.of(SiteType.LINKEDIN, new URL(url)));
        }
    }

    private static List<BookTestData> testBookData(List<Author> authors) {
        return List.of(
                new BookTestData("Book-1", Set.of(authors.get(0)), Set.of("Java", "Spring"),"application/pdf", "application/epub"),
                new BookTestData("Book-2", Set.of(authors.get(1)), Set.of("Java", "Spring Boot"),"application/pdf"),
                new BookTestData("Book-3", Set.of(authors.get(2)), Set.of("Python"),"application/pdf", "application/epub"),
                new BookTestData("Book-4", Set.of(authors.get(3)), Set.of("Cloud"),"application/epub"),
                new BookTestData("Book-5", Set.of(authors.get(4), authors.get(5)), Set.of("Elixir"),"application/pdf")
        );
    }

    record BookTestData(String schema, String id, String title, Set<Author> authors, Set<String> keywords, Set<MimeType> formats) {
        BookTestData(String title, Set<Author> authors, Set<String> keywords, String ... formats) {
            this(BookId.BookIdSchema.UUID.name()
                    , UUID.randomUUID().toString()
                    , title
                    , authors
                    , keywords
                    , Arrays.stream(formats).map(MimeTypes::toMimeType).collect(Collectors.toSet())
            );
        }
    }
}
