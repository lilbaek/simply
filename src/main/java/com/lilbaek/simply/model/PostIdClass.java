package com.lilbaek.simply.model;

import com.lilbaek.simply.converters.YNToBooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

public record PostIdClass(
                @Column(name = "id")
                String id,
                @Column(name = "enabled")
                @Convert(converter = YNToBooleanConverter.class)
                boolean enabled
) {
}
