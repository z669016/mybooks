package com.putoet.mybooks.books.adapter.out.persistence.folder;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class KeywordLoader {
    private static final String KEYWORD = "/keywords";
    public static final Set<String> KEYWORD_SET = loadKeywords(KEYWORD);

    private static Set<String> loadKeywords(String keyword) {
        final Path path;
        try {
            final var url = KeywordLoader.class.getResource(keyword);
            if (url == null) {
                log.error("Resource '{}' not found.", KEYWORD);
                throw new IllegalStateException("Resource '" + KEYWORD + "' not found.");
            }
            path = Paths.get(url.toURI());
        } catch (URISyntaxException exc) {
            throw new IllegalStateException("Not able to load keywords", exc);
        }

        try (var lines = Files.lines(path)) {
            return lines.collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException("Not able to load keywords", e);
        }
    }
}
