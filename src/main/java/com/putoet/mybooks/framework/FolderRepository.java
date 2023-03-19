package com.putoet.mybooks.framework;

import com.google.common.base.Joiner;
import com.putoet.mybooks.application.port.out.BookInquiryRepository;
import com.putoet.mybooks.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * class FolderRepository
 * Repository based on the file system. Loads recursively the data from EPUB files in the root folder provided at
 * construction, and extracts Book and Author data from the files. Data cannot be written to this repository
 */
public class FolderRepository implements BookInquiryRepository {
    private static final Logger logger = LoggerFactory.getLogger(FolderRepository.class);

    private final Path folder;
    private final Set<String> files;
    private final Map<AuthorId, Author> authors;
    private final Map<BookId, Book> books;

    public FolderRepository(Path folder) {
        logger.info("FolderRepository({})", folder);
        Objects.requireNonNull(folder, "Book folder must be provided");

        this.folder = folder;
        this.files = listEpubFiles(folder);
        this.books = booksForFiles(files);
        this.authors = authorsForBooks(books);
    }

    public static Set<String> listEpubFiles(Path folder) {
        try (Stream<Path> stream = Files.walk(folder)) {
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
                .map(EPUBBookLoader::bookForFile)
                .reduce(new HashMap<>(),
                        FolderRepository::addBookWithoutDuplicateIds,
                        FolderRepository::addBookWithoutDuplicateIds
                );
    }

    private static HashMap<BookId, Book> addBookWithoutDuplicateIds(HashMap<BookId, Book> hashMap1, HashMap<BookId, Book> hashMap2) {
        if (hashMap1 == hashMap2)
            return hashMap1;

        hashMap2.values().forEach(book-> addBookWithoutDuplicateIds(hashMap1,book));

        return hashMap1;
    }

    private static HashMap<BookId, Book> addBookWithoutDuplicateIds(HashMap<BookId, Book> hashMap, Book book) {
        if (hashMap.containsKey(book.id())) {
            logger.warn("Duplicate id {}, generated new book id for {}", book.id(), book.title());
            book = new Book(new BookId(), book.title(), book.authors(), book.description(), book.keywords(), book.formats());
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
    public List<Author> findAuthors() {
        logger.info("findAuthors()");

        return authors.values().stream().toList();
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        logger.info("findAuthorsByName({})", name);

        return authors.values().parallelStream()
                .filter(author -> author.name().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    @Override
    public Author findAuthorById(AuthorId authorId) {
        logger.info("findAuthorById({})", authorId);

        return authors.get(authorId);
    }

    @Override
    public List<Book> findBooks() {
        logger.info("findBooks()");

        return List.copyOf(books.values());
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        logger.info("findBooksByTitle({})", title);

        return books.values().parallelStream()
                .filter(book -> book.title().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    @Override
    public Book findBookById(BookId bookId) {
        logger.info("findBookById({})", bookId);

        return books.get(bookId);
    }

    @Override
    public List<Book> findBooksByAuthorId(AuthorId authorId) {
        logger.info("findBooksByAuthorId({})", authorId);

        return books.values().parallelStream()
                .filter(book -> book.authors().stream().anyMatch(author -> author.id().equals(authorId)))
                .toList();
    }

    @Override
    public String toString() {
        return "BookFolder{" +
               "folder=" + folder +
               ", files=" + files +
               ", authors=" + Joiner.on(";").withKeyValueSeparator("=").join(authors) +
               ", books=" + books.values() +
               '}';
    }
}
