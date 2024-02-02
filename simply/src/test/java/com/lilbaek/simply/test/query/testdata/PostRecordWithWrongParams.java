package com.lilbaek.simply.test.query.testdata;

import jakarta.persistence.Column;

public record PostRecordWithWrongParams(
        @Column(name = "id") String id,
        @Column(name = "DOES_NOT_EXIST_IN_QUERY")
        String doesNotExist
) {
}

