package com.hcl.petadoption.model;

/**
 * Concrete pet subclass representing a dog.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Inheritance</b>  – extends {@link Pet}</li>
 *   <li><b>Polymorphism</b> – overrides {@code getSpeciesInfo()} and {@code makeSound()}</li>
 *   <li><b>super</b>        – delegates to parent constructor</li>
 *   <li><b>this</b>         – used in convenience constructor chaining</li>
 * </ul>
 */
public class Dog extends Pet {

    /** Dog-specific field – will become a column in the dog sub-table or a pet_detail column (Assessment 2). */
    private boolean isTrainedForService;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor.
     *
     * @param petId              stable unique identifier
     * @param name               pet name
     * @param age                age in years
     * @param breed              breed description
     * @param gender             biological gender
     * @param status             current shelter status
     * @param isTrainedForService whether this dog is trained for service/assistance
     */
    public Dog(String petId, String name, int age, String breed,
               Gender gender, PetStatus status, boolean isTrainedForService) {
        super(petId, name, age, breed, gender, status);   // super keyword
        this.isTrainedForService = isTrainedForService;    // this keyword
    }

    /**
     * Convenience constructor – defaults {@code isTrainedForService} to false.
     * Demonstrates constructor chaining via {@code this(...)}.
     */
    public Dog(String petId, String name, int age, String breed,
               Gender gender, PetStatus status) {
        this(petId, name, age, breed, gender, status, false);  // this keyword
    }

    // -------------------------------------------------------------------------
    // Polymorphic overrides
    // -------------------------------------------------------------------------

    @Override
    public String getSpeciesInfo() {
        return isTrainedForService
                ? "Dog – Service/Assistance trained. Ideal for individuals needing support."
                : "Dog – Loyal and playful companion, great for families and active owners.";
    }

    @Override
    public String makeSound() {
        return "Woof! Woof!";
    }

    // -------------------------------------------------------------------------
    // Dog-specific accessor
    // -------------------------------------------------------------------------

    public boolean isTrainedForService() {
        return isTrainedForService;
    }

    public void setTrainedForService(boolean trainedForService) {
        this.isTrainedForService = trainedForService;
    }
}
