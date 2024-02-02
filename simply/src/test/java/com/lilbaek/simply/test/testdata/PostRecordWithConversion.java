package com.lilbaek.simply.test.testdata;

import com.lilbaek.simply.test.domain.PostType;
import com.lilbaek.simply.test.domain.PostTypeConverter;
import com.lilbaek.simply.test.domain.YNToBooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PostRecordWithConversion(
        @Column(name = "enabled")
        @Convert(converter = YNToBooleanConverter.class)
        Boolean enabled,
        @Column(name = "date")
        LocalDate date,
        @Column(name = "type")
        @Convert(converter = PostTypeConverter.class)
        PostType type,
        @Column(name = "stars")
        BigDecimal stars
) {
}
