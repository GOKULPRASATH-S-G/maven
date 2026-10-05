package com.hcl.petadoption.repository;

import com.hcl.petadoption.model.AdoptionApplication;
import com.hcl.petadoption.model.AdoptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete adoption-application repository backed by {@link InMemoryRepository}.
 *
 * <p>Demonstrates Streams and Optional in query methods.
 */
public class AdoptionApplicationRepository extends InMemoryRepository<AdoptionApplication, String> {

    @Override
    protected String extractId(AdoptionApplication application) {
        return application.getApplicationId();
    }

    /**
     * Finds all applications for a specific pet (by petId).
     */
    public List<AdoptionApplication> findByPetId(String petId) {
        return findAll().stream()
                .filter(a -> a.getPetId().equals(petId))
                .collect(Collectors.toList());
    }

    /**
     * Finds all applications submitted by a specific adopter.
     */
    public List<AdoptionApplication> findByAdopterId(String adopterId) {
        return findAll().stream()
                .filter(a -> a.getAdopterId().equals(adopterId))
                .collect(Collectors.toList());
    }

    /**
     * Finds all applications with the given status.
     */
    public List<AdoptionApplication> findByStatus(AdoptionStatus status) {
        return findAll().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Returns the approved application for a pet, if one exists.
     * Used to enforce the one-approved-adoption-per-pet business rule.
     */
    public Optional<AdoptionApplication> findApprovedApplicationForPet(String petId) {
        return findAll().stream()
                .filter(a -> a.getPetId().equals(petId) && a.isApproved())
                .findFirst();
    }
}
