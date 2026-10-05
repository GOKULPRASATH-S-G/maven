package com.hcl.petadoption.interfaces;

import com.hcl.petadoption.model.Gender;
import com.hcl.petadoption.model.Pet;
import com.hcl.petadoption.model.PetStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for pet management operations.
 *
 * <p>Services and controllers (in later assessments) depend on this interface,
 * not concrete implementations – honouring the Dependency Inversion Principle.
 *
 * <p>Demonstrates: <b>interface</b>, <b>abstraction</b>, <b>Generics</b> (Optional, List),
 * <b>Comparator</b> parameter.
 */
public interface PetOperations {

    /**
     * Registers a new pet in the system.
     *
     * @param pet the pet to register (must not be null)
     * @throws IllegalArgumentException if a pet with the same ID already exists
     */
    void registerPet(Pet pet);

    /**
     * Finds a pet by its stable ID.
     *
     * @param petId the pet's unique identifier
     * @return an {@link Optional} containing the pet, or {@link Optional#empty()} if not found
     */
    Optional<Pet> findPetById(String petId);

    /**
     * Returns all registered pets sorted by their natural order (name).
     */
    List<Pet> listAllPets();

    /**
     * Filters pets by adoption status.
     *
     * @param status the status to filter by
     * @return list of pets matching the given status
     */
    List<Pet> filterByStatus(PetStatus status);

    /**
     * Filters pets by species type (Dog, Cat, …).
     *
     * @param speciesClass the concrete class to filter by
     * @param <T>          subtype of Pet
     * @return list of pets of the specified species
     */
    <T extends Pet> List<T> filterBySpecies(Class<T> speciesClass);

    /**
     * Filters available pets that contain ALL the specified traits.
     *
     * @param traits trait labels to match
     * @return list of matching pets
     */
    List<Pet> filterByTraits(List<String> traits);

    /**
     * Returns all pets sorted by the supplied {@link Comparator}.
     *
     * @param comparator the ordering strategy
     * @return sorted list
     */
    List<Pet> sortedBy(Comparator<Pet> comparator);

    /**
     * Returns a brief count summary of pets by status.
     */
    String getStatusSummary();
}
