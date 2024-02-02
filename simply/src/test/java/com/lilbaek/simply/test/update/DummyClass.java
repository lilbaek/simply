package com.lilbaek.simply.test.update;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Post")
public record DummyClass(
        @Column(name = "id")
        String id,
        @Column(name = "title")
        String title
) {
}
