package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
interface BookJpaRepository extends JpaRepository<BookEntity, BookIdEntity> {
    Set<BookEntity> findBookEntityByTitleContainsIgnoreCase(String title);

    Set<BookEntity> findBookEntityByAuthors_NameContainsIgnoreCase(String name);

    Set<BookEntity> findBookEntityByAuthorsContains(AuthorEntity author);
}
