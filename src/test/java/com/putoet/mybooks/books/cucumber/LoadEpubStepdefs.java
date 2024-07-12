package com.putoet.mybooks.books.cucumber;

import com.putoet.mybooks.books.adapter.out.persistence.FolderBookRepository;
import com.putoet.mybooks.books.adapter.out.persistence.H2BookRepository;
import com.putoet.mybooks.books.adapter.out.persistence.Rezipper;
import com.putoet.mybooks.books.application.BookInquiryService;
import com.putoet.mybooks.books.application.BookUpdateService;
import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.application.port.in.BookManagementUpdatePort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.Book;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class LoadEpubStepdefs extends MyBooksE2EBase {
    private static final String ROOT_FOLDER = "root_folder";
    private static final String ALL_AUTHORS = "all_authors";
    private static final String ALL_BOOKS = "all_books";
    private static final String DURATION = "duration";

    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public LoadEpubStepdefs(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Given("an existing root folder {string}")
    public void anExistingRootFolder(String rootFolder) {
        context.set(ROOT_FOLDER, rootFolder);
    }

    @When("all books are loaded from the the root folder")
    public void allBooksAreLoadedFromTheTheRootFolder() {
        final long start = System.currentTimeMillis();
        final H2BookRepository database = new H2BookRepository(jdbcTemplate);
        final FolderBookRepository folder = new FolderBookRepository(Paths.get(context.get(ROOT_FOLDER, String.class)));

        final BookManagementInquiryPort inputBbookManagementInquiryPort = new BookInquiryService(folder);
        final BookManagementUpdatePort outputBookManagementUpdatePort = new BookUpdateService(database);

        final Map<String, Author> storedAuthors = new HashMap<>();
        final Set<Book> storedBooks = new HashSet<>();

        for (Author author : inputBbookManagementInquiryPort.authors()) {
            try {
                storedAuthors.put(author.name(), outputBookManagementUpdatePort.registerAuthor(author.name(), author.sites()));
            } catch (RuntimeException exc) {
                logger.error("Failed to register author '{}'", author, exc);
            }
        }

        for (Book book : inputBbookManagementInquiryPort.books()) {
            final Set<Author> authors = book.authors().stream()
                    .map(author -> storedAuthors.get(author.name()))
                    .collect(Collectors.toSet());
            try {
                storedBooks.add(outputBookManagementUpdatePort.registerBook(book.id(), book.title(), authors, book.formats(), book.keywords()));
            } catch (RuntimeException exc) {
                logger.error("Failed to register book '{}'", book, exc);
            }
        }

        final long end = System.currentTimeMillis();

        context.set(ALL_AUTHORS, Set.copyOf(storedAuthors.values()));
        context.set(ALL_BOOKS, storedBooks);
        context.set(DURATION, end - start);
    }

    @Then("all authors list is not empty")
    public void allAuthorsListIsNotEmpty() {
        final Set<Author> storedAuthors = context.get(ALL_AUTHORS, Set.class);
        System.out.printf("Loaded %d authors:%n", storedAuthors.size());
        storedAuthors.stream()
                .sorted(Comparator.comparing(author -> author.name().toLowerCase()))
                .map(Author::name)
                .forEach(System.out::println);
        System.out.println();
        assertFalse(storedAuthors.isEmpty());
    }

    @And("all book list is not empty")
    public void allBookListIsNotEmpty() {
        final Set<Book> storedBooks = context.get(ALL_BOOKS, Set.class);
        System.out.printf("Loaded %d books:%n", storedBooks.size());
        storedBooks.stream()
                .sorted(Comparator.comparing(book -> book.title().toLowerCase()))
                .map(Book::title)
                .forEach(System.out::println);
        System.out.println();
        assertFalse(storedBooks.isEmpty());
    }

    @And("loading stats")
    public void rezipperStats() {
        final long duration = context.get(DURATION, Long.class);
        final int repackageCount = Rezipper.repackageCount();
        final int repackageFailedCount = Rezipper.repackageFailedCount();

        System.out.printf("Loading took %d seconds%n", duration/ 1000);
        System.out.printf("%d books had to be repackaged%n", repackageCount);
        if (repackageCount > 0)
            System.out.printf("for %d books repackaging failed%n", repackageFailedCount);

    }
}
