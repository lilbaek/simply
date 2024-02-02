package com.lilbaek.simply.test.domain;

import jakarta.persistence.Column;

public record PostId(
        @Column(name = "id")
        String id
) {
}
