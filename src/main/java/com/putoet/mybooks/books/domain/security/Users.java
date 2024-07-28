package com.putoet.mybooks.books.domain.security;

import java.util.*;

public final class Users {
    private Users() { }

    public static Set<User> ordered(Collection<User> users) {
        final var set = new TreeSet<>(Comparator.comparing(User::name).thenComparing(User::id));
        set.addAll(users);
        return Collections.unmodifiableSet(set);
    }

}
