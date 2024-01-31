package com.lilbaek.simply.model;

import com.lilbaek.simply.converters.YNToBooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

import java.time.LocalDate;

public record Post(
                @Column(name = "id")
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

