package com.putoet.mybooks.framework;

import com.putoet.mybooks.domain.*;
import jakarta.activation.MimeType;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EPUBBookLoader {
    private static final Logger logger = LoggerFactory.getLogger(EPUBBookLoader.class);

    public static final Set<String> KEYWORD_SET;
    private static final String KEYWORD = "/keywords";
    static {
        final Path path;
        try {
            final URL url = EPUBBookLoader.class.getResource(KEYWORD);
            if (url == null) {
                logger.error("Resource '{}' not found.", KEYWORD);
                throw new IllegalStateException("Resource '" + KEYWORD + "' not found.");
            }
            path = Paths.get(url.toURI());
        } catch (URISyntaxException exc) {
            throw new IllegalStateException("Not able to load keywords", exc);
        }

        try (Stream<String> lines = Files.lines(path)) {
            KEYWORD_SET = lines.collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException("Not able to load keywords", e);
        }
    }

    public static Book bookForFile(String fileName) {
        final nl.siegmann.epublib.domain.Book epub = readEpub(fileName);
        final Metadata metadata = epub.getMetadata();

        final BookId bookId = extractBookId(fileName, metadata.getIdentifiers());
        final String title = metadata.getTitles().get(0);
        final List<Author> authors = extractAuthors(metadata.getAuthors());
        final String description = String.join("\n", metadata.getTitles());
        final List<MimeType> formats = extractFormat(metadata.getFormat());

        return new Book(bookId, title, authors, description, Set.of(), new MimeTypes(formats));
    }

    protected static nl.siegmann.epublib.domain.Book readEpub(String fileName) {
        final EpubReader epubReader = new EpubReader();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            logger.info("Reading {}", fileName);
            return epubReader.readEpub(fis);
        } catch (IOException | RuntimeException exc) {
            logger.error("Error reading epub '{}': {}", fileName, exc.getMessage());
            throw new IllegalArgumentException("Could not read epub file " + fileName, exc);
        }
    }

    private static List<MimeType> extractFormat(String format) {
        if ("application/epub+zip".equalsIgnoreCase(format))
            return List.of(MimeTypes.EPUB);

        throw new IllegalArgumentException("Invalid format: " + format);
    }

    @SuppressWarnings("unused")
    protected static BookId extractBookId(String fileName, List<Identifier> identifiers) {
        if (identifiers.isEmpty())
            return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());

        final Optional<String> isbn = findISBNIdentifier(identifiers);
        String id = (isbn.orElseGet(() -> identifiers.get(0).getValue()).toLowerCase());
        id = id.replace("urn:", "");
        id = id.replace("uuid:", "");
        id = id.replace("isbn:", "");
        id = id.replace("isbn", "");
        id = id.trim();
        id = id.replace(' ', '-');

        if (ISBN.isValid(id))
            return new BookId(BookId.BookIdScheme.ISBN, id);

        try {
            final URL url = new URL(id);
            return new BookId(BookId.BookIdScheme.URL, id);
        } catch (MalformedURLException ignored) {
        }

        try {
            final UUID uuid = UUID.fromString(id);
            return new BookId(BookId.BookIdScheme.UUID, id);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            // URI's cannot be trusted (I found) so, replace them with UUID
            final URI uri = new URI(id);
            return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
        } catch (URISyntaxException ignored) {
        }

        logger.warn("Invalid book identifier '{}' for {}, generated a uuid", id, fileName);
        return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
    }

    private static Optional<String> findISBNIdentifier(List<Identifier> identifiers) {
        return identifiers.stream()
                .filter(i -> i.getScheme().equalsIgnoreCase(Identifier.Scheme.ISBN))
                .map(Identifier::getValue)
                .findFirst();
    }

    private static List<Author> extractAuthors(List<nl.siegmann.epublib.domain.Author> authors) {
        return authors.stream()
                .map(author -> author.getFirstname() + " " + author.getLastname())
                .map(name -> name.replace(" and ", ", "))
                .map(name -> name.replace(" & ", ", "))
                .map(name -> name.replace(" - ", ", "))
                .map(name -> name.replace(" en ", ", "))
                .map(name -> name.replace(" met ", ", "))
                .map(name -> name.replace("\n", ", "))
                .map(name -> name.split(", "))
                .flatMap(Arrays::stream)
                .filter(name -> !name.isBlank())
                .map(EPUBBookLoader::splitName)
                .map(name -> new Author(AuthorId.withoutId(), name, new HashMap<>()))
                .toList();
    }

    private static String splitName(String name) {
        name = name.trim();
        int last = name.lastIndexOf(' ');
        if (last != -1) {
            String lastName = name.substring(last + 1);
            String firstName = name.substring(0, last);

            if ((lastName.toLowerCase().startsWith("jr") || lastName.endsWith(")")) && firstName.lastIndexOf(' ') != -1) {
                last = firstName.lastIndexOf(' ');
                lastName = firstName.substring(last + 1) + " " + lastName;
                firstName = firstName.substring(0, last);
            }
            name = lastName + ", " + firstName;
        }

        return name;
    }
}
