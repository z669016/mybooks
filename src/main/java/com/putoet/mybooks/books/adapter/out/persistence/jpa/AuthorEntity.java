package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "author")
public class AuthorEntity {
    @Id
    @Column(name = "author_id", updatable = false, nullable = false)
    private UUID authorId;

    @Column(name = "version", nullable = false)
    private Instant version;

    @Column(name = "name", nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "site", joinColumns = @JoinColumn(name = "author_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "name")
    @Column(name = "url")
    private Map<String, String> sites = new HashMap<>();

    @ManyToMany(mappedBy = "authors")
    private Set<BookEntity> books = new HashSet<>();

    public AuthorEntity() {}

    public AuthorEntity(UUID authorId, Instant version, String name, Map<String, String> sites, Set<BookEntity> books) {
        this.authorId = authorId;
        this.version = version;
        this.name = name;
        this.sites = sites;
        this.books = books;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public Instant getVersion() {
        return version;
    }

    public void setVersion(Instant version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSites() {
        return sites;
    }

    public void setSites(Map<String, String> sites) {
        this.sites = sites;
    }

    public Set<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(Set<BookEntity> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "AuthorEntity{" +
                "authorId=" + authorId +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", sites=" + sites +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorEntity that)) return false;
        return Objects.equals(getAuthorId(), that.getAuthorId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
