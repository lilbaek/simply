package com.lilbaek.simply.sql;

import jakarta.persistence.Entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InsertBuilder extends BaseBuilder {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();

    private InsertBuilder(){}

    public static SqlStatement insertSql(final Object instance) {
        final Class<?> cls = instance.getClass();
        try {
            final var metadata = mapperCache.computeIfAbsent(cls, key -> createMetadata(cls));
            final var values = MetadataHelper.getValuesFromInstanceForInsert(instance, metadata.properties);
            return new SqlStatement(metadata.statement, values);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    private static Metadata createMetadata(final Class<?> instance) {
        final var entityAnnotation = getInstanceEntityAnnotation(instance);
        final var properties = getInstanceProperties(instance);
        final var props = properties.entrySet();
        // TODO: Schema name?
        final String statement = "INSERT INTO " + entityAnnotation.name() + "(" +
                        String.join(",", props.stream().filter(MetadataHelper::filterTransient).map(MetadataHelper::getColumnName).toList()) +
                        ") VALUES (" +
                        String.join(",", props.stream().filter(MetadataHelper::filterTransient).map(stringPropertyEntry -> "?").toList()) +
                        ")";
        return new Metadata(properties.values().stream().toList(), entityAnnotation, statement);

    }



    record Metadata(List<Property> properties,
                    Entity entityAnnotation,
                    String statement) {

    }
}