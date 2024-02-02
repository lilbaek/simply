package com.lilbaek.simply.sql;

import com.lilbaek.simply.exceptions.NoIdException;
import com.lilbaek.simply.exceptions.NotAnEntityException;
import jakarta.persistence.Entity;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.TypeMismatchDataAccessException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import static com.lilbaek.simply.sql.MetadataHelper.getColumnName;

public abstract class BaseBuilder {

    protected static LinkedHashMap<String, Property> getInstanceProperties(final Class<?> cls) {
        final var properties = MetadataHelper.getProperties(cls);
        if (properties.entrySet().stream().noneMatch(x -> x.getValue().idAnnotation() != null)) {
            throw new NoIdException(cls.getName());
        }
        return properties;
    }

    protected static Entity getInstanceEntityAnnotation(final Class<?> cls) {
        final var entityAnnotation = MetadataHelper.getEntityAnnotation(cls);
        if (entityAnnotation == null) {
            throw new NotAnEntityException(cls.getName());
        }
        return entityAnnotation;
    }

    protected static StringBuilder getConditionFromColumns(final Object instance, final ArrayList<Object> values, final Collection<Property> properties, boolean onlyIdColumns)
                    throws NoSuchFieldException, IllegalAccessException {
        boolean first = true;
        final var condition = new StringBuilder();
        for (final Property property : properties) {
            if (property.idAnnotation() == null && onlyIdColumns) {
                continue;
            }
            if (first) {
                condition.append(MetadataHelper.getColumnName(property)).append(" = ?");
            } else {
                condition.append(" AND ").append(MetadataHelper.getColumnName(property)).append(" = ?");
            }
            first = false;
            values.add(MetadataHelper.getFieldValue(instance, property));
        }
        return condition;
    }

    protected static SqlStatement buildStatementWithWhereCondition(final Class cls, final String statement, final ArrayList<Object> values, final Object conditions)
                    throws NoSuchFieldException, IllegalAccessException {
        if (BeanUtils.isSimpleProperty(conditions.getClass())) {
            throw new TypeMismatchDataAccessException(
                            conditions.getClass().getName() + " cannot be used as condition for " + cls.getName());
        }
        final var properties = MetadataHelper.getProperties(conditions.getClass());
        final var sqlCondition = getConditionFromColumns(conditions, values, properties.values(), false);
        return new SqlStatement(statement + sqlCondition, values);
    }
}
