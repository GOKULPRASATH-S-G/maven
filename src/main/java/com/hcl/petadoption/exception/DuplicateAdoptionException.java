package com.hcl.petadoption.exception;

/**
 * Thrown when an attempt is made to approve a second adoption for a pet that
 * already has an approved (or adopted) status.
 *
 * <p>This is an <b>unchecked exception</b> because duplicate-adoption approval
 * violates a fundamental business invariant and should never happen under correct
 * usage – it signals a logic error in the caller.
 *
 * <p>Demonstrates: custom unchecked exception, meaningful error messaging.
 */
public class DuplicateAdoptionException extends RuntimeException {

    private final String petId;
    private final String existingApplicationId;

    public DuplicateAdoptionException(String petId, String existingApplicationId) {
        super(String.format(
                "Pet '%s' already has an approved adoption (Application: %s). "
                + "A pet can have only one approved adoption.",
                petId, existingApplicationId));
        this.petId = petId;
        this.existingApplicationId = existingApplicationId;
    }

    public String getPetId() {
        return petId;
    }

    public String getExistingApplicationId() {
        return existingApplicationId;
    }
}
