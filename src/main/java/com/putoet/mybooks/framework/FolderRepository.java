package com.putoet.mybooks.framework;

import com.google.common.base.Joiner;
import com.putoet.mybooks.application.port.out.BookInquiryRepository;
import com.putoet.mybooks.domain.*;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.epub.EpubReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class FolderRepository
 * Repository based on the file system. Loads recursively the data from EPUB files in the root folder provided at
 * construction, and extracts Book and Author data from the files. Data cannot be written to this repository
 */
public class FolderRepository implements BookInquiryRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path folder;
    private final Set<String> files;
    private final Map<AuthorId,Author> authors;
    private final Map<BookId, Book> books;

    public FolderRepository(Path folder) {
        logger.info("FolderRepository({})", folder);
        Objects.requireNonNull(folder, "Book folder must be provided");

        this.folder = folder;
        this.files = listEpubFiles(folder);
        this.books = booksForFiles(files);
        this.authors = authorsForBooks(books);
    }

    private Map<AuthorId, Author> authorsForBooks(Map<BookId, Book> books) {
        return books.values().stream()
                .flatMap(book -> book.authors().stream())
                .collect(Collectors.toMap(Author::id, author -> author));
    }

    protected static Map<BookId, Book> booksForFiles(Set<String> files) {
        return files.stream()
                .map(FolderRepository::bookForFile)
                .collect(Collectors.toMap(Book::id, book -> book));
    }


    protected static Book bookForFile(String fileName) {
        final EpubReader epubReader = new EpubReader();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            final Metadata metadata = epubReader.readEpub(fis).getMetadata();

            final BookId bookId = extractBookId(metadata.getIdentifiers());
            final String title = metadata.getTitles().get(0);
            final List<Author> authors = extractAuthors(metadata.getAuthors());
            final String description = String.join("\n", metadata.getTitles());
            final List<FormatType> formats = extractFormat(metadata.getFormat());
            return new Book(bookId, title, authors, description, List.of(), formats);
        } catch (IOException exc) {
            throw new IllegalArgumentException("Could not read epub file " + fileName);
        }
    }

    private static List<FormatType> extractFormat(String format) {
        if ("application/epub+zip".equalsIgnoreCase(format))
            return List.of(FormatType.EPUB);

        throw new IllegalArgumentException("Invalid format: " + format);
    }

    @SuppressWarnings("unused")
    protected static BookId extractBookId(List<Identifier> identifiers) {
        Optional<String> identifier = findIdentifier(identifiers, Identifier.Scheme.ISBN);
        if (identifier.isPresent())
            return new BookId(BookId.BookIdScheme.ISBN, identifier.get());

        identifier = findIdentifier(identifiers, Identifier.Scheme.URL);
        if (identifier.isPresent())
            return new BookId(BookId.BookIdScheme.URL, identifier.get());

        identifier = findIdentifier(identifiers, Identifier.Scheme.URI);
        if (identifier.isPresent())
            return new BookId(BookId.BookIdScheme.URI, identifier.get());

        identifier = findIdentifier(identifiers, Identifier.Scheme.UUID);
        if (identifier.isPresent())
            return new BookId(BookId.BookIdScheme.UUID, identifier.get());

        identifier = findIdentifier(identifiers, "");
        if (identifier.isPresent()) {
            final String id = identifier.get();
            if (ISBN.isValid(id))
                return new BookId(BookId.BookIdScheme.ISBN, id);

            try {
                final URL url = new URL(id);
                return new BookId(BookId.BookIdScheme.URL, id);
            } catch (MalformedURLException ignored) {}

            try {
                final URI url = new URI(id);
                return new BookId(BookId.BookIdScheme.URI, id);
            } catch (URISyntaxException ignored) {}

            return new BookId(BookId.BookIdScheme.UUID, id);
        }

        throw new IllegalArgumentException("Could not make any BookId for " + identifiers);
    }

    private static Optional<String> findIdentifier(List<Identifier> identifiers, String schema) {
        return identifiers.stream()
                .filter(i -> i.getScheme().equals(schema))
                .map(Identifier::toString)
                .findFirst();
    }

    private static List<Author> extractAuthors(List<nl.siegmann.epublib.domain.Author> authors) {
        return authors.stream()
                .map(author -> new Author(AuthorId.withoutId(), author.getLastname() + ", " + author.getFirstname(), new HashMap<>()))
                .toList();
    }

    protected static Set<String> listEpubFiles(Path folder) {
        try (Stream<Path> stream = Files.walk(folder)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .filter(p -> p.toLowerCase().endsWith(".epub"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not list books from " + folder.getFileName());
        }
    }

    @Override
    public List<Author> findAuthors() {
        logger.info("findAuthors()");

        return authors.values().stream().toList();
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        logger.info("findAuthorsByName({})", name);

        return authors.values().stream()
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

        return books.values().stream().toList();
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        logger.info("findBooksByTitle({})", title);

        return books.values().stream()
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

        return books.values().stream()
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
