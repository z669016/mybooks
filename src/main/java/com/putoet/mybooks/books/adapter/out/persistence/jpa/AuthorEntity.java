package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "author")
class AuthorEntity {
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
