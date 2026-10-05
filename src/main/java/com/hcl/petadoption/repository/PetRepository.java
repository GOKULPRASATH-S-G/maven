package com.hcl.petadoption.repository;

import com.hcl.petadoption.model.Pet;
import com.hcl.petadoption.model.PetStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete pet repository backed by the generic {@link InMemoryRepository}.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Generics</b>    – specialises {@code InMemoryRepository<Pet, String>}</li>
 *   <li><b>Streams</b>     – used for status-based and species-based queries</li>
 *   <li><b>Inheritance</b> – extends {@link InMemoryRepository}</li>
 * </ul>
 */
public class PetRepository extends InMemoryRepository<Pet, String> {

    @Override
    protected String extractId(Pet pet) {
        return pet.getPetId();
    }

    /**
     * Returns all pets with the given {@link PetStatus}.
     * Uses Stream + filter demonstrating the Stream API.
     */
    public List<Pet> findByStatus(PetStatus status) {
        return findAll().stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Returns all pets of the given concrete species class.
     * Demonstrates a bounded wildcard / generic method usage.
     *
     * @param <T>          concrete subtype of Pet
     * @param speciesClass the class literal for the species
     */
    public <T extends Pet> List<T> findBySpecies(Class<T> speciesClass) {
        return findAll().stream()
                .filter(speciesClass::isInstance)
                .map(speciesClass::cast)
                .collect(Collectors.toList());
    }

    /**
     * Returns pets that have ALL of the requested traits.
     */
    public List<Pet> findByTraits(List<String> traits) {
        if (traits == null || traits.isEmpty()) {
            return findAll();
        }
        List<String> normalised = traits.stream()
                .map(t -> t.toLowerCase().trim())
                .collect(Collectors.toList());
        return findAll().stream()
                .filter(p -> p.getTraits().containsAll(normalised))
                .collect(Collectors.toList());
    }
}
