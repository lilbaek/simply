package com.lilbaek.simply.database;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Transient;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.NumberUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps the tuples in a constructor call for usage with Java Records.
 * Supports @Column, @Convert and @Transient annotations
 */
public class ResultToRecordTransformer<T> {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();
    private final Class<T> resultClass;
    private final ConversionService conversionService = DefaultConversionService.getSharedInstance();

    public ResultToRecordTransformer(final Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    public T map(final Map<String, Object> data, final String[] columns) {
        try {
            final var metadata = mapperCache.computeIfAbsent(resultClass, key -> createMetadata(columns));
            check(columns, metadata);
            final var constructorArgs = new ArrayList<>();
            for (final var prop : metadata.properties) {
                if (isTransient(prop)) {
                    constructorArgs.add(null);
                } else {
                    constructorArgs.add(getConstructorValue(data, prop, metadata));
                }
            }
            return (T) metadata.constructor.newInstance(constructorArgs.toArray());
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not instantiate resultclass: " + resultClass.getName(), e);
        }
    }

    private static boolean isTransient(final Property prop) {
        return Boolean.TRUE.equals(prop.isTransient);
    }

    private Object getConstructorValue(final Map<String, Object> tuple, final Property prop, final Metadata metadata) {
        final var index = metadata.columnsAsList.indexOf(prop.name);
        if (index == -1) {
            throw new IllegalArgumentException(prop.name + " does not exist as a column in the result");
        }
        final var sqlValue = tuple.get(prop.name);
        if (sqlValue == null) {
            return handleNullValue(prop);
        } else if (prop.converter != null) {
            return getConvertedValued(prop, sqlValue);
        } else if (!sqlValue.getClass().equals(prop.dataType)) {
            return convertValueToRequiredType(sqlValue, prop.dataType);
        }
        return sqlValue;
    }

    private Object convertValueToRequiredType(final Object value, final Class<?> requiredType) {
        if (String.class == requiredType) {
            return value.toString();
        }
        if (Number.class.isAssignableFrom(requiredType)) {
            if (value instanceof final Number number) {
                // Convert original Number to target Number class.
                return NumberUtils.convertNumberToTargetClass(number, (Class<Number>) requiredType);
            }
            // Convert stringified value to target Number class.
            return NumberUtils.parseNumber(value.toString(), (Class<Number>) requiredType);
        }
        if (conversionService.canConvert(value.getClass(), requiredType)) {
            return conversionService.convert(value, requiredType);
        }
        throw new IllegalArgumentException(
                        "Value [" + value + "] is of type [" + value.getClass().getName() +
                                        "] and cannot be converted to required type [" + requiredType.getName() + "]");
    }

    private Object handleNullValue(final Property prop) {
        if (prop.converter != null) {
            return prop.converter.convertToEntityAttribute(null);
        }
        return null;
    }

    // Will return the converted value by using the AttributeConverter. Ask conversionService to convert to the expected param
    // in the AttributeConverter first. Example when we have a AttributeConverter<boolean, String> but the DB type is actually a Char (Instead of String)
    private Object getConvertedValued(final Property prop, final Object value) {
        final var converterClass = prop.converter.getClass();
        final var genericInterfaces = converterClass.getGenericInterfaces();
        final var genericTypes = getGenericTypeForConverter(genericInterfaces);
        if (!value.getClass().getTypeName().equals(genericTypes.getTypeName())) {
            if (conversionService.canConvert(value.getClass(), (Class<?>) genericTypes)) {
                return prop.converter.convertToEntityAttribute(conversionService.convert(value, (Class<?>) genericTypes));
            }
            throw new IllegalArgumentException(
                            "Value [" + value + "] is of type [" + value.getClass().getName() +
                                            "] and cannot be converted to required type [" + ((Class<?>) genericTypes).getName() + "]");
        }
        return prop.converter.convertToEntityAttribute(value);
    }

    // Find the second generic argument in a AttributeConverter<boolean, String> (Will return String in this case)
    private static Type getGenericTypeForConverter(final Type[] genericInterfaces) {
        for (final var genericInterface : genericInterfaces) {
            if (genericInterface instanceof final ParameterizedType parameterizedType) {
                return parameterizedType.getActualTypeArguments()[1];
            }
        }
        throw new UnsupportedOperationException("The type converter does not have a type argument: " + Arrays.toString(genericInterfaces));
    }

    // Find and map fields to constructor params.
    private Metadata createMetadata(final String[] columns) {
        try {
            final List<Property> metadata = new ArrayList<>();
            final var aliasAsList = Arrays.asList(columns);
            final var properties = new LinkedHashMap<String, Property>();
            for (final var field : resultClass.getDeclaredFields()) {
                properties.put(field.getName(), new Property(field.getName(),
                                field.getType(),
                                getColumnAnnotation(field),
                                getConverter(field),
                                getTransientAnnotation(field) != null));
            }
            final var constructors = resultClass.getConstructors();
            if (constructors.length != 1) {
                throw new IllegalArgumentException("Only a single constructor is supported for : %s".formatted(resultClass.getTypeName()));
            }
            final var constructor = Arrays.stream(resultClass.getConstructors()).findFirst().orElseThrow();
            final var parameters = constructor.getParameters();
            for (final var parameter : parameters) {
                // find the field and get the corresponding annotations
                final var match = properties.get(parameter.getName());
                metadata.add(new Property(getConstructorParamName(match), parameter.getType(), match.columnAnnotation, match.converter,
                                match.isTransient));
            }
            final var missingParams = metadata.stream().filter(x -> !aliasAsList.contains(x.name) && !x.isTransient).toList();
            if (!missingParams.isEmpty()) {
                throw new IllegalArgumentException(
                                "The constructor " + resultClass.getTypeName() + " takes more arguments than returned as columns by the query. Arguments not in query: " +
                                                String.join(",", missingParams.stream()
                                                                .map(x -> x.name).toList()));
            }
            if (parameters.length == 0) {
                throw new IllegalArgumentException(
                                "The constructor " + resultClass.getTypeName() + " takes no arguments. You need to have a minimum of 1 argument in your constructor");
            }
            return new Metadata(columns, metadata, aliasAsList, constructor);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create metadata for: " + resultClass.getName(), e);
        }
    }

    private String getConstructorParamName(final Property match) {
        if (match.columnAnnotation != null) {
            final var name = match.columnAnnotation.name().trim();
            if (!name.isEmpty()) {
                return name.toUpperCase();
            }
        }
        return match.name.toUpperCase();
    }

    private Column getColumnAnnotation(final AnnotatedElement ae) {
        return ae.getAnnotation(Column.class);
    }

    private Transient getTransientAnnotation(final AnnotatedElement ae) {
        return ae.getAnnotation(Transient.class);
    }

    private AttributeConverter getConverter(final AnnotatedElement ae)
                    throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final var converter = ae.getAnnotation(Convert.class);
        if (converter != null) {
            return (AttributeConverter) converter.converter().getDeclaredConstructor().newInstance();
        }
        return null;
    }

    private void check(final String[] aliases, final Metadata metadata) {
        if (!Arrays.equals(aliases, metadata.columns)) {
            throw new IllegalStateException(
                            "aliases are different from what is cached; aliases=" + Arrays.asList(aliases) +
                                            " cached=" + Arrays.asList(metadata.columns));
        }
    }

    record Property(
                    String name,
                    Class<?> dataType,
                    Column columnAnnotation,
                    AttributeConverter converter,
                    Boolean isTransient
    ) {
    }

    record Metadata(String[] columns,
                    List<Property> properties,
                    List<String> columnsAsList,
                    Constructor<?> constructor) {

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (ResultToRecordTransformer<T>) o;
        return resultClass.equals(that.resultClass);
    }

    @Override
    public int hashCode() {
        return resultClass.hashCode();
    }
}

