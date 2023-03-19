package com.putoet.mybooks;

import com.putoet.mybooks.application.BookInquiryService;
import com.putoet.mybooks.application.BookService;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.framework.EPUBBookLoader;
import com.putoet.mybooks.framework.FolderRepository;
import com.putoet.mybooks.framework.H2BookRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
class MybooksApplicationTests {
    private static final String BOOKS = "/Users/renevanputten/OneDrive/Documents/Books";
    private static final String LEANPUB = "/Users/renevanputten/OneDrive/Documents/Books/leanpub";

    private static final Logger logger = LoggerFactory.getLogger(MybooksApplicationTests.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadBooks() {
        final H2BookRepository database = new H2BookRepository(jdbcTemplate);
        final FolderRepository folder = new FolderRepository(Paths.get(BOOKS));

        final BookInquiryService inquiry = new BookInquiryService(folder);
        final BookService service = new BookService(database);

        final Map<String, Author> storedAuthors = new HashMap<>();
        for (Author author : inquiry.authors()) {
            try {
                storedAuthors.put(author.name(), service.registerAuthor(author.name(), author.sites()));
            } catch (RuntimeException exc) {
                logger.error("Failed to register author '" + author + "'", exc);
            }
        }

        storedAuthors.values().stream()
                .sorted(Comparator.comparing(Author::name))
                .forEach(System.out::println);

        for (Book book : inquiry.books()) {
            final List<Author> authors = book.authors().stream()
                    .map(author -> storedAuthors.get(author.name()))
                    .distinct()
                    .toList();
            try {
                service.registerBook(book.id(), book.title(), authors, book.description(), book.formats());
            } catch (RuntimeException exc) {
                logger.error("Failed to register book '" + book + "'", exc);
            }
        }

        System.out.println("All stored books:");
        service.books().stream().sorted(Comparator.comparing(Book::title)).forEach(book -> System.out.println(book.title()));
    }

    @Test
    void validateBooks() {
        final Path folder = Paths.get(BOOKS);
        final Set<String> epubFiles = FolderRepository.listEpubFiles(folder);

        long start = System.currentTimeMillis();
        epubFiles.parallelStream().forEach(EPUBBookLoader::bookForFile);
        long end = System.currentTimeMillis();
        System.out.printf("Loading %d books took %.4f ms\n", epubFiles.size(), (end-start)/1000.0);
    }
}
