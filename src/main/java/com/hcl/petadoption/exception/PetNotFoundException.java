package com.hcl.petadoption.exception;

/**
 * Thrown when a requested pet does not exist in the repository.
 *
 * <p>This is an <b>unchecked exception</b> ({@link RuntimeException} subclass)
 * because callers are not expected to recover from a missing pet reference at
 * runtime – the ID is either stale or incorrect, which is a programming/data error.
 *
 * <p>Demonstrates: custom unchecked exception, {@code throw} keyword.
 */
public class PetNotFoundException extends RuntimeException {

    private final String petId;

    public PetNotFoundException(String petId) {
        super("No pet found with ID: " + petId);
        this.petId = petId;
    }

    public PetNotFoundException(String petId, String message) {
        super(message);
        this.petId = petId;
    }

    /** Returns the ID that was not found, useful for logging/diagnostics. */
    public String getPetId() {
        return petId;
    }
}
