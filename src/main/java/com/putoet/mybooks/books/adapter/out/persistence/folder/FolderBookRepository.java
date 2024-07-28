package com.putoet.mybooks.books.adapter.out.persistence.folder;

import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceQueryPort;
import com.putoet.mybooks.books.domain.Author;
import com.putoet.mybooks.books.domain.AuthorId;
import com.putoet.mybooks.books.domain.Book;
import com.putoet.mybooks.books.domain.BookId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * class FolderRepository
 * <p>
 * Repository based on the file system. Loads recursively the data from EPUB files in the root folder provided at
 * * construction, and extracts Book and Author data from the files. Data cannot be written to this repository as it
 * * extends BookInquiryRepository (which only provides read operations).
 * </p>
 * <p>
 * On startup the constructor recursively loads all books from root folder and its sub folders, and creates a hash map
 * with books (key = BookId) and authors (key = AuthorId) for search optimization only.
 * The root folder is 'walked' to search for all EPUB books using a parallel stream. After all EPUB file names have
 * been collected, the list is processed (again as parallel stream) to load all data from the books.
 * </p>
 */
@Slf4j
public class FolderBookRepository implements BookPersistenceQueryPort {
    private final Path folder;
    private final Map<AuthorId, Author> authors;
    private final Map<BookId, Book> books;

    public FolderBookRepository(Path folder) {
        log.info("FolderRepository({})", folder);

        Objects.requireNonNull(folder, "Book folder must be provided");
        final var files = listEpubFiles(folder);

        this.folder = folder;
        this.books = booksForFiles(files);
        this.authors = authorsForBooks(books);
    }

    public static Set<String> listEpubFiles(Path folder) {
        try (var stream = Files.walk(folder)) {
            return stream
                    .parallel()
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .filter(p -> p.toLowerCase().endsWith(".epub"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not list books from " + folder.getFileName());
        }
    }

    protected static Map<BookId, Book> booksForFiles(Set<String> files) {
        return files.parallelStream()
                .map(filename -> EpubBookLoader.bookForFile(filename, true))
                .reduce(new HashMap<>(),
                        FolderBookRepository::addBookWithoutDuplicateIds,
                        FolderBookRepository::addBookWithoutDuplicateIds
                );
    }

    private static HashMap<BookId, Book> addBookWithoutDuplicateIds(HashMap<BookId, Book> hashMap1, HashMap<BookId, Book> hashMap2) {
        if (hashMap1 == hashMap2)
            return hashMap1;

        hashMap2.values().forEach(book -> addBookWithoutDuplicateIds(hashMap1, book));

        return hashMap1;
    }

    private static HashMap<BookId, Book> addBookWithoutDuplicateIds(HashMap<BookId, Book> hashMap, Book book) {
        if (hashMap.containsKey(book.id())) {
            log.warn("Duplicate id {}, generated new book id for {}", book.id(), book.title());
            book = new Book(new BookId(), book.title(), book.authors(), book.keywords(), book.formats());
        }
        hashMap.put(book.id(), book);
        return hashMap;
    }

    private Map<AuthorId, Author> authorsForBooks(Map<BookId, Book> books) {
        return books.values().parallelStream()
                .flatMap(book -> book.authors().stream())
                .collect(toMap(Author::id, author -> author));
    }

    @Override
    public Set<Author> findAuthors() {
        log.info("findAuthors()");

        return Set.copyOf(authors.values());
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        log.info("findAuthorsByName({})", name);

        return authors.values().parallelStream()
                .filter(author -> author.name().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public Author findAuthorById(AuthorId authorId) {
        log.info("findAuthorById({})", authorId);

        return authors.get(authorId);
    }

    @Override
    public Set<Book> findBooks() {
        log.info("findBooks()");

        return Set.copyOf(books.values());
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        log.info("findBooksByTitle({})", title);

        return books.values().parallelStream()
                .filter(book -> book.title().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public Book findBookById(BookId bookId) {
        log.info("findBookById({})", bookId);

        return books.get(bookId);
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        log.info("findBooksByAuthorId({})", authorId);

        return books.values().parallelStream()
                .filter(book -> book.authors().stream().anyMatch(author -> author.id().equals(authorId)))
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return String.format("%s(folder=%s)", this.getClass().getName(), folder);
    }
}
