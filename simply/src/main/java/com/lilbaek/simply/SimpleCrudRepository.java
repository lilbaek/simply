package com.lilbaek.simply;

import org.springframework.core.GenericTypeResolver;

import java.util.Optional;

public abstract class SimpleCrudRepository<T, ID> {
    protected final DBClient dbClient;

    protected SimpleCrudRepository(final DBClient dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<T> findById(final ID id) {
        final var cls = gettClass();
        return dbClient.findByIdOptional(id, cls);
    }

    public void deleteById(final ID id) {
        final var cls = gettClass();
        dbClient.deleteSingle(cls, id);
    }

    public void insert(final T instance) {
        dbClient.insert(instance);
    }

    public void update(final T instance) {
        dbClient.updateSingle(instance);
    }

    private Class<T> gettClass() {
        return (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(), SimpleCrudRepository.class)[0];
    }
}
