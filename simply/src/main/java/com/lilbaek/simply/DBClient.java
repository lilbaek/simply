package com.lilbaek.simply;

import com.lilbaek.simply.sql.DeleteBuilder;
import com.lilbaek.simply.sql.InsertBuilder;
import com.lilbaek.simply.sql.SchemaReplacer;
import com.lilbaek.simply.sql.SqlStatement;
import com.lilbaek.simply.sql.UpdateBuilder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

import java.util.Optional;

public class DBClient {
    private final JdbcClient jdbcClient;
    private final SchemaReplacer schemaReplacer;

    public DBClient(final JdbcClient jdbcClient, final SchemaReplacer schemaReplacer) {
        this.jdbcClient = jdbcClient;
        this.schemaReplacer = schemaReplacer;
    }

    /**
     * Create an instance of <code>QuerySpec</code> for executing
     * a native SQL statement
     *
     * @param sqlString a native SQL query string
     * @return the new query instance
     */
    public QuerySpec sql(final String sqlString) {
        return new QuerySpecImpl(jdbcClient.sql(schemaReplacer.replaceSchema(sqlString)));
    }

    /***
     * Inserts a single record. Throws exception if insert count != 1
     */
    public <T> void insert(final T instance) {
        final var insertStatement = InsertBuilder.insertSql(instance);
        final var sql = getSqlFromStatement(insertStatement);
        sql.params(insertStatement.values());
        final var update = sql.update();
        Assert.state(update == 1, "DBClient - Failed to insert record %s".formatted(instance));
    }

    /***
     * Updates one or more records. Returns affected count.
     * The record is updated based on all columns annotated with @Id
     */
    public <T> int update(final T instance) {
        return doUpdate(instance, null);
    }

    /***
     * Updates one or more records. Returns affected count.
     * The record is updated based on the conditions object. Alle @Columns in the conditions record will be added as WHERE/AND conditions
     */
    public <T> int update(final T instance, final Object conditions) {
        return doUpdate(instance, conditions);
    }

    /***
     * Updates a single record. Throws exception if modified count != 1
     * The record is updated based on all columns annotated with @Id
     */
    public <T> void updateSingle(final T instance) {
        Assert.state(doUpdate(instance, null) == 1, "DBClient - Failed to update %s".formatted(instance.toString()));
    }

    /***
     * Updates a single record. Throws exception if modified count != 1
     * The record is updated based on the conditions object. Alle @Columns in the conditions record will be added as WHERE/AND conditions
     */
    public <T> void updateSingle(final T instance, final Object conditions) {
        Assert.state(doUpdate(instance, conditions) == 1, "DBClient - Failed to update %s with conditions: %s".formatted(instance.toString(), conditions));
    }

    private <T> int doUpdate(final T instance, final Object conditions) {
        final var updateStatement = UpdateBuilder.updateSql(instance, Optional.ofNullable(conditions));
        final var sql = getSqlFromStatement(updateStatement);
        sql.params(updateStatement.values());
        return sql.update();
    }

    /***
     * Deletes a single record. Throws exception if deleted count != 1
     * The record is deleted based on all columns annotated with @Id
     */
    public <T> void deleteSingle(final T instance) {
        final int deleted = delete(instance);
        Assert.state(deleted == 1, "DBClient - Delete count was not 1. Was %d for %s : %s".formatted(deleted, instance.getClass().getName(), instance));
    }

    /***
     * Deletes a single record. Throws exception if deleted count != 1
     * The record is deleted based on the conditions object. Alle @Columns in the conditions record will be added as WHERE/AND conditions
     */
    public void deleteSingle(final Class<?> cls, final Object conditions) {
        final int deleted = delete(cls, conditions);
        Assert.state(deleted == 1, "DBClient - Delete count was not 1. Was %d for %s with conditions: %s".formatted(deleted, cls.getName(), conditions));
    }

    /***
     * Deletes one or more records. Returns deleted count.
     * The record(s) are deleted based on all columns annotated with @Id
     */
    public <T> int delete(final T instance) {
        final var delete = DeleteBuilder.deleteSql(instance, Optional.empty());
        final var sql = getSqlFromStatement(delete);
        sql.params(delete.values());
        return sql.update();
    }

    /***
     * Deletes one or more records. Returns deleted count.
     * The record(s) are deleted based on the conditions object. Alle @Columns in the conditions record will be added as WHERE/AND conditions
     */
    public <T> int delete(final Class<T> cls, final Object conditions) {
        final var delete = DeleteBuilder.deleteSql(cls, conditions);
        final var sql = getSqlFromStatement(delete);
        sql.params(delete.values());
        return sql.update();
    }

    private JdbcClient.StatementSpec getSqlFromStatement(final SqlStatement statement) {
        return jdbcClient.sql(schemaReplacer.replaceSchema(statement.sql()));
    }
}
