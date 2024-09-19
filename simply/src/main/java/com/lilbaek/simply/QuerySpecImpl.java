package com.lilbaek.simply;

import dk.bankdata.kfa.simply.sql.ResultToRecordTransformer;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuerySpecImpl implements QuerySpec {
    private final JdbcClient.StatementSpec query;
    private final String sql;
    private final Logger logger;
    private final Map<String, Object> parms = new HashMap<>();

    public QuerySpecImpl(final JdbcClient.StatementSpec query, final String sql, final Logger logger) {
        this.query = query;
        this.sql = sql;
        this.logger = logger;
    }

    @Override
    public QuerySpec param(final String name, final Object value) {
        query.param(name, value);
        parms.putIfAbsent(name, value);
        return this;
    }

    public QuerySpec params(final List<?> values) {
        query.params(values);
        for (int i = 0; i < values.size(); i++) {
            parms.putIfAbsent(String.valueOf(i), values.get(i));
        }
        return this;
    }

    @Override
    public <T> T single(final Class<T> cls) {
        final List<T> list = getList(cls);
        if (list.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }
        return list.getFirst();
    }

    @Override
    public <T> T singleOrNull(final Class<T> cls) {
        final List<T> list = getList(cls);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }
        return list.getFirst();
    }

    @Override
    public <T> Optional<T> optional(final Class<T> cls) {
        return Optional.ofNullable(singleOrNull(cls));
    }

    @Override
    public <T> List<T> list(final Class<T> cls) {
        return getList(cls);
    }

    @Override
    public int update() {
        logger.logStatement(sql, parms);
        return query.update();
    }

    @Override
    public void updateSingle() {
        Assert.state(update() == 1, "DBClient - Failed to update %s | Params: %s".formatted(sql, String.join(",", parms.entrySet().stream().map(x -> x.getKey() + " = " + x.getValue()).toList())));
    }

    @Override
    public int delete() {
        logger.logStatement(sql, parms);
        return query.update();
    }

    @Override
    public void deleteSingle() {
        final var deleted = delete();
        Assert.state(deleted == 1, "DBClient - Delete count was not 1. Was %d for %s | Params: %s".formatted(deleted, sql, String.join(",", parms.entrySet().stream().map(x -> x.getKey() + " = " + x.getValue()).toList())));
    }

    @Override
    public JdbcClient.StatementSpec statementSpec() {
        return query;
    }

    private <T> List<T> getList(final Class<T> cls) {
        if (BeanUtils.isSimpleProperty(cls)) {
            return query.query(cls).list();
        }
        final var aliasToClassResultTransformer = new ResultToRecordTransformer<>(cls);
        logger.logStatement(sql, parms);
        final var queryResult = query.query();
        final var metaData = queryResult.rowSet().getMetaData();
        final int columnCount = metaData.getColumnCount();
        final var labels = new String[columnCount];
        for (int i = 0; i < columnCount; ++i) {
            // We use getColumnLabel as we want the alias names and not the column names
            labels[i] = metaData.getColumnLabel(i + 1);
        }
        final var rows = queryResult.listOfRows();
        final var result = new ArrayList<T>();
        for (final var row : rows) {
            result.add(aliasToClassResultTransformer.map(row, labels));
        }
        return result;
    }
}
