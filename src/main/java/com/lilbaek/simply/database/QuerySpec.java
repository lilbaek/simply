package com.lilbaek.simply.database;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface QuerySpec {
    QuerySpec param(String name, @Nullable Object value);

    /**
     * Transforms a query result to a Java record.
     * Supports @Column, @Convert and @Transient annotations
     *
     * @param cls   result type
     */
    <T> T record(final Class<T> cls);

    /**
     * Returns a simple type
     */
    <T> T value(final Class<T> cls);

    /**
     * Returns a simple type or null
     */
    <T> T valueOrNull(final Class<T> cls);

    /**
     * Transforms a query result to a Java record.
     * Supports @Column, @Convert and @Transient annotations
     *
     * @param cls   result type
     */
    <T> T recordOrNull(final Class<T> cls);

    /**
     * Transforms a query result to a Java record wrapped in Optional.
     * Supports @Column, @Convert and @Transient annotations
     *
     * @param cls   result type
     */
    <T> Optional<T> optional(final Class<T> cls);

    /**
     * Transforms a query result to a Java record.
     * Supports @Column, @Convert and @Transient annotations
     *
     * @param cls   result type
     */
    <T> List<T> list(final Class<T> cls);

    /**
     * Returns the underlying JdbcClient.StatementSpec
     */
    JdbcClient.StatementSpec statementSpec();
}
