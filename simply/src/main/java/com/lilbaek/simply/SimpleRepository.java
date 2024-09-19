package com.lilbaek.simply;

import org.springframework.core.GenericTypeResolver;

import java.util.Optional;

public abstract class SimpleRepository<T, ID> {
    protected final DBClient dbClient;

    protected SimpleRepository(final DBClient dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<T> findById(final ID id) {
        final var cls = gettClass();
        return dbClient.findByIdOptional(id, cls);
    }

    private Class<T> gettClass() {
        return (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(), SimpleRepository.class)[0];
    }
}
