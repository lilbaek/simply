package com.lilbaek.simply.sql;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UpdateBuilder extends BaseBuilder {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();

    private UpdateBuilder() {
    }

    public static SqlStatement updateSql(final Object instance, final Optional<Object> conditions) {
        final Class<?> cls = instance.getClass();
        try {
            final var metadata = getMetadata(cls);
            final var values = MetadataHelper.getValuesFromInstanceForUpdate(instance, metadata.properties);
            if (conditions.isPresent()) {
                return buildStatementWithWhereCondition(cls, metadata.statement, values, conditions.get());
            }
            return buildStatementWithIdWhereCondition(instance, metadata, values);
        } catch (final NoSuchFieldException | IllegalAccessException | InvocationTargetException |
                       InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    private static Metadata getMetadata(final Class<?> cls) {
        return mapperCache.computeIfAbsent(cls, key -> {
            try {
                return createMetadata(cls);
            } catch (final InvocationTargetException | InstantiationException | IllegalAccessException |
                           NoSuchMethodException e) {
                throw new RuntimeException("Could not create metadata: " + cls.getName(), e);
            }
        });
    }

    private static SqlStatement buildStatementWithIdWhereCondition(final Object instance, final Metadata metadata,
                                                                   final ArrayList<Object> values)
            throws NoSuchFieldException, IllegalAccessException {
        final var condition = getConditionFromColumns(instance, values, metadata.properties, true);
        return new SqlStatement(metadata.statement + condition, values);
    }

    private static Metadata createMetadata(final Class<?> instance) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        final var tableAnnotation = getInstanceTableAnnotation(instance);
        final var properties = getInstanceProperties(instance);
        final var props = properties.entrySet();
        final String statement = "UPDATE {schema}" + tableAnnotation.name() + " SET " +
                String.join(",", props.stream().filter(MetadataHelper::filterTransientAndId).map(x -> MetadataHelper.getColumnName(x) + " = ?").toList()) +
                " WHERE ";
        return new Metadata(properties.values().stream().toList(), statement);
    }

    record Metadata(List<Property> properties,
                    String statement) {

    }
}
