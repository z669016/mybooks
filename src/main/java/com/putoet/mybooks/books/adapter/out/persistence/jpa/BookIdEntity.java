package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class BookIdEntity implements Serializable {
    @Column(name = "book_id_type", updatable = false, nullable = false)
    private String idType;

    @Column(name = "book_id", updatable = false, nullable = false)
    private String id;

    @Override
    public String toString() {
        return "BookIdEntity{" +
                "idType='" + idType + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookIdEntity that)) return false;
        return Objects.equals(getIdType(), that.getIdType()) && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdType(), getId());
    }
}
