package com.lilbaek.simply.sql;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.lilbaek.simply.sql.MetadataHelper.getColumnName;

public class DeleteBuilder extends BaseBuilder {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();

    private DeleteBuilder() {
    }

    public static SqlStatement deleteSql(final Object instance, final Optional<Object> conditions) {
        final Class<?> cls = instance.getClass();
        try {
            final var metadata = mapperCache.computeIfAbsent(cls, key -> {
                try {
                    return createMetadata(cls);
                } catch (final InvocationTargetException | InstantiationException | IllegalAccessException |
                               NoSuchMethodException e) {
                    throw new RuntimeException("Could not create metadata: " + cls.getName(), e);
                }
            });
            if (conditions.isPresent()) {
                return buildStatementWithWhereCondition(cls, metadata.statement, new ArrayList<>(), conditions.get());
            }
            return buildStatementWithIdWhereCondition(instance, metadata, new ArrayList<>());
        } catch (final NoSuchFieldException | IllegalAccessException | InvocationTargetException |
                       InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    public static SqlStatement deleteSql(final Class<?> cls, final Object conditions) {
        try {
            final var metadata = mapperCache.computeIfAbsent(cls, key -> {
                try {
                    return createMetadata(cls);
                } catch (final InvocationTargetException | InstantiationException | IllegalAccessException |
                               NoSuchMethodException e) {
                    throw new RuntimeException("Could not create metadata: " + cls.getName(), e);
                }
            });
            if (!BeanUtils.isSimpleProperty(conditions.getClass())) {
                return buildStatementWithWhereCondition(cls, metadata.statement, new ArrayList<>(), conditions);
            }
            final var values = new ArrayList<>();
            values.add(conditions);
            return buildStatementWithIdWhereConditionSimpleType(metadata, values);
        } catch (final NoSuchFieldException | IllegalAccessException | InvocationTargetException |
                       InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create delete statement: " + cls.getName(), e);
        }
    }

    private static SqlStatement buildStatementWithIdWhereConditionSimpleType(final Metadata metadata,
                                                                             final ArrayList<Object> values) {
        final var first = metadata.properties.stream().filter(x -> x.idAnnotation() != null).findFirst().orElseThrow();
        return new SqlStatement(metadata.statement + getColumnName(first) + " = ?", values);
    }

    private static SqlStatement buildStatementWithIdWhereCondition(final Object instance, final Metadata metadata, final ArrayList<Object> values)
            throws NoSuchFieldException, IllegalAccessException {
        final var condition = getConditionFromColumns(instance, values, metadata.properties, true);
        return new SqlStatement(metadata.statement + condition, values);
    }

    private static Metadata createMetadata(final Class<?> cls) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        final var entityAnnotation = getInstanceTableAnnotation(cls);
        final var properties = getInstanceProperties(cls);
        final String statement = "DELETE FROM {schema}" + entityAnnotation.name() + " WHERE ";
        return new Metadata(properties.values().stream().toList(), statement);
    }

    record Metadata(List<Property> properties,
                    String statement) {

    }
}
