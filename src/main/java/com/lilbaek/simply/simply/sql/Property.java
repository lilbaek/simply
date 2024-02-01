package com.lilbaek.simply.simply.sql;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

public record Property(
                String name,
                Class<?> dataType,
                Column columnAnnotation,
                Id idAnnotation,
                AttributeConverter converter,
                Boolean isTransient
) {
}
