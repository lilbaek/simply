package com.lilbaek.simply.sql;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.lilbaek.simply.sql.MetadataHelper.getColumnName;

public class SelectBuilder extends BaseBuilder {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();

    private SelectBuilder() {
    }

    public static <T> SqlStatement selectStatement(final Class<T> cls, final Object conditions) {
        try {
            final var metadata = getMetadata(cls);
            if (!BeanUtils.isSimpleProperty(conditions.getClass())) {
                return buildStatementWithWhereCondition(cls, metadata.statement, new ArrayList<>(), conditions);
            }
            final var values = new ArrayList<>();
            values.add(conditions);
            return buildStatementWithIdWhereCondition(metadata, values);
        } catch (final NoSuchFieldException | IllegalAccessException | InvocationTargetException |
                       InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    private static SqlStatement buildStatementWithIdWhereCondition(final Metadata metadata,
                                                                   final ArrayList<Object> values) {
        final var first = metadata.properties.stream().filter(x -> x.idAnnotation() != null).findFirst().orElseThrow();
        return new SqlStatement(metadata.statement + getColumnName(first) + " = ?", values);
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

    private static Metadata createMetadata(final Class<?> instance) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        final var entityAnnotation = getInstanceTableAnnotation(instance);
        final var properties = getInstanceProperties(instance);
        final var props = properties.entrySet();
        final String statement = "SELECT " + String.join(",", props.stream().filter(MetadataHelper::filterTransient).map(MetadataHelper::getColumnName).toList()) +
                " FROM {schema}" + entityAnnotation.name() +
                " WHERE ";
        return new Metadata(properties.values().stream().toList(), statement);
    }

    record Metadata(List<Property> properties,
                    String statement) {

    }
}
