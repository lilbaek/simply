package com.lilbaek.simply.test.query.testdata;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;

import java.util.List;

public record PostRecordWithTransient(
        @Transient
        String otherPropNotInQuery,
        @Column(name = "id") String id,
        @Transient
        List<String> propNotInQuery
) {
}
