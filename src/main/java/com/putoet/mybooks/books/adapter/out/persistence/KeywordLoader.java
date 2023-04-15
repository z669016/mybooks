package com.putoet.mybooks.books.adapter.out.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeywordLoader {
    private static final Logger logger = LoggerFactory.getLogger(KeywordLoader.class);

    private static final String KEYWORD = "/keywords";
    public static final Set<String> KEYWORD_SET = loadKeywords(KEYWORD);

    private static Set<String> loadKeywords(String keyword) {
        final Path path;
        try {
            final URL url = KeywordLoader.class.getResource(keyword);
            if (url == null) {
                logger.error("Resource '{}' not found.", KEYWORD);
                throw new IllegalStateException("Resource '" + KEYWORD + "' not found.");
            }
            path = Paths.get(url.toURI());
        } catch (URISyntaxException exc) {
            throw new IllegalStateException("Not able to load keywords", exc);
        }

        try (Stream<String> lines = Files.lines(path)) {
            return lines.collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException("Not able to load keywords", e);
        }
    }
}
