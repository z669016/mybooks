package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

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

    public BookEntity() {
    }

    public BookEntity(BookIdEntity bookId, String title, Set<String> formats, Set<AuthorEntity> authors, Set<String> keywords) {
        this.bookId = bookId;
        this.title = title;
        this.formats = formats;
        this.authors = authors;
        this.keywords = keywords;
    }

    public BookIdEntity getBookId() {
        return bookId;
    }

    public void setBookId(BookIdEntity bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getFormats() {
        return formats;
    }

    public void setFormats(Set<String> formats) {
        this.formats = formats;
    }

    public Set<AuthorEntity> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<AuthorEntity> authors) {
        this.authors = authors;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

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
