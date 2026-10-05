package com.hcl.petadoption.repository;

import com.hcl.petadoption.model.Adopter;

import java.util.Optional;

/**
 * Concrete adopter repository backed by {@link InMemoryRepository}.
 */
public class AdopterRepository extends InMemoryRepository<Adopter, String> {

    @Override
    protected String extractId(Adopter adopter) {
        return adopter.getAdopterId();
    }

    /**
     * Finds an adopter by their email address (natural unique key).
     */
    public Optional<Adopter> findByEmail(String email) {
        return findAll().stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
