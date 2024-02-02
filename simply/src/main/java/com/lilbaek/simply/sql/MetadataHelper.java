package com.lilbaek.simply.sql;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MetadataHelper {
    private MetadataHelper() {

    }

    public static LinkedHashMap<String, Property> getProperties(final Class resultClass) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        final var properties = new LinkedHashMap<String, Property>();
        for (final var field : resultClass.getDeclaredFields()) {
            properties.put(field.getName(), new Property(field.getName(),
                    field.getType(),
                    getColumnAnnotation(field),
                    getIdAnnotation(field),
                    getConverter(field),
                    getTransientAnnotation(field) != null));
        }
        return properties;
    }

    public static AttributeConverter getConverter(final AnnotatedElement ae)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final var converter = ae.getAnnotation(Convert.class);
        if (converter != null) {
            return (AttributeConverter) converter.converter().getDeclaredConstructor().newInstance();
        }
        return null;
    }

    public static boolean isTransient(final Property prop) {
        return Boolean.TRUE.equals(prop.isTransient());
    }

    public static boolean filterTransient(final Map.Entry<String, Property> x) {
        return !isTransient(x.getValue());
    }

    public static boolean filterTransientAndId(final Map.Entry<String, Property> x) {
        return !isTransient(x.getValue()) && x.getValue().idAnnotation() == null;
    }

    public static Column getColumnAnnotation(final AnnotatedElement ae) {
        return ae.getAnnotation(Column.class);
    }

    public static Id getIdAnnotation(final AnnotatedElement ae) {
        return ae.getAnnotation(Id.class);
    }

    public static Transient getTransientAnnotation(final AnnotatedElement ae) {
        return ae.getAnnotation(Transient.class);
    }

    public static Entity getEntityAnnotation(final AnnotatedElement ae) {
        return ae.getAnnotation(Entity.class);
    }

    // Find the second generic argument in a AttributeConverter<boolean, String> (Will return String in this case)
    public static Type getGenericTypeForConverter(final Type[] genericInterfaces) {
        for (final var genericInterface : genericInterfaces) {
            if (genericInterface instanceof final ParameterizedType parameterizedType) {
                return parameterizedType.getActualTypeArguments()[1];
            }
        }
        throw new UnsupportedOperationException("The type converter does not have a type argument: " + Arrays.toString(genericInterfaces));
    }

    public static String getColumnName(final Map.Entry<String, Property> x) {
        return getColumnName(x.getValue());
    }

    public static String getColumnName(final Property x) {
        return x.columnAnnotation().name().isEmpty() ? x.name() : x.columnAnnotation().name();
    }

    public static ArrayList<Object> getValuesFromInstanceForInsert(final Object instance, final List<Property> properties)
            throws NoSuchFieldException, IllegalAccessException {
        final var values = new ArrayList<>();
        for (final Property property : properties) {
            if (isTransient(property)) {
                continue;
            }
            values.add(getFieldValue(instance, property));
        }
        return values;
    }

    public static ArrayList<Object> getValuesFromInstanceForUpdate(final Object instance, final List<Property> properties)
            throws NoSuchFieldException, IllegalAccessException {
        final var values = new ArrayList<>();
        for (final Property property : properties) {
            if (isTransient(property) || property.idAnnotation() != null) {
                continue;
            }
            values.add(getFieldValue(instance, property));
        }
        return values;
    }

    public static Object getFieldValue(final Object instance, final Property property)
            throws NoSuchFieldException, IllegalAccessException {
        final var field = instance.getClass().getDeclaredField(property.name());
        field.setAccessible(true);
        if (property.converter() != null) {
            return property.converter().convertToDatabaseColumn(field.get(instance));
        }
        return field.get(instance);
    }
}
