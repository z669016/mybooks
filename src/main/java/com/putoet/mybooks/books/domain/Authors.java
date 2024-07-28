package com.putoet.mybooks.books.domain;

import java.util.*;

public final class Authors {
    private Authors() { }

    public static Set<Author> ordered(Collection<Author> authors) {
        final var ordered = new TreeSet<>(
                Comparator.comparing(Author::name)
                        .thenComparing(a -> a.id().uuid())
        );
        ordered.addAll(authors);
        return Collections.unmodifiableSet(ordered);
    }
}
