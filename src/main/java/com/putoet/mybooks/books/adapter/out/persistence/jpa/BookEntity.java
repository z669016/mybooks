package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "book")
public class BookEntity {
    @EmbeddedId
    private BookIdEntity bookId;

    @Column(name = "title", nullable = false)
    private String title;

    @ElementCollection
    @CollectionTable(name = "book_format", joinColumns = {
            @JoinColumn(name = "book_id_type", referencedColumnName = "book_id_type"),
            @JoinColumn(name = "book_id", referencedColumnName = "book_id")
    })
    @Column(name = "format")
    private Set<String> formats;


    @ManyToMany
    @JoinTable(name = "book_author", joinColumns = {
            @JoinColumn(name = "book_id_type", referencedColumnName = "book_id_type"),
            @JoinColumn(name = "book_id", referencedColumnName = "book_id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "author_id", referencedColumnName = "author_id")
    })
    private Set<AuthorEntity> authors;

    @ElementCollection
    @CollectionTable(name = "book_key_word", joinColumns = {
            @JoinColumn(name = "book_id_type", referencedColumnName = "book_id_type"),
            @JoinColumn(name = "book_id", referencedColumnName = "book_id")
    })

    @Column(name = "keyword")
    private Set<String> keywords;

    @Override
    public String toString() {
        return "BookEntity{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", formats=" + formats +
                ", authors=" + authors +
                ", keywords=" + keywords +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookEntity that)) return false;
        return Objects.equals(getBookId(), that.getBookId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
