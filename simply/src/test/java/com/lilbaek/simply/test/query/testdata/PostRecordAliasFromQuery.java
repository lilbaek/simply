package com.lilbaek.simply.test.query.testdata;

import com.lilbaek.simply.test.domain.PostType;
import com.lilbaek.simply.test.domain.PostTypeConverter;
import com.lilbaek.simply.test.domain.YNToBooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PostRecordAliasFromQuery (
        @Column(name = "ENABLEDALIAS")
        @Convert(converter = YNToBooleanConverter.class)
        Boolean enabled,
        @Column(name = "DATEALIAS")
        LocalDate date,
        @Column(name = "TYPEALIAS")
        @Convert(converter = PostTypeConverter.class)
        PostType type,
        @Column(name = "STARSALIAS")
        BigDecimal stars
) {
}
