package com.lilbaek.simply.simply.sql;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.NumberUtils;

public class ConversionHelper {
    private static final ConversionService conversionService = DefaultConversionService.getSharedInstance();
    private ConversionHelper() {
    }

    public static Object convertValueToRequiredType(final Object value, final Class<?> requiredType) {
        if (String.class == requiredType) {
            return value.toString();
        }
        if (Number.class.isAssignableFrom(requiredType)) {
            if (value instanceof final Number number) {
                return NumberUtils.convertNumberToTargetClass(number, (Class<Number>) requiredType);
            }
            return NumberUtils.parseNumber(value.toString(), (Class<Number>) requiredType);
        }
        if (conversionService.canConvert(value.getClass(), requiredType)) {
            return conversionService.convert(value, requiredType);
        }
        throw new IllegalArgumentException(
                        "Value [" + value + "] is of type [" + value.getClass().getName() +
                                        "] and cannot be converted to required type [" + requiredType.getName() + "]");
    }

    public static Object handleNullValue(final Property prop) {
        if (prop.converter() != null) {
            return prop.converter().convertToEntityAttribute(null);
        }
        return null;
    }

    // Will return the converted value by using the AttributeConverter. Ask conversionService to convert to the expected param
    // in the AttributeConverter first. Example when we have a AttributeConverter<boolean, String> but the DB type is actually a Char (Instead of String)
    public static Object getConvertedValued(final Property prop, final Object value) {
        final var converterClass = prop.converter().getClass();
        final var genericInterfaces = converterClass.getGenericInterfaces();
        final var genericTypes = MetadataHelper.getGenericTypeForConverter(genericInterfaces);
        if (!value.getClass().getTypeName().equals(genericTypes.getTypeName())) {
            if (conversionService.canConvert(value.getClass(), (Class<?>) genericTypes)) {
                return prop.converter().convertToEntityAttribute(conversionService.convert(value, (Class<?>) genericTypes));
            }
            throw new IllegalArgumentException(
                            "Value [" + value + "] is of type [" + value.getClass().getName() +
                                            "] and cannot be converted to required type [" + ((Class<?>) genericTypes).getName() + "]");
        }
        return prop.converter().convertToEntityAttribute(value);
    }
}
