package com.putoet.mybooks;

import com.putoet.mybooks.application.BookInquiryService;
import com.putoet.mybooks.application.BookService;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.framework.EPUBBookLoader;
import com.putoet.mybooks.framework.FolderRepository;
import com.putoet.mybooks.framework.H2BookRepository;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
class MybooksApplicationTests {
    private static final String BOOKS = "/Users/renevanputten/OneDrive/Documents/Books";
    private static final String LEANPUB = "/Users/renevanputten/OneDrive/Documents/Books/leanpub";
    private static final Logger logger = LoggerFactory.getLogger(MybooksApplicationTests.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadBooks() {
        final long start = System.currentTimeMillis();
        final H2BookRepository database = new H2BookRepository(jdbcTemplate);
        final FolderRepository folder = new FolderRepository(Paths.get(BOOKS));

        final BookInquiryService inquiry = new BookInquiryService(folder);
        final BookService service = new BookService(database);

        final Map<String, Author> storedAuthors = new HashMap<>();
        for (Author author : inquiry.authors()) {
            try {
                storedAuthors.put(author.name(), service.registerAuthor(author.name(), author.sites()));
            } catch (RuntimeException exc) {
                logger.error("Failed to register author '" + author + "'", exc);
            }
        }

        storedAuthors.values().stream()
                .sorted(Comparator.comparing(Author::name))
                .forEach(System.out::println);

        for (Book book : inquiry.books()) {
            final List<Author> authors = book.authors().stream()
                    .map(author -> storedAuthors.get(author.name()))
                    .distinct()
                    .toList();
            try {
                service.registerBook(book.id(), book.title(), authors, book.description(), book.formats());
            } catch (RuntimeException exc) {
                logger.error("Failed to register book '" + book + "'", exc);
            }
        }
        final long end = System.currentTimeMillis();

        System.out.println("All stored books:");
        service.books().stream().sorted(Comparator.comparing(Book::title)).forEach(book -> System.out.println(book.title()));

        System.out.printf("Registered %d books in %.3f seconds%n", service.books().size(), (end - start) / 1000.0);
    }

    @Test
    void validateBooks() {
        final Path folder = Paths.get(BOOKS);
        final Set<String> epubFiles = FolderRepository.listEpubFiles(folder);

        long start = System.currentTimeMillis();
        epubFiles.parallelStream().forEach(fileName -> {
            logger.warn("loading [{}]", fileName);
            EPUBBookLoader.bookForFile(fileName);
        });
        long end = System.currentTimeMillis();
        System.out.printf("Loading %d books took %.3f seconds\n", epubFiles.size(), (end - start) / 1000.0);
    }

    private static final String[] KEYWORDS = {
            "algorithm",
            "cloud",
            "container",
            "security",
            "artificial intelligence",
            "machine learning",
            "blockchain",
            "big data",
            "internet of things",
            "iot",
            "virtualization",
            "augmented reality",
            "quantum",
            "devops",
            "edge computing",
            "data analytics",
            "robotics",
            "natural language processing",
            "nlp",
            "5G",
            "network",
            "wireless",
            "development",
            "engineering",
            "database",
            "user experience",
            "business intelligence",
            "storage",
            "chatbot",
            "voice recognition",
            "predictive",
            "analytics",
            "serverless",
            "API",
            "microservice",
            "agile",
            "ci/cd",
            "continuous integration",
            "continuous delivery",
            "biometric",
            "python",
            "java",
            "javascript",
            "c++",
            "swift",
            "kotlin",
            "ruby",
            "php",
            "go language",
            "golang",
            "typescript",
            "scala",
            "perl",
            "rust",
            "rest",
            "openai",
            "android",
            "windows",
            "linux",
            "kali",
            "ios",
            "macos",
            "active directory",
            "architecture",
            "angular",
            "awk",
            "arduino",
            "bdd",
            "bpm",
            "bpmn",
            "d3",
            "functional programming",
            "gradle",
            "groovy",
            "html",
            "xhtml",
            "nodejs",
            "nosql",
            "sql",
            "osb",
            "osgi",
            "performance",
            "reactive",
            "sre",
            "spring boot",
            "spring framework",
            "web assembly"
    };

    private static final Set<String> KEYWORD_SET;
    static{
        KEYWORD_SET = Arrays.stream(KEYWORDS).collect(Collectors.toSet());
    }

    private static Set<String> keywords(String text) {
        return Arrays.stream(text.split("\n"))
                .filter(s -> !s.isEmpty())
                .map(s -> KEYWORD_SET.parallelStream()
                            .filter(s::contains)
                            .collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private static final Map<String, POSModel> MODELS = new HashMap<>();
    private static final Map<String, String> RESOURCES = Map.of("en", "/en-pos-maxent.bin");

    static {
        for (String language : RESOURCES.keySet()) {
            try {
                MODELS.put(language, model(language, RESOURCES.get(language)));
            } catch (RuntimeException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

    public static POSModel model(String language, String resourceName) {
        try (InputStream is = MybooksApplicationTests.class.getResourceAsStream(resourceName)) {
            if (is == null)
                throw new IllegalStateException("Language model '" + resourceName + "' for language '" + language + "' not available");

            return new POSModel(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Set<String> extractNouns(String text, String language) {
        final POSTaggerME tagger = new POSTaggerME(MODELS.get(language));
        final String[] words = SimpleTokenizer.INSTANCE.tokenize(text);
        final String[] tags = tagger.tag(words);

        return IntStream.range(0, words.length)
                .filter(i -> tags[i].startsWith("NN"))
                .mapToObj(i -> words[i])
                .filter(s -> s.length() > 2)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Test
    void tika() throws IOException, MimeTypeParseException {
        final Path path = Paths.get(LEANPUB + "/humansvscomputers.epub");
        final Tika tika = new Tika();
        final MimeType mime = new MimeType(tika.detect(path));

        System.out.printf("MIME type is %s%n", mime);
        final Metadata metadata = new Metadata();
        try (InputStream is = Files.newInputStream(path)) {
            final String text = tika.parseToString(is, metadata);
            final String language = metadata.get("dc:language");
            final String author = metadata.get("dc:creator");

            final Set<String> keywords = keywords(text);

            System.out.println("Author: " + author);
            System.out.println("Keywords: " + keywords);
        } catch (TikaException e) {
            throw new RuntimeException(e);
        }
    }
}
