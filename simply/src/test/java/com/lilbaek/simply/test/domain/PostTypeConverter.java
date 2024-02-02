package com.lilbaek.simply.test.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter
public class PostTypeConverter implements AttributeConverter<PostType, String> {
    @Override
    public String convertToDatabaseColumn(final PostType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public PostType convertToEntityAttribute(final String dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(PostType.values()).filter(c -> c.getCode().equals(dbData)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
