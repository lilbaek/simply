package com.lilbaek.simply.test.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class YNToBooleanConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(final Boolean aBoolean) {
        if (aBoolean == null) {
            return "N";
        }
        return aBoolean ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(final String s) {
        return "Y".equals(s);
    }
}
