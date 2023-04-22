package com.putoet.mybooks.books.adapter.out.persistence;

import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class EpubBookLoader
 * Loads data from an EPUB file using Apache Tika and returns a Book entity. In case book data cannot be properly
 * extracted, the epub file could be 'repackaged' (unzipped and zipped again) which can do miracles ;-)
 */
public class EpubBookLoader {
    private static final Logger logger = LoggerFactory.getLogger(EpubBookLoader.class);
    public static final int MAX_EPUB_LOAD_SIZE = 10_000_000;

    public static Book bookForFile(String fileName, boolean repair) {
        try {
            return bookForFile(fileName);
        } catch (IOException | NullPointerException exc) {
            if (!repair) {
                logger.error("Could not load book {}", fileName, exc);
                throw new IllegalStateException(exc);
            }
        }

        final Optional<String> temp = Rezipper.repackage(fileName);
        if (temp.isEmpty())
            throw new IllegalStateException("Could not repackage file " + fileName);

        logger.warn("Repackaged '{}' into '{}'", fileName, temp.get());
        return bookForFile(temp.get(), false);
    }

    public static Book bookForFile(String fileName) throws IOException {
        final Tika tika = new Tika();
        final String mimeType = tika.detect(fileName);

        try (InputStream is = new FileInputStream(fileName)) {
            final org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
            final Optional<String> data = loadData(tika, is, metadata);

            checkForError(fileName, metadata);

            final BookId bookId = extractBookId(fileName, metadata.get("dc:identifier"));
            final String title = metadata.get("dc:title");
            final List<Author> authors = extractAuthors(metadata.get("dc:creator"));
            final List<MimeType> formats = List.of(MimeTypes.toMimeType(mimeType));
            final Set<String> keywords = data.isPresent() ? findKeywords(data.get()) : Set.of();

            return new Book(bookId, title, authors, keywords, new MimeTypes(formats));
        }
    }

    private static void checkForError(String fileName, Metadata metadata) {
        for (String key : metadata.names()) {
            if (key.startsWith("X-TIKA")) {
                final String[] names = key.split(":");
                if ("EXCEPTION".equalsIgnoreCase(names[1])) {
                    logger.error("TIKA Exception: {}", metadata.get(key));
                    logger.info("metadata={}", metadata);
                }
            }
        }

        if (metadata.get("dc:title") == null) {
            logger.error("Book '{}' doesnt have a title", fileName);
        }
    }

    private static Optional<String> loadData(Tika tika, InputStream is, org.apache.tika.metadata.Metadata metadata) {
        try {
            return Optional.of(tika.parseToString(is, metadata, MAX_EPUB_LOAD_SIZE));
        } catch (TikaException | IOException e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private static Set<String> findKeywords(String data) {
        return keywordTrie().parseText(data).stream()
                .map(Emit::getKeyword)
                .collect(Collectors.toSet());
    }

    private static Trie keywordTrie() {
        final Trie.TrieBuilder builder = Trie.builder()
                .onlyWholeWords()
                .ignoreCase();
        KeywordLoader.KEYWORD_SET.forEach(builder::addKeyword);
        return builder.build();
    }

    protected static BookId extractBookId(String fileName, String identifier) {
        if (identifier == null || identifier.isBlank())
            return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());

        String id = identifier.toLowerCase();
        id = id.replace("urn:", "");
        id = id.replace("uuid:", "");
        id = id.replace("isbn:", "");
        id = id.replace("isbn", "");
        id = id.trim();
        id = id.replace(' ', '-');

        if (ISBN.isValid(id))
            return new BookId(BookId.BookIdScheme.ISBN, id);
        try {
            new URL(id);
            return new BookId(BookId.BookIdScheme.URL, id);
        } catch (MalformedURLException ignored) {
        }

        try {
            UUID.fromString(id);
            return new BookId(BookId.BookIdScheme.UUID, id);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            // URI's cannot be trusted to be unique (I found) so, replace them with UUID
            new URI(id);
            return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
        } catch (URISyntaxException ignored) {
        }

        logger.warn("Invalid book identifier '{}' for {}, generated a uuid", id, fileName);
        return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
    }

    private static List<Author> extractAuthors(String authors) {
        if (authors == null || authors.isBlank())
            return List.of();

        authors = authors.replace(" and ", ", ");
        authors = authors.replace(" & ", ", ");
        authors = authors.replace(" - ", ", ");
        authors = authors.replace(" en ", ", ");
        authors = authors.replace(" met ", ", ");
        authors = authors.replace("\n", ", ");

        return Arrays.stream(authors.split(", "))
                .filter(name -> !name.isBlank())
                .map(EpubBookLoader::splitName)
                .map(name -> new Author(AuthorId.withoutId(), Instant.now(), name, new HashMap<>()))
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
