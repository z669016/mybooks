package com.putoet.mybooks;

import com.putoet.mybooks.books.adapter.out.persistence.folder.FolderBookRepository;
import com.putoet.mybooks.books.adapter.out.persistence.jdbc.H2BookRepository;
import com.putoet.mybooks.books.adapter.out.persistence.jdbc.H2UserRepository;
import com.putoet.mybooks.books.domain.security.AccessRole;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled
@SpringBootTest
class MybooksApplicationDatabaseCreateTest {

    public static final String BOOKS_FOLDER = "/Users/renevanputten/OneDrive/Books";
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    /*
     * Create a database from the books folder, import all books and authors.
     * Load 2 default users into the "users" table.
     *
     * For this test to run against a persistent database (not ad-hoc created for only this test),
     * you need to manually delete the database file (books.mv.db).
     */
    void createDatabaseFromBookFolder() {
        System.out.println("[createDatabaseFromBookFolder v0.1]");

        final var bookRepository = new H2BookRepository(jdbcTemplate);
        final var userRepository = new H2UserRepository(jdbcTemplate);
        System.out.println("Book repository: " + bookRepository);
        
        loadBooksFromFolder(bookRepository);
        loadDefaultUsers(userRepository);
    }

    private static void loadBooksFromFolder(H2BookRepository bookRepository) {
        System.out.println("Load books from the folder repository...");
        final var folderBooks = new FolderBookRepository(Path.of(BOOKS_FOLDER));

        System.out.println("Delete books and authors from the book repository...");
        bookRepository.forgetAllBooks();
        bookRepository.forgetAllAuthors();

        System.out.println("Store all books and authors from the folder into the database...");
        final var allFolderBooks = folderBooks.findBooks();
        final var allFolderAuthors = folderBooks.findAuthors();
        for (var author : allFolderAuthors) {
            bookRepository.registerAuthor(author.id(), author.version(), author.name(), author.sites());
        }

        for (var book : allFolderBooks) {
            bookRepository.registerBook(book.id(), book.title(), book.authors(), book.formats(), book.keywords());
        }

        System.out.printf("Loaded %d books, and %d authors.%n", allFolderBooks.size(), allFolderAuthors.size());
        System.out.println();

        assertFalse(allFolderBooks.isEmpty());
        assertFalse(allFolderAuthors.isEmpty());
    }

    private void loadDefaultUsers(H2UserRepository userRepository) {
        System.out.println("Delete users from user repository...");
        userRepository.forgetAllUsers();

        System.out.println("Load default users...");
        userRepository.registerUser("z669016@gmail.com", "Z669016", "1password!", AccessRole.ADMIN);
        userRepository.registerUser("putoet@outlook.com", "PUTOET", "2password!", AccessRole.USER);

        final var allUsers = userRepository.findUsers();
        System.out.printf("Loaded %d users.%n", allUsers.size());

        assertFalse(allUsers.isEmpty());
    }
}