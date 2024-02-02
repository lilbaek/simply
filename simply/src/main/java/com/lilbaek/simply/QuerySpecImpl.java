package com.lilbaek.simply;

import com.lilbaek.simply.sql.ResultToRecordTransformer;
import com.lilbaek.simply.sql.StatementLogger;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuerySpecImpl implements QuerySpec {
    private final JdbcClient.StatementSpec query;
    private final String sql;
    private final StatementLogger logger;
    private final Map<String, Object> parms = new HashMap<>();

    public QuerySpecImpl(final JdbcClient.StatementSpec query, final String sql, final StatementLogger logger) {
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
    public <T> T record(final Class<T> cls) {
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
    public <T> T value(final Class<T> cls) {
        if (BeanUtils.isSimpleProperty(cls)) {
            final RowMapper<T> rowMapper = new SingleColumnRowMapper<>(cls);
            logger.logStatement(sql, parms);
            return query.query(rowMapper).single();
        }
        throw new TypeMismatchDataAccessException(cls.getName() + " is not a simple type");
    }

    @Override
    public <T> T valueOrNull(final Class<T> cls) {
        if (BeanUtils.isSimpleProperty(cls)) {
            final RowMapper<T> rowMapper = new SingleColumnRowMapper<>(cls);
            logger.logStatement(sql, parms);
            return query.query(rowMapper).optional().orElse(null);
        }
        throw new TypeMismatchDataAccessException(cls.getName() + " is not a simple type");
    }

    @Override
    public <T> T recordOrNull(final Class<T> cls) {
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
        return Optional.ofNullable(recordOrNull(cls));
    }

    @Override
    public <T> List<T> list(final Class<T> cls) {
        return getList(cls);
    }

    @Override
    public JdbcClient.StatementSpec statementSpec() {
        return query;
    }

    private <T> List<T> getList(final Class<T> cls) {
        final var aliasToClassResultTransformer = new ResultToRecordTransformer<>(cls);
        logger.logStatement(sql, parms);
        final var queryResult = query.query();
        final var columnNames = queryResult.rowSet().getMetaData().getColumnNames();
        final var rows = queryResult.listOfRows();
        final var result = new ArrayList<T>();
        for (final var row : rows) {
            result.add(aliasToClassResultTransformer.map(row, columnNames));
        }
        return result;
    }
}
