package com.putoet.mybooks;

import com.putoet.mybooks.books.adapter.out.persistence.EpubBookLoader;
import com.putoet.mybooks.books.adapter.out.persistence.FolderBookRepository;
import com.putoet.mybooks.books.adapter.out.persistence.H2BookRepository;
import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MybooksApplication.class)
@AutoConfigureMockMvc
class MybooksLoaderTests {
    private static final String BOOKS = "/Users/renevanputten/OneDrive/Documents/Books";
    private static final String LEANPUB = "/Users/renevanputten/OneDrive/Documents/Books/leanpub";
    private static final Logger logger = LoggerFactory.getLogger(MybooksLoaderTests.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadBooks() {
        final long start = System.currentTimeMillis();
        final H2BookRepository database = new H2BookRepository(jdbcTemplate);
        final FolderBookRepository folder = new FolderBookRepository(Paths.get(BOOKS));

        final BookInquiryService folderBookInquiryService = new BookInquiryService(folder);
        final BookInquiryService bookInquiryService = new BookInquiryService(database);
        final BookUpdateService bookUpdateService = new BookUpdateService(database);

        final Map<String, Author> storedAuthors = new HashMap<>();
        for (Author author : folderBookInquiryService.authors()) {
            try {
                storedAuthors.put(author.name(), bookUpdateService.registerAuthor(author.name(), author.sites()));
            } catch (RuntimeException exc) {
                logger.error("Failed to register author '" + author + "'", exc);
            }
        }

        storedAuthors.values().stream()
                .sorted(Comparator.comparing(Author::name))
                .forEach(System.out::println);

        for (Book book : folderBookInquiryService.books()) {
            final List<Author> authors = book.authors().stream()
                    .map(author -> storedAuthors.get(author.name()))
                    .distinct()
                    .toList();
            try {
                bookUpdateService.registerBook(book.id(), book.title(), authors, book.formats().mimeTypes(), book.keywords());
            } catch (RuntimeException exc) {
                logger.error("Failed to register book '" + book + "'", exc);
            }
        }
        final long end = System.currentTimeMillis();

        System.out.println("All stored books:");
        bookInquiryService.books().stream()
                .sorted(Comparator.comparing(Book::title))
                .forEach(book -> System.out.printf("%s (%d)%n", book.title(), book.keywords().size()));

        System.out.printf("Registered %d books in %.3f seconds%n", bookInquiryService.books().size(), (end - start) / 1000.0);
    }

    @Test
    void validateBooks() {
        final Path folder = Paths.get(BOOKS);
        final Set<String> epubFiles = FolderBookRepository.listEpubFiles(folder);

        long start = System.currentTimeMillis();
        epubFiles.parallelStream().forEach(fileName -> {
            logger.warn("loading [{}]", fileName);
            EpubBookLoader.bookForFile(fileName, true);
        });
        long end = System.currentTimeMillis();
        System.out.printf("Loading %d books took %.3f seconds\n", epubFiles.size(), (end - start) / 1000.0);
    }
}