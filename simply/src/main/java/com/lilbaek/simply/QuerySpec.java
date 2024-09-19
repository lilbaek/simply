package com.lilbaek.simply;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface QuerySpec {
    /**
     * Bind a named statement parameter for ":x" placeholder resolution, with each "x" name matching a ":x" placeholder in the SQL statement.
     * Params:
     * name – the parameter name value – the parameter value to bind
     */
    QuerySpec param(String name, @Nullable Object value);

    /**
     * Transforms a query result to a Java record or a primitive type.
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> T single(final Class<T> cls);

    /**
     * Transforms a query result to a Java record, a primitive type or null
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> T singleOrNull(final Class<T> cls);

    /**
     * Transforms a query result to a Java record or a primitive type wrapped in Optional.
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> Optional<T> optional(final Class<T> cls);

    /**
     * Transforms a query result to list of <T>
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> List<T> list(final Class<T> cls);

    /***
     * Updates one or more records. Returns affected count.
     */
    int update();

    /***
     * Updates a single record. Throws exception if modified count != 1
     */
    void updateSingle();

    /***
     * Deletes one or more records. Returns deleted count.
     */
    int delete();

    /***
     * Deletes a single record. Throws exception if deleted count != 1
     */
    void deleteSingle();

    /**
     * Returns the underlying JdbcClient.StatementSpec
     */
    JdbcClient.StatementSpec statementSpec();
}
