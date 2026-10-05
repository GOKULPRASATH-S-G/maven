package com.hcl.petadoption.service;

import com.hcl.petadoption.exception.PetNotFoundException;
import com.hcl.petadoption.interfaces.PetOperations;
import com.hcl.petadoption.model.Pet;
import com.hcl.petadoption.model.PetStatus;
import com.hcl.petadoption.repository.PetRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementing pet management operations.
 *
 * <p>Implements {@link PetOperations} interface, honouring Dependency Inversion.
 * Business rules live here, not in the repository or the console layer.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Interface</b>    – implements {@link PetOperations}</li>
 *   <li><b>Streams</b>      – filter, sort, count, groupingBy</li>
 *   <li><b>Optional</b>     – findPetById return type</li>
 *   <li><b>Comparable</b>   – natural sort via {@code Collections.sort}</li>
 *   <li><b>Comparator</b>   – custom sort via {@code sortedBy}</li>
 *   <li><b>Generics</b>     – {@code filterBySpecies(Class<T>)}</li>
 *   <li><b>Exception</b>    – throws {@link PetNotFoundException}</li>
 * </ul>
 */
public class PetService implements PetOperations {

    private final PetRepository petRepository;

    // -------------------------------------------------------------------------
    // Static Comparators (reusable ordering strategies)
    // -------------------------------------------------------------------------

    /** Orders pets by age ascending (youngest first). */
    public static final Comparator<Pet> BY_AGE_ASC =
            Comparator.comparingInt(Pet::getAge);

    /** Orders pets by age descending (oldest first). */
    public static final Comparator<Pet> BY_AGE_DESC =
            Comparator.comparingInt(Pet::getAge).reversed();

    /** Orders pets by breed alphabetically. */
    public static final Comparator<Pet> BY_BREED =
            Comparator.comparing(Pet::getBreed, String.CASE_INSENSITIVE_ORDER);

    /** Orders pets by status display name. */
    public static final Comparator<Pet> BY_STATUS =
            Comparator.comparing(p -> p.getStatus().getDisplayName());

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // -------------------------------------------------------------------------
    // PetOperations implementation
    // -------------------------------------------------------------------------

    @Override
    public void registerPet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet must not be null.");
        }
        if (petRepository.existsById(pet.getPetId())) {
            throw new IllegalArgumentException(
                    "A pet with ID '" + pet.getPetId() + "' is already registered.");
        }
        petRepository.save(pet);
    }

    @Override
    public Optional<Pet> findPetById(String petId) {
        return petRepository.findById(petId);
    }

    /**
     * Returns all pets sorted by natural order (name, via {@link Comparable}).
     */
    @Override
    public List<Pet> listAllPets() {
        return petRepository.findAll().stream()
                .sorted()          // uses Pet.compareTo (Comparable – natural order by name)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pet> filterByStatus(PetStatus status) {
        return petRepository.findByStatus(status);
    }

    @Override
    public <T extends Pet> List<T> filterBySpecies(Class<T> speciesClass) {
        return petRepository.findBySpecies(speciesClass);
    }

    @Override
    public List<Pet> filterByTraits(List<String> traits) {
        return petRepository.findByTraits(traits);
    }

    /**
     * Returns pets sorted by the supplied {@link Comparator}, demonstrating
     * Comparator-based ordering separate from the natural Comparable order.
     */
    @Override
    public List<Pet> sortedBy(Comparator<Pet> comparator) {
        return petRepository.findAll().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Returns a status summary built via {@link java.util.stream.Collectors#groupingBy}.
     */
    @Override
    public String getStatusSummary() {
        Map<PetStatus, Long> counts = petRepository.findAll().stream()
                .collect(Collectors.groupingBy(Pet::getStatus, Collectors.counting()));

        StringBuilder sb = new StringBuilder("Pet Status Summary:\n");
        for (PetStatus status : PetStatus.values()) {
            sb.append(String.format("  %-15s : %d%n",
                    status.getDisplayName(),
                    counts.getOrDefault(status, 0L)));
        }
        sb.append(String.format("  %-15s : %d", "TOTAL", petRepository.count()));
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Helper used by other services
    // -------------------------------------------------------------------------

    /**
     * Finds a pet or throws {@link PetNotFoundException}.
     * Demonstrates: Optional.orElseThrow, custom unchecked exception.
     */
    public Pet findPetOrThrow(String petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
    }

    /** Updates the pet's status in the repository. */
    public void updatePetStatus(String petId, PetStatus newStatus) {
        Pet pet = findPetOrThrow(petId);
        pet.setStatus(newStatus);
        petRepository.save(pet);
    }

    public int getTotalCount() {
        return petRepository.count();
    }
}
