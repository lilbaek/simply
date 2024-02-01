package com.lilbaek.simply.simply.sql;

import jakarta.persistence.Entity;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.lilbaek.simply.simply.sql.MetadataHelper.getColumnName;
import static com.lilbaek.simply.simply.sql.MetadataHelper.getFieldValue;
import static com.lilbaek.simply.simply.sql.MetadataHelper.getProperties;
import static com.lilbaek.simply.simply.sql.MetadataHelper.getValuesFromInstanceForUpdate;

public class UpdateBuilder extends BaseBuilder {
    private static final Map<Class<?>, Metadata> mapperCache = new ConcurrentHashMap<>();

    private UpdateBuilder() {
    }

    public static SqlStatement updateSql(final Object instance, final Optional<Object> conditions) {
        final Class<?> cls = instance.getClass();
        try {
            final var metadata = mapperCache.computeIfAbsent(cls, key -> createMetadata(cls));
            final var values = getValuesFromInstanceForUpdate(instance, metadata.properties);
            if (conditions.isPresent()) {
                return buildStatementWithWhereCondition(cls, metadata.statement, values, conditions.get());
            }
            return buildStatementWithIdWhereCondition(instance, metadata, values);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not create update statement: " + cls.getName(), e);
        }
    }

    private static SqlStatement buildStatementWithIdWhereCondition(final Object instance, final UpdateBuilder.Metadata metadata,
                    final ArrayList<Object> values)
                    throws NoSuchFieldException, IllegalAccessException {
        final var condition = getConditionFromColumns(instance, values, metadata.properties, true);
        return new SqlStatement(metadata.statement + condition, values);
    }

    private static Metadata createMetadata(final Class<?> instance) {
        final var entityAnnotation = getInstanceEntityAnnotation(instance);
        final var properties = getInstanceProperties(instance);
        final var props = properties.entrySet();
        // TODO: Schema name?
        final String statement = "UPDATE " + entityAnnotation.name() + " SET " +
                        String.join(",", props.stream().filter(MetadataHelper::filterTransientAndId).map(x -> getColumnName(x) + " = ?").toList()) +
                        " WHERE ";
        return new Metadata(properties.values().stream().toList(), entityAnnotation, statement);
    }

    record Metadata(List<Property> properties,
                    Entity entityAnnotation,
                    String statement) {

    }
}
