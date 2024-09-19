package com.lilbaek.simply.sql;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps the sql result in a constructor call for usage with Java Records.
 * Supports @Column, @Convert and @Transient annotations
 */
public class ResultToRecordTransformer<T> {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();
    private final Class<T> resultClass;

    public ResultToRecordTransformer(final Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    public T map(final SqlRowSet data, final String[] columns) {
        try {
            final var metadata = mapperCache.computeIfAbsent(resultClass, key -> {
                try {
                    return createMetadata(columns);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    throw new RuntimeException("Could not create metadata: " + resultClass.getName(), e);
                }
            });
            check(columns, metadata);
            final var constructorArgs = new ArrayList<>();
            for (final var prop : metadata.properties) {
                if (MetadataHelper.isTransient(prop)) {
                    constructorArgs.add(null);
                } else {
                    constructorArgs.add(getConstructorValue(data, prop, metadata));
                }
            }
            return (T) metadata.constructor.newInstance(constructorArgs.toArray());
        } catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Could not instantiate resultclass: " + resultClass.getName(), e);
        }
    }

    private Object getConstructorValue(final SqlRowSet tuple, final Property prop, final Metadata metadata) {
        final var index = metadata.columnsAsList.indexOf(prop.name());
        if (index == -1) {
            throw new IllegalArgumentException(prop.name() + " does not exist as a column in the result");
        }
        final var sqlValue = tuple.getObject(prop.name());
        if (sqlValue == null) {
            return ConversionHelper.handleNullValue(prop);
        } else if (prop.converter() != null) {
            return ConversionHelper.getConvertedValued(prop, sqlValue);
        } else if (!sqlValue.getClass().equals(prop.dataType())) {
            return ConversionHelper.convertValueToRequiredType(sqlValue, prop.dataType());
        }
        return sqlValue;
    }

    // Find and map fields to constructor params.
    private Metadata createMetadata(final String[] columns) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        final List<Property> propertyMetadata = new ArrayList<>();
        final var aliasAsList = Arrays.asList(columns);
        final var properties = MetadataHelper.getProperties(resultClass);
        final var constructors = resultClass.getConstructors();
        if (constructors.length != 1) {
            throw new InvalidDataAccessApiUsageException("Only a single constructor is supported for : %s".formatted(resultClass.getTypeName()));
        }
        final var constructor = Arrays.stream(resultClass.getConstructors()).findFirst().orElseThrow();
        final var parameters = constructor.getParameters();
        for (final var parameter : parameters) {
            // find the field and get the corresponding annotations
            final var match = properties.get(parameter.getName());
            propertyMetadata.add(new Property(getConstructorParamName(match), parameter.getType(), match.columnAnnotation(), match.idAnnotation(), match.converter(),
                    match.isTransient()));
        }
        final var missingParams = propertyMetadata.stream().filter(x -> !aliasAsList.contains(x.name()) && !x.isTransient()).toList();
        if (!missingParams.isEmpty()) {
            throw new InvalidDataAccessApiUsageException(
                    "The constructor " + resultClass.getTypeName() + " takes more arguments than returned as columns by the query. Arguments not in query: " +
                            String.join(",", missingParams.stream()
                                    .map(Property::name).toList()));
        }
        if (parameters.length == 0) {
            throw new InvalidDataAccessApiUsageException(
                    "The constructor " + resultClass.getTypeName() + " takes no arguments. You need to have a minimum of 1 argument in your constructor");
        }
        return new Metadata(List.of(columns), propertyMetadata, aliasAsList, constructor);
    }

    private String getConstructorParamName(final Property match) {
        if (match.columnAnnotation() != null) {
            final var name = match.columnAnnotation().name().trim();
            if (!name.isEmpty()) {
                return name.toUpperCase();
            }
        }
        return match.name().toUpperCase();
    }

    private void check(final String[] aliases, final Metadata metadata) {
        if (aliases.length != metadata.columns.size()) {
            throw new IllegalStateException(
                    "aliases count different from what is cached; aliases=" + Arrays.asList(aliases) +
                            " cached=" + List.of(metadata.columns));
        }
        for (final String alias : aliases) {
            if (!metadata.columns.contains(alias)) {
                throw new IllegalStateException(
                        "aliases are different from what is cached; aliases=" + Arrays.asList(aliases) +
                                " cached=" + List.of(metadata.columns));
            }
        }
    }

    record Metadata(List<String> columns,
                    List<Property> properties,
                    List<String> columnsAsList,
                    Constructor<?> constructor) {

    }
}

