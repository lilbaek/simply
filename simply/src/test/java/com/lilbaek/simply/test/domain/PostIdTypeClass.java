package com.lilbaek.simply.test.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;

public record PostIdTypeClass(
                @Column(name = "id")
                String id,
                @Column(name = "type")
                @Convert(converter = PostTypeConverter.class)
                PostType type
) {
}
