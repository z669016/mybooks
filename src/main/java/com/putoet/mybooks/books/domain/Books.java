package com.putoet.mybooks.books.domain;

import java.util.*;

public final class Books {
    private Books() {
    }

    public static Set<Book> ordered(Collection<Book> books) {
        final var ordered = new TreeSet<>(
                Comparator.comparing(Book::title)
                        .thenComparing(b -> b.id().schema())
                        .thenComparing(b -> b.id().id())
        );
        ordered.addAll(books);
        return Collections.unmodifiableSet(ordered);
    }
}
