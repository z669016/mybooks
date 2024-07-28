package com.putoet.mybooks.books.adapter.out.persistence.folder;

import com.putoet.mybooks.books.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

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
@Slf4j
public final class EpubBookLoader {
    public static final int MAX_EPUB_LOAD_SIZE = 10_000_000;

    private EpubBookLoader() {}


    public static Book bookForFile(String fileName, boolean repair) {
        try {
            return bookForFile(fileName);
        } catch (IOException | NullPointerException exc) {
            if (!repair) {
                log.error("Could not load book {}", fileName, exc);
                throw new IllegalStateException(exc);
            }
        }

        final Optional<String> temp = Rezipper.repackage(fileName);
        if (temp.isEmpty())
            throw new IllegalStateException("Could not repackage file " + fileName);

        log.warn("Repackaged '{}' into '{}'", fileName, temp.get());
        return bookForFile(temp.get(), false);
    }

    public static Book bookForFile(String fileName) throws IOException {
        final var tika = new Tika();
        final var mimeType = tika.detect(fileName);

        try (var is = new FileInputStream(fileName)) {
            final var metadata = new org.apache.tika.metadata.Metadata();
            final var data = loadData(tika, is, metadata);

            checkForError(fileName, metadata);

            final var bookId = extractBookId(fileName, metadata.get("dc:identifier"));
            final var title = metadata.get("dc:title");
            final var authors = extractAuthors(metadata.get("dc:creator"));
            final var formats = Set.of(MimeTypes.toMimeType(mimeType));
            final var keywords = data.isPresent() ? findKeywords(data.get()) : Set.<String>of();

            return new Book(bookId, title, authors, keywords, formats);
        }
    }

    private static void checkForError(String fileName, Metadata metadata) {
        for (var key : metadata.names()) {
            if (key.startsWith("X-TIKA")) {
                final var names = key.split(":");
                if ("EXCEPTION".equalsIgnoreCase(names[1])) {
                    log.error("TIKA Exception: {}", metadata.get(key));
                    log.info("metadata={}", metadata);
                }
            }
        }

        if (metadata.get("dc:title") == null) {
            log.error("Book '{}' doesnt have a title", fileName);
        }
    }

    private static Optional<String> loadData(Tika tika, InputStream is, org.apache.tika.metadata.Metadata metadata) {
        try {
            return Optional.of(tika.parseToString(is, metadata, MAX_EPUB_LOAD_SIZE));
        } catch (TikaException | IOException e) {
            log.error(e.getMessage(), e);
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

    private static BookId extractBookId(String fileName, String identifier) {
        if (identifier == null || identifier.isBlank())
            return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());

        var id = identifier.toLowerCase();
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

        log.warn("Invalid book identifier '{}' for {}, generated a uuid", id, fileName);
        return new BookId(BookId.BookIdScheme.UUID, UUID.randomUUID().toString());
    }

    private static Set<Author> extractAuthors(String authors) {
        if (authors == null || authors.isBlank())
            return Set.of();

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
                .collect(Collectors.toSet());
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
