package com.putoet.mybooks;

import com.putoet.mybooks.books.adapter.out.persistence.folder.FolderBookRepository;
import com.putoet.mybooks.books.adapter.out.persistence.jdbc.H2BookRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;

@Disabled
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MybooksApplicationDatabaseCreateTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createDatabaseFromBookFolder() {
        final var folderBooks = new FolderBookRepository(Path.of("/Users/renevanputten/OneDrive/Books"));
        final var database = new H2BookRepository(jdbcTemplate);

        System.out.println(database);

        final var allFolderBooks = folderBooks.findBooks();
        final var allFolderAuthors = folderBooks.findAuthors();

        for (var author : allFolderAuthors) {
            database.registerAuthor(author.id(), author.version(), author.name(), author.sites());
        }

        for (var book : allFolderBooks) {
            database.registerBook(book.id(), book.title(), book.authors(), book.formats(), book.keywords());
        }

        final var allDatabaseBooks = database.findBooks();
        final var allDatabaseAuthors = database.findAuthors();

        System.out.printf("Found %d authors and %d books in folder%n", allFolderAuthors.size(), allFolderBooks.size());
        System.out.printf("Stored %d authors and %d books in database%n", allDatabaseAuthors.size(), allDatabaseBooks.size());

        var failed = false;
        for (var author : allDatabaseAuthors) {
            if (!allFolderAuthors.contains(author)) {
                failed = true;
                System.out.println("Author not found in folder: " + author);
                System.out.println("Possible folder matches: " + folderBooks.findAuthorsByName(author.name()));
                System.out.println("Possible database matches: " + database.findAuthorsByName(author.name()));
                System.out.println();
            }
        }

        System.out.printf("Load test %s%n", failed ? "FAILED" : "PASSED");
    }
}