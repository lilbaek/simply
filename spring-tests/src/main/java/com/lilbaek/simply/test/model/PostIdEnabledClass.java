package com.lilbaek.simply.test.model;

import com.lilbaek.simply.test.converters.YNToBooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

public record PostIdEnabledClass(
                @Column(name = "id")
                String id,
                @Column(name = "enabled")
                @Convert(converter = YNToBooleanConverter.class)
                boolean enabled
) {
}
