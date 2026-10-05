package com.hcl.petadoption.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository abstraction for CRUD operations on any entity type.
 *
 * <p>Using a type parameter {@code T} (entity) and {@code ID} (key type) keeps the
 * contract free of concrete types.  In Assessment 3 this interface will be replaced
 * by Spring Data JPA's {@code JpaRepository<T, ID>} – the method signatures are
 * intentionally aligned with that future contract.
 *
 * <p>Demonstrates: <b>Generics</b> with two type parameters, <b>Optional</b>
 * return type, <b>interface</b>.
 *
 * @param <T>  the entity type managed by this repository
 * @param <ID> the type of the entity's primary key
 */
public interface Repository<T, ID> {

    /**
     * Persists (inserts or updates) an entity.
     *
     * @param entity the entity to save
     * @return the saved entity (may differ if the implementation enriches it)
     */
    T save(T entity);

    /**
     * Finds an entity by its primary key.
     *
     * @param id the primary key value
     * @return an {@link Optional} wrapping the entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Returns all entities managed by this repository.
     */
    List<T> findAll();

    /**
     * Removes the entity with the given primary key.
     *
     * @param id the primary key of the entity to remove
     * @return {@code true} if the entity was present and removed
     */
    boolean delete(ID id);

    /**
     * Returns the number of entities currently stored.
     */
    int count();

    /**
     * Returns {@code true} if an entity with the given ID exists.
     */
    boolean existsById(ID id);
}
