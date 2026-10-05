package com.hcl.petadoption.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class representing a pet in the shelter system.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Abstraction</b>  – abstract class with abstract methods</li>
 *   <li><b>Encapsulation</b> – private fields with accessor methods</li>
 *   <li><b>Inheritance</b>  – {@link Dog} and {@link Cat} extend this class</li>
 *   <li><b>Polymorphism</b> – {@code getSpeciesInfo()} and {@code makeSound()} are overridden in subclasses</li>
 *   <li><b>Comparable</b>   – natural ordering by name (case-insensitive)</li>
 *   <li><b>java.time</b>    – not needed directly on Pet; used in related entities</li>
 * </ul>
 *
 * <p>Persistence note: {@code petId} is the stable surrogate key that will become the
 * primary key in the {@code pet} table during Assessment 2.
 */
public abstract class Pet implements Comparable<Pet> {

    private final String petId;
    private String name;
    private int age;          // in years
    private String breed;
    private Gender gender;
    private PetStatus status;

    /**
     * Traits use a Set to guarantee uniqueness and model the many-to-many
     * relationship (pet_traits junction table) that will be introduced in Assessment 2.
     */
    private final Set<String> traits;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Primary constructor.  All subclasses must call {@code super(...)} to ensure
     * every Pet has a valid, stable identity from creation.
     *
     * @param petId  stable unique identifier (never null)
     * @param name   pet name (never null)
     * @param age    age in years (>= 0)
     * @param breed  breed description
     * @param gender biological gender
     * @param status current shelter/adoption status
     */
    protected Pet(String petId, String name, int age, String breed, Gender gender, PetStatus status) {
        if (petId == null || petId.isBlank()) {
            throw new IllegalArgumentException("Pet ID must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pet name must not be blank.");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age must be non-negative.");
        }
        this.petId  = petId;
        this.name   = name;
        this.age    = age;
        this.breed  = breed;
        this.gender = gender;
        this.status = status;
        this.traits = new HashSet<>();
    }

    // -------------------------------------------------------------------------
    // Abstract methods – force subclasses to supply species-specific behaviour
    // -------------------------------------------------------------------------

    /**
     * Returns a species-specific description (polymorphic behaviour).
     * Overridden by {@link Dog} and {@link Cat}.
     */
    public abstract String getSpeciesInfo();

    /**
     * Returns a species-typical sound representation (polymorphic behaviour).
     */
    public abstract String makeSound();

    // -------------------------------------------------------------------------
    // Trait management (uses the Set<String> collection)
    // -------------------------------------------------------------------------

    /**
     * Adds a trait to this pet's trait set.  Duplicate traits are silently ignored
     * because {@code Set} already guarantees uniqueness.
     *
     * @param trait trait label (e.g. "vaccinated", "house-trained")
     */
    public void addTrait(String trait) {
        if (trait != null && !trait.isBlank()) {
            traits.add(trait.toLowerCase().trim());
        }
    }

    /**
     * Returns an unmodifiable view of the trait set so callers cannot mutate it directly.
     */
    public Set<String> getTraits() {
        return Collections.unmodifiableSet(traits);
    }

    // -------------------------------------------------------------------------
    // Comparable – natural ordering by name (case-insensitive)
    // -------------------------------------------------------------------------

    @Override
    public int compareTo(Pet other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    // -------------------------------------------------------------------------
    // Accessors / Mutators
    // -------------------------------------------------------------------------

    public String getPetId() {
        return petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age must be non-negative.");
        }
        this.age = age;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public PetStatus getStatus() {
        return status;
    }

    public void setStatus(PetStatus status) {
        this.status = status;
    }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted multi-line summary.  Subclasses contribute via
     * {@code getSpeciesInfo()} demonstrating polymorphism.
     */
    public String getSummary() {
        return String.format(
                "  ID      : %s%n" +
                "  Name    : %s%n" +
                "  Species : %s%n" +
                "  Breed   : %s%n" +
                "  Age     : %d yr(s)%n" +
                "  Gender  : %s%n" +
                "  Status  : %s%n" +
                "  Traits  : %s%n" +
                "  Sound   : %s%n" +
                "  Info    : %s",
                petId, name, getClass().getSimpleName(), breed, age,
                gender.getDisplayName(), status.getDisplayName(),
                traits.isEmpty() ? "none" : String.join(", ", traits),
                makeSound(), getSpeciesInfo());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) – %s – Status: %s",
                petId, name, getClass().getSimpleName(), breed, status.getDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pet other)) return false;
        return petId.equals(other.petId);
    }

    @Override
    public int hashCode() {
        return petId.hashCode();
    }
}
