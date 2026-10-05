package com.hcl.petadoption.model;

/**
 * Concrete pet subclass representing a cat.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Inheritance</b>  – extends {@link Pet}</li>
 *   <li><b>Polymorphism</b> – overrides {@code getSpeciesInfo()} and {@code makeSound()}</li>
 *   <li><b>super</b>        – delegates to parent constructor</li>
 *   <li><b>this</b>         – used in constructor chaining</li>
 * </ul>
 */
public class Cat extends Pet {

    /** Cat-specific field – indoor-only preference. */
    private boolean isIndoorOnly;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor.
     *
     * @param petId        stable unique identifier
     * @param name         pet name
     * @param age          age in years
     * @param breed        breed description
     * @param gender       biological gender
     * @param status       current shelter status
     * @param isIndoorOnly whether this cat is suited only for indoor living
     */
    public Cat(String petId, String name, int age, String breed,
               Gender gender, PetStatus status, boolean isIndoorOnly) {
        super(petId, name, age, breed, gender, status);  // super keyword
        this.isIndoorOnly = isIndoorOnly;                 // this keyword
    }

    /**
     * Convenience constructor – defaults {@code isIndoorOnly} to true.
     */
    public Cat(String petId, String name, int age, String breed,
               Gender gender, PetStatus status) {
        this(petId, name, age, breed, gender, status, true);  // this keyword
    }

    // -------------------------------------------------------------------------
    // Polymorphic overrides
    // -------------------------------------------------------------------------

    @Override
    public String getSpeciesInfo() {
        return isIndoorOnly
                ? "Cat – Indoor-only. Prefers a calm, enclosed environment."
                : "Cat – Indoor/outdoor. Adaptable and independent, suits varied home types.";
    }

    @Override
    public String makeSound() {
        return "Meow~";
    }

    // -------------------------------------------------------------------------
    // Cat-specific accessor
    // -------------------------------------------------------------------------

    public boolean isIndoorOnly() {
        return isIndoorOnly;
    }

    public void setIndoorOnly(boolean indoorOnly) {
        this.isIndoorOnly = indoorOnly;
    }
}
