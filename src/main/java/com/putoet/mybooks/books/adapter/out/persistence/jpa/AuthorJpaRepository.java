package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface AuthorJpaRepository extends JpaRepository<AuthorEntity, UUID> {
    List<AuthorEntity> findAuthorEntityByNameContainsIgnoreCase(String name);
}
