package com.lilbaek.simply.test.testdata;

import jakarta.persistence.Column;

public record PostRecordFromQuery(
        @Column(name = "id") String id
) {
}
