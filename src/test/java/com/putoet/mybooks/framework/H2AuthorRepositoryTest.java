package com.putoet.mybooks.framework;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorTest;
import org.junit.jupiter.api.Test;

class H2AuthorRepositoryTest {
    @Test
    void sqlTestData() {
        final Author author = AuthorTest.author;
        System.out.printf("insert into author values('%s', '%s');%n", author.id().uuid(), author.name());
        System.out.println();
        author.sites().values()
                .forEach(site -> System.out.printf("insert into site values('%s', '%s', '%s', '%s');%n",
                        site.id().uuid(),
                        author.id().uuid(),
                        site.type().name(),
                        site.url())
                );
    }
}