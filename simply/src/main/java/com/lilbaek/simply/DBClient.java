package com.lilbaek.simply;

import com.lilbaek.simply.sql.DeleteBuilder;
import com.lilbaek.simply.sql.InsertBuilder;
import com.lilbaek.simply.sql.SchemaReplacer;
import com.lilbaek.simply.sql.SelectBuilder;
import com.lilbaek.simply.sql.SqlStatement;
import com.lilbaek.simply.sql.StatementLogger;
import com.lilbaek.simply.sql.UpdateBuilder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

import java.util.Optional;

public class DBClient {
    private final JdbcClient jdbcClient;
    private final SchemaReplacer schemaReplacer;
    private final StatementLogger statementLogger;

    public DBClient(final JdbcClient jdbcClient, final SchemaReplacer schemaReplacer, final StatementLogger statementLogger) {
        this.jdbcClient = jdbcClient;
        this.schemaReplacer = schemaReplacer;
        this.statementLogger = statementLogger;
    }

    public <T> T findById(final Object id, final Class<T> cls) {
        final QuerySpecImpl querySpec = prepareFindBy(id, cls);
        return querySpec.record(cls);
    }

    public <T> T findByIdOrNull(final Object id, final Class<T> cls) {
        final QuerySpecImpl querySpec = prepareFindBy(id, cls);
        return querySpec.recordOrNull(cls);
    }

    public <T> Optional<T> findByIdOptional(final Object id, final Class<T> cls) {
        final QuerySpecImpl querySpec = prepareFindBy(id, cls);
        return querySpec.optional(cls);
    }

    private <T> QuerySpecImpl prepareFindBy(final Object condition, final Class<T> cls) {
        final var sqlStatement = SelectBuilder.selectStatement(cls, condition);
        final String sql = schemaReplacer.replaceSchema(sqlStatement.sql());
        final QuerySpecImpl querySpec = new QuerySpecImpl(jdbcClient.sql(sql), sql, statementLogger);
        querySpec.params(sqlStatement.values());
        return querySpec;
    }

    /**
     * Create an instance of <code>QuerySpec</code> for executing
     * a native SQL statement
     *
     * @param sqlString a native SQL query string
     * @return the new query instance
     */
    public QuerySpec sql(final String sqlString) {
        final String sql = schemaReplacer.replaceSchema(sqlString);
        return new QuerySpecImpl(jdbcClient.sql(sql), sql, statementLogger);
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
        final String sql = schemaReplacer.replaceSchema(statement.sql());
        statementLogger.logStatement(sql, statement.values());
        return jdbcClient.sql(sql);
    }
}
