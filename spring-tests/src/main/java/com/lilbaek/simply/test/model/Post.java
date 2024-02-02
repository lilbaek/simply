package com.lilbaek.simply.test.model;

import com.lilbaek.simply.test.converters.YNToBooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity(name = "Post")
public record Post(
                @Column(name = "id")
                @Id
                String id,
                @Column(name = "title")
                String title,
                @Column(name = "slug")
                String slug,
                @Column(name = "enabled")
                @Convert(converter = YNToBooleanConverter.class)
                Boolean enabled,
                @Column(name = "date")
                LocalDate date,
                @Column(name = "time_to_read")
                int timeToRead,
                @Column(name = "tags")
                String tags) {

}

