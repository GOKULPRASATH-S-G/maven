package com.hcl.petadoption.model;

/**
 * Represents the adoption/availability status of a pet in the shelter.
 * Designed to map cleanly to a database ENUM column in Assessment 2.
 */
public enum PetStatus {
    AVAILABLE("Available for Adoption"),
    PENDING("Adoption Pending"),
    ADOPTED("Adopted"),
    UNAVAILABLE("Not Available");

    private final String displayName;

    PetStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
