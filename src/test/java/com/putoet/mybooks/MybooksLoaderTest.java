package com.putoet.mybooks;

import com.putoet.mybooks.books.adapter.out.persistence.EpubBookLoader;
import com.putoet.mybooks.books.adapter.out.persistence.FolderBookRepositoryPersistence;
import com.putoet.mybooks.books.adapter.out.persistence.H2BookRepositoryPersistence;
import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.Book;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MybooksApplication.class)
@AutoConfigureMockMvc
class MybooksLoaderTest {
    private static final String BOOKS = "/Users/renevanputten/OneDrive/Documents/Books";
    private static final String LEANPUB = "/Users/renevanputten/OneDrive/Documents/Books/leanpub";
    private static final Logger logger = LoggerFactory.getLogger(MybooksLoaderTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadBooks() {
        final long start = System.currentTimeMillis();
        final H2BookRepositoryPersistence database = new H2BookRepositoryPersistence(jdbcTemplate);
        final FolderBookRepositoryPersistence folder = new FolderBookRepositoryPersistence(Paths.get(BOOKS));

        final BookManagementInquiryPort inputBbookManagementInquiryPort = new BookInquiryService(folder);
        final BookManagementInquiryPort outputBookManagementInquiryPort = new BookInquiryService(database);
        final BookManagementUpdatePort outputBookManagementUpdatePort = new BookUpdateService(database);

        final Map<String, Author> storedAuthors = new HashMap<>();
        for (Author author : inputBbookManagementInquiryPort.authors()) {
            try {
                storedAuthors.put(author.name(), outputBookManagementUpdatePort.registerAuthor(author.name(), author.sites()));
            } catch (RuntimeException exc) {
                logger.error("Failed to register author '" + author + "'", exc);
            }
        }

        storedAuthors.values().stream()
                .sorted(Comparator.comparing(Author::name))
                .forEach(System.out::println);

        for (Book book : inputBbookManagementInquiryPort.books()) {
            final Set<Author> authors = book.authors().stream()
                    .map(author -> storedAuthors.get(author.name()))
                    .collect(Collectors.toSet());
            try {
                outputBookManagementUpdatePort.registerBook(book.id(), book.title(), authors, book.formats().mimeTypes(), book.keywords());
            } catch (RuntimeException exc) {
                logger.error("Failed to register book '" + book + "'", exc);
            }
        }
        final long end = System.currentTimeMillis();

        System.out.println("All stored books:");
        outputBookManagementInquiryPort.books().stream()
                .sorted(Comparator.comparing(Book::title))
                .forEach(book -> System.out.printf("%s (%d)%n", book.title(), book.keywords().size()));

        System.out.printf("Registered %d books in %.3f seconds%n", outputBookManagementInquiryPort.books().size(), (end - start) / 1000.0);
    }

    @Test
    void validateBooks() {
        final Path folder = Paths.get(BOOKS);
        final Set<String> epubFiles = FolderBookRepositoryPersistence.listEpubFiles(folder);

        long start = System.currentTimeMillis();
        epubFiles.parallelStream().forEach(fileName -> {
            logger.warn("loading [{}]", fileName);
            EpubBookLoader.bookForFile(fileName, true);
        });
        long end = System.currentTimeMillis();
        System.out.printf("Loading %d books took %.3f seconds\n", epubFiles.size(), (end - start) / 1000.0);
    }
}
