package com.hcl.petadoption.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic in-memory implementation of {@link Repository} backed by a {@link Map}.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Generics</b>       – {@code T} and {@code ID} type parameters without raw types</li>
 *   <li><b>Map collection</b> – {@link LinkedHashMap} for O(1) lookup and insertion-order iteration</li>
 *   <li><b>Encapsulation</b>  – the internal map is never exposed directly</li>
 * </ul>
 *
 * <p>The class is intentionally abstract so concrete repositories can supply the
 * key-extraction logic via {@link #extractId(Object)}.
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 */
public abstract class InMemoryRepository<T, ID> implements Repository<T, ID> {

    /**
     * Internal Map<ID, Entity> store.  Uses LinkedHashMap to preserve insertion order
     * when listing – consistent with future ORDER BY primary-key queries in Assessment 2.
     */
    private final Map<ID, T> store = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    // Template method – subclasses supply ID extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the primary key from an entity.
     * Subclasses must implement this to keep {@code InMemoryRepository} generic.
     */
    protected abstract ID extractId(T entity);

    // -------------------------------------------------------------------------
    // Repository implementation
    // -------------------------------------------------------------------------

    @Override
    public T save(T entity) {
        store.put(extractId(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public boolean delete(ID id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public boolean existsById(ID id) {
        return store.containsKey(id);
    }

    /**
     * Returns the raw store map as an unmodifiable view.
     * Used by subclasses that need map-level operations (e.g. iteration for streaming).
     */
    protected Map<ID, T> getStore() {
        return Collections.unmodifiableMap(store);
    }
}
