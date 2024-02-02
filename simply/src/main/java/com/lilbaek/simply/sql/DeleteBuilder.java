package com.lilbaek.simply.sql;

import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DeleteBuilder extends BaseBuilder {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();

    private DeleteBuilder() {
    }

    public static SqlStatement deleteSql(final Object instance, final Optional<Object> conditions) {
        final Class<?> cls = instance.getClass();
        try {
            final var metadata = mapperCache.computeIfAbsent(cls, key -> createMetadata(cls));
            if (conditions.isPresent()) {
                return buildStatementWithWhereCondition(cls, metadata.statement, new ArrayList<>(), conditions.get());
            }
            return buildStatementWithIdWhereCondition(instance, metadata, new ArrayList<>());
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    public static SqlStatement deleteSql(final Class<?> cls, final Object conditions) {
        try {
            final var metadata = mapperCache.computeIfAbsent(cls, key -> createMetadata(cls));
            return buildStatementWithWhereCondition(cls, metadata.statement, new ArrayList<>(), conditions);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    private static SqlStatement buildStatementWithIdWhereCondition(final Object instance, final Metadata metadata, final ArrayList<Object> values)
                    throws NoSuchFieldException, IllegalAccessException {
        final var condition = getConditionFromColumns(instance, values, metadata.properties, true);
        return new SqlStatement(metadata.statement + condition, values);
    }

    private static Metadata createMetadata(final Class<?> cls) {
        final var entityAnnotation = getInstanceEntityAnnotation(cls);
        final var properties = getInstanceProperties(cls);
        // TODO: Schema name?
        final String statement = "DELETE FROM " + entityAnnotation.name() + " WHERE ";
        return new Metadata(properties.values().stream().toList(), entityAnnotation, statement);
    }

    record Metadata(List<Property> properties,
                    Entity entityAnnotation,
                    String statement) {

    }
}
