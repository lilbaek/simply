package com.lilbaek.simply.test.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Post")
public record Post(
        @Column(name = "id")
        @Id
        String id,
        @Column(name = "title")
        String title,
        @Column(name = "enabled")
        @Convert(converter = YNToBooleanConverter.class)
        Boolean enabled,
        @Column(name = "date")
        LocalDate date,
        @Column(name = "type")
        @Convert(converter = PostTypeConverter.class)
        PostType type,
        @Column(name = "stars")
        BigDecimal stars,
        @Transient
        List<String> authorIds) {
}
