package com.lilbaek.simply.simply.sql;

import com.lilbaek.simply.simply.exceptions.NoIdException;
import com.lilbaek.simply.simply.exceptions.NotAnEntityException;
import jakarta.persistence.Entity;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.TypeMismatchDataAccessException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static com.lilbaek.simply.simply.sql.MetadataHelper.getColumnName;
import static com.lilbaek.simply.simply.sql.MetadataHelper.getEntityAnnotation;
import static com.lilbaek.simply.simply.sql.MetadataHelper.getFieldValue;
import static com.lilbaek.simply.simply.sql.MetadataHelper.getProperties;

public abstract class BaseBuilder {

    protected static LinkedHashMap<String, Property> getInstanceProperties(final Class<?> cls) {
        final var properties = getProperties(cls);
        if (properties.entrySet().stream().noneMatch(x -> x.getValue().idAnnotation() != null)) {
            throw new NoIdException(cls.getName());
        }
        return properties;
    }

    protected static Entity getInstanceEntityAnnotation(final Class<?> cls) {
        final var entityAnnotation = getEntityAnnotation(cls);
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
                condition.append(getColumnName(property)).append(" = ?");
            } else {
                condition.append(" AND ").append(getColumnName(property)).append(" = ?");
            }
            first = false;
            values.add(getFieldValue(instance, property));
        }
        return condition;
    }

    protected static SqlStatement buildStatementWithWhereCondition(final Class cls, final String statement, final ArrayList<Object> values, final Object conditions)
                    throws NoSuchFieldException, IllegalAccessException {
        if (BeanUtils.isSimpleProperty(conditions.getClass())) {
            throw new TypeMismatchDataAccessException(
                            conditions.getClass().getName() + " cannot be used as condition for " + cls.getName());
        }
        final var properties = getProperties(conditions.getClass());
        final var sqlCondition = getConditionFromColumns(conditions, values, properties.values(), false);
        return new SqlStatement(statement + sqlCondition, values);
    }
}
