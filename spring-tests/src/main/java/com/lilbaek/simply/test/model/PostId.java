package com.lilbaek.simply.test.model;

import jakarta.persistence.Column;

public record PostId(
        @Column(name = "id")
        String id
) {
}
