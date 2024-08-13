package com.putoet.mybooks.books.adapter.out.persistence.folder;

import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceQueryPort;
import com.putoet.mybooks.books.domain.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

        deduplicateAuthors(books, authors);
    }

    private void deduplicateAuthors(Map<BookId, Book> books, Map<AuthorId, Author> authors) {
        // Group authors by name
        final var names = authors.values().stream()
                .collect(Collectors.groupingBy(Author::name));

        // For each book, check if the author is already in the group and if so,
        // replace the author with the first author in the group.
        // Use a separate list to go through, so the map can be updated along the way.
        final var bookIds = new ArrayList<>(books.keySet());
        for (final var id : bookIds) {
            var book = books.get(id);
            final var bookAuthors = book.authors();
            for (var author : bookAuthors) {
                final var authorGroup = names.get(author.name());

                // If the author isn't the same as the first in the list, it's a duplicate
                if (!author.id().equals(authorGroup.get(0).id())) {
                    // create a new list of authors with the current one, replaced with the first in the group
                    final var newAuthors = new HashSet<>(book.authors());
                    newAuthors.remove(author);
                    newAuthors.add(authorGroup.get(0));

                    // put a new book in the map with the new authors
                    book = new Book(book.id(), book.title(), newAuthors, book.keywords(), book.formats());
                    books.put(book.id(), book);

                    // remove the replaced author from the author map
                    authors.remove(author.id());
                }
            }
        }

        for (var book : books.values()) {
            for (var author : book.authors()) {
                if (!authors.containsKey(author.id())) {
                    System.out.printf("Author %s not found in authors, for book %s%n", author, book);
                    System.out.printf("names: %s%n", names.get(author.name()));
                }
            }
        }
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

        return Authors.ordered(authors.values());
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        log.info("findAuthorsByName({})", name);

        return Authors.ordered(
                authors.values().parallelStream()
                        .filter(author -> author.name().toLowerCase().contains(name.toLowerCase()))
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public Author findAuthorById(AuthorId authorId) {
        log.info("findAuthorById({})", authorId);

        return authors.get(authorId);
    }

    @Override
    public Set<Book> findBooks() {
        log.info("findBooks()");

        return Books.ordered(books.values());
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        log.info("findBooksByTitle({})", title);

        return Books.ordered(
                books.values().parallelStream()
                        .filter(book -> book.title().toLowerCase().contains(title.toLowerCase()))
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public Book findBookById(BookId bookId) {
        log.info("findBookById({})", bookId);

        return books.get(bookId);
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        log.info("findBooksByAuthorId({})", authorId);

        return Books.ordered(
                books.values().parallelStream()
                        .filter(book -> book.authors().stream().anyMatch(author -> author.id().equals(authorId)))
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public String toString() {
        return String.format("%s(folder=%s)", this.getClass().getName(), folder);
    }
}
