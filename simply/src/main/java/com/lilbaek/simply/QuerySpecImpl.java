package com.lilbaek.simply;

import com.lilbaek.simply.sql.ResultToRecordTransformer;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuerySpecImpl implements QuerySpec {
    private final JdbcClient.StatementSpec query;

    public QuerySpecImpl(final JdbcClient.StatementSpec spec) {
        this.query = spec;
    }

    @Override
    public QuerySpec param(final String name, final Object value) {
        query.param(name, value);
        return this;
    }

    @Override
    public <T> T record(final Class<T> cls) {
        final List<T> list = getList(query, cls);
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
            return query.query(rowMapper).single();
        }
        throw new TypeMismatchDataAccessException(cls.getName() + " is not a simple type");
    }

    @Override
    public <T> T valueOrNull(final Class<T> cls) {
        if (BeanUtils.isSimpleProperty(cls)) {
            final RowMapper<T> rowMapper = new SingleColumnRowMapper<>(cls);
            return query.query(rowMapper).optional().orElse(null);
        }
        throw new TypeMismatchDataAccessException(cls.getName() + " is not a simple type");
    }

    @Override
    public <T> T recordOrNull(final Class<T> cls) {
        final List<T> list = getList(query, cls);
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
        return getList(query, cls);
    }

    @Override
    public JdbcClient.StatementSpec statementSpec() {
        return query;
    }

    private <T> List<T> getList(final JdbcClient.StatementSpec query, final Class<T> cls) {
        final var aliasToClassResultTransformer = new ResultToRecordTransformer<>(cls);
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
